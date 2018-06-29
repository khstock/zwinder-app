package kast0013.zwinder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
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

    private FirebaseAuth firebaseAuth;

    private String myUID;
    private DatabaseReference usersDb;

    ListView listView;
    List<cards> rowItems;

    @BindView(R.id.btn_logout) Button _logoutButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        usersDb = FirebaseDatabase.getInstance().getReference().child("UIDs");

        firebaseAuth = FirebaseAuth.getInstance();          //greift firebase auth instanz
        myUID = firebaseAuth.getCurrentUser().getUid();     //greift eigene user id

        DatabaseReference interedb = usersDb.child(myUID).child("interests");
        interedb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    myInterests = dataSnapshot.getValue().toString();
                    Toast.makeText(MainActivity.this, myInterests, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //logout button
        _logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        rowItems = new ArrayList<cards>();
        arrayAdapter = new arrayAdapter(this, R.layout.item, rowItems);

        SwipeFlingAdapterView flingContainer = findViewById(R.id.frame);

        flingContainer.setAdapter(arrayAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                Log.d("LIST", "removed object!");
                rowItems.remove(0);
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
                // Ask for more data here
                //al.add("XML ".concat(String.valueOf(i)));
                checkUser();
                arrayAdapter.notifyDataSetChanged();
                Log.d("LIST", "notified");
                i++;
            }

            @Override
            public void onScroll(float scrollProgressPercent) {
            }

        });


        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {
                Toast.makeText(MainActivity.this, "CLICKED!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void checkMatch(final String swipeUID){          //überpfürft, ob der andere diesen user geswiped und geliked hat
        DatabaseReference swipeUser = usersDb.child(swipeUID).child("Swipes").child(myUID);     //checkt, ob dieser user vom anderen geliked wurde
        swipeUser.addValueEventListener(new ValueEventListener() {
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

    //GET USERS * WHERE GENDER = MY USER>INTERESTS && != MY USERS > SWIPES > UID

    public void checkUser(){
        //QUERIES!
        //DatabaseReference maleUserDb = FirebaseDatabase.getInstance().getReference().child("UIDs");
        //Query potentialUser = usersDb.orderByChild("gender").equalTo("Male");
        //Query potentialUser = usersDb.child("gender").equalTo("Male");
        usersDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.exists() && dataSnapshot.child("gender").hasChild(myInterests)){
                    //!dataSnapshot.child(myUID).child("Swipes").hasChild(dataSnapshot.getKey())
                    cards item = new cards(dataSnapshot.getKey(), dataSnapshot.child("name").getValue().toString());
                    rowItems.add(item);
                    arrayAdapter.notifyDataSetChanged();            //update das arrayAdapter
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