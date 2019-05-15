package com.android.navada.donit.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.android.navada.donit.R;
import com.android.navada.donit.threads.SignInThread;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText emailEditText;
    private EditText passwordEditText;
    private TextView forgotPasswordTextView;
    public static CheckBox rememberMeCheckBox;
    private Intent intent;
    public  ProgressDialog progressDialog;
    private SharedPreferences sharedPreferences;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private String email;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        initialize();

    }

    @Override
    protected void onResume() {
        super.onResume();

        attachListeners();

        if(sharedPreferences.getBoolean("remember", false)){
            emailEditText.setText(sharedPreferences.getString("email", ""));
            passwordEditText.setText(sharedPreferences.getString("password", ""));
            rememberMeCheckBox.setChecked(true);
        }

    }

    private void initialize(){

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait");
        progressDialog.setCancelable(false);
        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = this.getSharedPreferences("com.android.navada.donit", Context.MODE_PRIVATE);

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                final FirebaseUser user = firebaseAuth.getCurrentUser();

                if(user != null) {

                    if(!progressDialog.isShowing())
                        progressDialog.show();

                    if(user.isEmailVerified())
                        checkIfUserIsOrganization(firebaseAuth);
                    else {
                        FirebaseAuth.getInstance().signOut();
                        progressDialog.cancel();
                        makeToast("Please verify your email!");
                    }

                }

            }
        };

    }

    private void checkIfUserIsOrganization(final FirebaseAuth firebaseAuth){

        FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                HashMap<String, Object> user = (HashMap<String, Object>) dataSnapshot.getValue();
                if(!user.get("typeOfUser").equals("organization"))
                    startMainActivity();
                else if(user.get("approved").equals(false)){
                    progressDialog.cancel();
                    firebaseAuth.signOut();
                    makeToast("Not approved by superadmin yet!");
                }
                else
                    startMainActivity();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                makeToast(databaseError.getMessage());
                progressDialog.cancel();

            }
        });


    }

    private void startMainActivity(){

        progressDialog.cancel();
        intent = new Intent(SignInActivity.this, MainActivity.class);
        startActivity(intent);

    }

    private void attachListeners(){

        forgotPasswordTextView.setOnClickListener(this);
        mAuth.addAuthStateListener(authStateListener);

    }

    public void onClickSignInButton(View view){

        email = emailEditText.getText().toString().trim();
        password = passwordEditText.getText().toString();
        if(email.isEmpty() || password.isEmpty())
            makeToast("Fields cannot be empty!");
        else {
            progressDialog.show();
            Thread thread = new SignInThread(email, password, SignInActivity.this);
            thread.start();
        }

    }

    public void onClickDonorSignUpButton(View view){

        intent = new Intent(this, DonorSignUpActivity.class);
        startActivity(intent);

    }

    public void onClickOrganisationSignUpButton(View view){

        intent = new Intent(this, OrganisationSignUpActivity.class);
        startActivity(intent);

    }

    private void makeToast(String message){

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mAuth != null && !progressDialog.isShowing())
            mAuth.removeAuthStateListener(authStateListener);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.forgotPasswordTextView : intent = new Intent(this, ResetPasswordActivity.class);
                                               startActivity(intent);
                                               break;

        }
    }
}
