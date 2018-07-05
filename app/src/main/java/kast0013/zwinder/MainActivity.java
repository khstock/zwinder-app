package kast0013.zwinder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends Activity {

    private cards karten[];
    private arrayAdapter arrayAdapter;
    private int i;
    private String myInterests = "none";
    private List<String> myGender;

    private FirebaseAuth firebaseAuth;

    private String myUID;
    private DatabaseReference usersDb;

    List<cards> swipeCardArray;

    @BindView(R.id.btn_settings) Button _settingsButton;
    @BindView(R.id.btn_matches) Button _matchesButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        usersDb = FirebaseDatabase.getInstance().getReference().child("UIDs");

        firebaseAuth = FirebaseAuth.getInstance();          //greift firebase auth instanz
        myUID = firebaseAuth.getCurrentUser().getUid();     //greift eigene user id

        //Meine Interessen lokasl abspeichern (vereinfacht Query)
        DatabaseReference interesseDb = usersDb.child(myUID).child("interests");
        interesseDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    myInterests = dataSnapshot.getValue().toString();
                    Toast.makeText(MainActivity.this, myInterests, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Mein Gender Lokal abspeichern (vereinfacht Query)
        myGender = new ArrayList<String>();
        DatabaseReference genderDb = usersDb.child(myUID).child("gender");
        genderDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.d("LIST", snapshot.getKey().toString());
                    myGender.add(snapshot.getKey().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        //Log.d("LIST", myGender.get(0));

        //Settings Button
        _settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), UserSettingsActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //Matches Button
        _matchesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MatchesActivity.class);
                startActivity(intent);
                finish();
            }
        });


        //Array Adapter und Swipekarten klasse anlegen
        swipeCardArray = new ArrayList<cards>();
        arrayAdapter = new arrayAdapter(this, R.layout.item, swipeCardArray);

        //Swipecards Library, hier wird die Swipecard + Listener angelegt
        SwipeFlingAdapterView flingContainer = findViewById(R.id.frame);
        flingContainer.setMinStackInAdapter(1);
        flingContainer.setAdapter(arrayAdapter);

        //Listener (Swipecard)
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                Log.d("LIST", "removed object!");
                swipeCardArray.remove(0);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                //Beim Swipen der Karte nach links
                //dataObject in eigenes Objekt casten
                cards kartenObjekt = (cards) dataObject;    //Um die Aktuelle Swipecard zu
                String cardUID = kartenObjekt.getUserId();
                String cardName = kartenObjekt.getName();

                usersDb.child(cardUID).child("Swipes").child(myUID).setValue("dislikes");
                Toast.makeText(MainActivity.this, "Disliked " + cardName, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                //Beim Swipen der Karte nach rechts
                //dataObject in eigenes Objekt casten
                cards kartenObjekt = (cards) dataObject;    //Um die Aktuelle Swipecard zu
                String cardUID = kartenObjekt.getUserId();
                String cardName = kartenObjekt.getName();

                usersDb.child(cardUID).child("Swipes").child(myUID).setValue("likes");
                checkMatch(cardUID);                                                    //Prüft ob die User sich gegenseitig mögen

                Toast.makeText(MainActivity.this, "liked " + cardName, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                checkUser();
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onScroll(float scrollProgressPercent) {
            }

        });
    }

    //Stellt fest, ob sich zwei User gegenseitig geliked haben, falls ja, zeigt es das Match an
    public void checkMatch(final String swipeUID){          //überpfürft, ob der andere diesen user geswiped und geliked hat
        DatabaseReference whoLikesMe = usersDb.child(myUID).child("Swipes").child(swipeUID);     //checkt, ob dieser user vom anderen geliked wurde
        whoLikesMe.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){                                            //falls dieser User dort geswiped wurde
                    if (dataSnapshot.getValue().toString().equals("likes")) {           //prüft ob dieser user diesen liked
                        usersDb.child(myUID).child("Matches").child(swipeUID).setValue("true");     //Setzt das match bei diesem User
                        usersDb.child(swipeUID).child("Matches").child(myUID).setValue("true");     //Setzt das match bei gematchtem User
                        Toast.makeText(MainActivity.this, "ITS A MATCH!", Toast.LENGTH_SHORT).show();   //Kündigt es an
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //Die Query, aufwendig erklärt in der Dokumentation
    public void checkUser() {
        Query potentialUser = FirebaseDatabase.getInstance().getReference("UIDs");
        potentialUser.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.exists() && dataSnapshot.child("gender").hasChild(myInterests)){
                    if (myGender.contains(dataSnapshot.child("interests").getValue().toString())){
                        if(!dataSnapshot.child("Swipes").hasChild(myUID)) {
                            cards item = new cards(dataSnapshot.getKey(), dataSnapshot.child("name").getValue().toString(), dataSnapshot.child("ProfilePictureUrl").getValue().toString());
                            swipeCardArray.add(item);
                            arrayAdapter.notifyDataSetChanged();            //update das arrayAdapter
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}