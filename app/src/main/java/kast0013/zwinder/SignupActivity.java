package kast0013.zwinder;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.ButterKnife;
import butterknife.BindView;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";

    //Firebase Auths
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    //Bindviews mit Butterknife
    @BindView(R.id.input_name) EditText _nameText;
    @BindView(R.id.input_email) EditText _emailText;
    @BindView(R.id.input_password) EditText _passwordText;
    @BindView(R.id.btn_signup) Button _signupButton;
    @BindView(R.id.link_login) TextView _loginLink;
    @BindView(R.id.radio_gender) RadioGroup _radioGender;
    //Sexualität/Interessen Checkboxen: Male, Female, Other
    @BindView(R.id.interest_text) TextView _interestText;
    @BindView(R.id.interest_checkbox1) CheckBox _interestBox1;
    @BindView(R.id.interest_checkbox2) CheckBox _interestBox2;
    @BindView(R.id.interest_checkbox3) CheckBox _interestBox3;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        //Firebase Auth und Listener initalisieren
        firebaseAuth = FirebaseAuth.getInstance();

        //Falls User authentifiziert -> weiterleiten an onSignupSucess() -> MainActivity
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null){
                    onSignupSuccess();
                }
            }
        };

        //Button: Startet Registrierung mit eingegebenen Daten
        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        //TextView: Geht zurück zur LoginActivity, wenn gedrückt
        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void signup() {
        //Validieren der Eingabewerte, andernfalls -> Failed
        if (!validate()) {
            onSignupFailed();
            return;
        }

        //Ausblenden des Signup Buttons
        _signupButton.setEnabled(false);

        //ProgressDialog für den Vorgang
        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Account wird erstellt...");
        progressDialog.show();

        //Name, E-Mail, Passwort
        final String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        //Gender Auswahl Male/Female/Other
        int selectId = _radioGender.getCheckedRadioButtonId();
        final RadioButton radioButton = findViewById(selectId);
        if(radioButton.getText() == null) onSignupFailed();

        //Firebase Logik zum Eintragen von DB Einträgen
        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    progressDialog.dismiss();
                                    //UserID dieses erstellten Users
                                    String userId = firebaseAuth.getCurrentUser().getUid();

                                    //Datenbankverweise um übersichtlich darzustellen, was im nächsten Schritt angelegt wird
                                    DatabaseReference dBUserName = FirebaseDatabase.getInstance().getReference().child("UIDs").child(userId).child("name");
                                    DatabaseReference dBUserGender = FirebaseDatabase.getInstance().getReference().child("UIDs").child(userId).child("gender");
                                    DatabaseReference dBUserInteressen = FirebaseDatabase.getInstance().getReference().child("UIDs").child(userId).child("interests");
                                    DatabaseReference dbUserPicture = FirebaseDatabase.getInstance().getReference().child("UIDs").child(userId).child("ProfilePictureUrl");
                                    DatabaseReference dbUserSwipes = FirebaseDatabase.getInstance().getReference().child("UIDs").child(userId).child("Swipes");

                                    //Setzen der Werte in der Datenbank
                                    dBUserName.setValue(name);                                      //namen in Datenbank setzen
                                    dBUserGender.setValue(radioButton.getText().toString());        //Geschlecht in Datenbank setzen
                                    dbUserPicture.setValue("https://firebasestorage.googleapis.com/v0/b/zwinder-32ccf.appspot.com/o/noProfile.jpg?alt=media&token=5cdf2ece-ff00-4ef1-a229-fcbd09b2c7df"); //Default Image
                                    setupGenderQuery(dBUserGender, radioButton.getText().toString());

                                    //Sich selbst "Dislike" Swipen, nimmt arbeit für späteres Filtern ab
                                    dbUserSwipes.child(userId).setValue("dislikes");

                                    //Sexualität/Interessen als ein Schlagwort vereinfacht, um später vereinfacht Daten zu filtern
                                    String interessen = "";
                                    String m = "Male"; String f = "Female"; String o = "Other";
                                    if(_interestBox1.isChecked()) interessen += m;
                                    if(_interestBox2.isChecked()) interessen += f;
                                    if(_interestBox3.isChecked()) interessen += o;
                                    dBUserInteressen.setValue(interessen);                          //Abspeichern der Interessen in DB
                                }
                                else{
                                    onSignupFailed();
                                }

                            }
                        }
                );
    }

    //Einteilen der Geschlechter in Brackets entsprechend der Interessen, vereinfacht filtern von potenziellen Matches
    // Male -> Alle Gender Keywords, welche Male enthalten
    // Female -> Alle Gender Keywords, welche Female enthalten
    // Other -> Alle Gender Keywords, welche Other enthalten
    public void setupGenderQuery(DatabaseReference db, String gender){
        switch (gender){
            case "Male":
                db.child("Male").setValue("true");
                db.child("MaleFemale").setValue("true");
                db.child("MaleOther").setValue("true");
                db.child("MaleFemaleOther").setValue("true");
                break;
            case "Female":
                db.child("Female").setValue("true");
                db.child("MaleFemale").setValue("true");
                db.child("FemaleOther").setValue("true");
                db.child("MaleFemaleOther").setValue("true");
                break;
            case "Other":
                db.child("Other").setValue("true");
                db.child("MaleOther").setValue("true");
                db.child("FemaleOther").setValue("true");
                db.child("MaleFemaleOther").setValue("true");
        }
    }

    //User über Firebase Auth erkannt -> direkt zur MainActivity weiterleiten
    public void onSignupSuccess() {
        _signupButton.setEnabled(true);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    //Fehlgeschlagener Registrierungsversuch + Meldung
    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
        _signupButton.setEnabled(true);
    }

    //Validieren von Eingabewerten
    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        //Name: Nicht leer, mindestens 3 Zeichen
        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("Minimum: 3 Zeichen");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        //E-Mail wird über android util Pattern match validiert (android doku)
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("Bitte gib eine korrekte E-Mail Adresse an");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        //Passowrt zwischen 4 und 10 Zeichen
        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("Zwischen 4-10 alphanumerische Zeichen");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        //Mindestens eine Interesse muss gewählt werden
        if (!_interestBox1.isChecked() && !_interestBox2.isChecked() && !_interestBox3.isChecked()) {
            _interestText.setError("Eine Interesse wählen, kein Dating für Asexuelle");
            valid = false;
        } else {
            _interestBox1.setError(null);
        }

        return valid;
    }

    //fireBaseAuth listener initialisieren/entfernen
    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(firebaseAuthListener);
    }
    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(firebaseAuthListener);
    }
}