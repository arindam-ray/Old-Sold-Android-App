package com.avinabaray.oldsold;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    public static FirebaseUser currentUser;
    public static String CURRENT_USER_EMAIL;
    public static String CURRENT_USER_ID;
    public static String CURRENT_USER_ROLE;

    private EditText editTextEmail;
    private EditText editTextPassword;
    private String stringEmail;
    private String stringPassword;

    private AlertDialog.Builder alertBuilderMainActivity;
    private CollectionReference mDocRef = FirebaseFirestore.getInstance().collection("users");
    CommonMethods commonMethods = new CommonMethods();

    public SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onStart() {
        super.onStart();

        // Retrieve SharePReference data
        CURRENT_USER_ID = pref.getString("CURRENT_USER_ID", "");
        CURRENT_USER_ROLE = pref.getString("CURRENT_USER_ROLE", "");

        if (CURRENT_USER_ID.isEmpty()) {
            currentUser = mAuth.getCurrentUser();
            updateUI(currentUser);
        } else {
            Log.wtf("ID", CURRENT_USER_ID);
            // Intents start
            Intent intentFromMainActivity;
            if (CURRENT_USER_ROLE.equals("Customer")) {
                intentFromMainActivity = new Intent(MainActivity.this, CustomerLoggedIn.class);
            } else {
                intentFromMainActivity = new Intent(MainActivity.this, SellerLoggedIn.class);
            }
            startActivity(intentFromMainActivity);
            currentUser = mAuth.getCurrentUser();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextEmail = findViewById(R.id.editTextPhone);
        editTextPassword = findViewById(R.id.editTextPassword);

        // Initializing SharedPreferences
        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();

        mAuth = FirebaseAuth.getInstance();
        alertBuilderMainActivity = new AlertDialog.Builder(MainActivity.this);
    }

    @Override
    public void onBackPressed() {
        // Uncomment the line below to enable BACK button
//        super.onBackPressed();
    }

    public void login(View view){
        stringEmail = editTextEmail.getText().toString();
        stringPassword = editTextPassword.getText().toString();

        if(stringEmail.isEmpty() || stringPassword.isEmpty()) {
            commonMethods.createAlert(alertBuilderMainActivity, "Please fill all the fields");
            return;
        }

        commonMethods.loadingDialogStart(this);
        mAuth.signInWithEmailAndPassword(stringEmail, stringPassword).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                commonMethods.loadingDialogStop();
                if(task.isSuccessful()){
                    // Sign in success
                    FirebaseUser user = mAuth.getCurrentUser();
                    updateUI(user);
                } else {
                    // Sign in fails
                    String taskErrorMsg = task.getException().getMessage();
                    if (taskErrorMsg == null) {
                        taskErrorMsg = "Authentication Failed";
                    }
                    commonMethods.createAlert(alertBuilderMainActivity, taskErrorMsg);
                    updateUI(null);
                }
            }
        });


    }

    public void register(View view) {
        Intent intentToRegisterUser = new Intent(this, RegisterUser.class);
        startActivity(intentToRegisterUser);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            commonMethods.loadingDialogStart(this);
            CURRENT_USER_EMAIL = user.getEmail();

            // Fetch user details from Firestore
            mDocRef.whereEqualTo("email", CURRENT_USER_EMAIL).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        CURRENT_USER_ID = documentSnapshot.getId();
                        CURRENT_USER_ROLE = documentSnapshot.getString("userRole");

                        // Writing SharedPreferences
                        editor.putString("CURRENT_USER_ID", CURRENT_USER_ID);
                        editor.putString("CURRENT_USER_ROLE", CURRENT_USER_ROLE);
                        editor.commit();

                        Log.wtf("CURRENT_USER_ID", CURRENT_USER_ID);
                        Log.wtf("CURRENT_USER_ROLE", CURRENT_USER_ROLE);

                        // Intents start
                        Intent intentFromMainActivity;
                        if (CURRENT_USER_ROLE.equals("Customer")) {
                            intentFromMainActivity = new Intent(MainActivity.this, CustomerLoggedIn.class);
                        } else {
                            intentFromMainActivity = new Intent(MainActivity.this, SellerLoggedIn.class);
                        }
                        commonMethods.loadingDialogStop();
                        startActivity(intentFromMainActivity);
                    }
                }
            });
        } else {
            editTextEmail.setText("");
            editTextPassword.setText("");
        }
    }
}
