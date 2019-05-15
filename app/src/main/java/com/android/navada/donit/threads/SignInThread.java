package com.android.navada.donit.threads;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import android.widget.Toast;
import com.android.navada.donit.activities.SignInActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignInThread extends Thread {

    private String email;
    private String password;
    private FirebaseAuth mAuth;
    private SignInActivity activity;

    public SignInThread(String email, String password, SignInActivity activity){

        this.email = email;
        this.password = password;
        this.activity = activity;
        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public void run() {

        mAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {

            @Override
            public void onSuccess(AuthResult authResult) {

                SharedPreferences sharedPreferences = activity.getSharedPreferences("com.android.navada.donit", Context.MODE_PRIVATE);
                sharedPreferences.edit().putBoolean("remember",SignInActivity.rememberMeCheckBox.isChecked()).apply();
                sharedPreferences.edit().putString("email", email).apply();
                sharedPreferences.edit().putString("password", password).apply();



            }

        }).addOnFailureListener(new OnFailureListener() {

            @Override
            public void onFailure(@NonNull Exception e) {

                activity.progressDialog.cancel();
                makeToast(e.getMessage());

            }

        });

    }

    private void makeToast(String message){

        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();

    }

}
