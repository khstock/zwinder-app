package kast0013.zwinder;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

    @BindView(R.id.input_name) EditText _nameText;
    @BindView(R.id.input_email) EditText _emailText;
    @BindView(R.id.input_password) EditText _passwordText;
    @BindView(R.id.btn_signup) Button _signupButton;
    @BindView(R.id.link_login) TextView _loginLink;
    @BindView(R.id.radio_gender) RadioGroup _radioGender;
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
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null){
                    onSignupSuccess();
                }
            }
        };

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                finish();
            }
        });
    }

    public void signup() {
        Log.d(TAG, "Registrieren");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Account wird erstellt...");
        progressDialog.show();

        final String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        //Gender Auswahl Male/Female/Other
        int selectId = _radioGender.getCheckedRadioButtonId();
        final RadioButton radioButton = findViewById(selectId);
        if(radioButton.getText() == null) onSignupFailed();

        //Meine Firebase Logik
        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    progressDialog.dismiss();
                                    String userId = firebaseAuth.getCurrentUser().getUid();
                                    //DatabaseReference createUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child(radioButton.getText().toString()).child(userId).child("name");
                                    DatabaseReference dBUserName = FirebaseDatabase.getInstance().getReference().child("UIDs").child(userId).child("name");
                                    DatabaseReference dBUserGender = FirebaseDatabase.getInstance().getReference().child("UIDs").child(userId).child("gender");
                                    DatabaseReference dBUserInteressen = FirebaseDatabase.getInstance().getReference().child("UIDs").child(userId).child("interests");

                                    dBUserName.setValue(name);                                      //namen in Datenbank setzen
                                    dBUserGender.setValue(radioButton.getText().toString());        //Geschlecht in Datenbank setzen
                                    setupGenderQuery(dBUserGender, radioButton.getText().toString());

                                    //Sexualität/InteressenQuery für später vorbereiten
                                    String interessen = "";
                                    String m = "Male"; String f = "Female"; String o = "Other";
                                    if(_interestBox1.isChecked()) interessen += m;
                                    if(_interestBox2.isChecked()) interessen += f;
                                    if(_interestBox3.isChecked()) interessen += o;
                                    dBUserInteressen.setValue(interessen);

                                    //onSignupSuccess();
                                    //benutze stattdessen firebase auth listener zum feststellen ob auth
                                }
                                else{
                                    onSignupFailed();
                                }

                            }
                        }
                );

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onSignupSuccess or onSignupFailed
                        // depending on success
                        // onSignupSuccess();
                        // onSignupFailed();

                    }
                }, 3000);
    }

    public void setupGenderQuery(DatabaseReference db, String gender){
        String someThing;
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

    public void onSignupSuccess() {
        _signupButton.setEnabled(true);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("Minimum: 3 Zeichen");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("Bitte gib eine korrekte E-Mail Adresse an");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("Zwischen 4-10 alphanumerische Zeichen");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

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