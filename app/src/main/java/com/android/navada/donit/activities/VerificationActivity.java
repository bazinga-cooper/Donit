package com.android.navada.donit.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.android.navada.donit.R;
import com.android.navada.donit.pojos.Organization;
import com.android.navada.donit.pojos.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class VerificationActivity extends AppCompatActivity {

    private TextView timerTextView;
    private EditText verificationCodeEditText;
    private ProgressDialog mProgressDialog;
    private CountDownTimer mCountDownTimer;
    private PhoneAuthProvider.ForceResendingToken token;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private long left;
    private User user;
    private Organization organization;
    private String verificationId = "";
    private String mobileNumber = "";
    private String email = "";
    private String password = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        getData();
        initialize();
        startTimer(65000);

    }

    private void getData() {

        Intent intent = getIntent();
        String userType = intent.getStringExtra("userType");
        verificationId = intent.getStringExtra("verificationId");
        mobileNumber = intent.getStringExtra("mobileNumber");
        email = intent.getStringExtra("email");
        password = intent.getStringExtra("password");

        if (userType.equals("organization")){
            organization = new Organization(intent.getStringExtra("name"), email,
                    mobileNumber, intent.getStringExtra("type"),
                    intent.getStringExtra("city"), intent.getStringExtra("address"),
                    intent.getStringExtra("cause"), userType, false);
        organization.setLatitude(intent.getDoubleExtra("latitude", 0));
        organization.setLongitude(intent.getDoubleExtra("longitude", 0));
    }

        else {
            HashMap<String, String> orgs = new HashMap<>();
            if(userType.equals("volunteer"))
            orgs.put(intent.getStringExtra("organization"), "false");
            user = new User(intent.getStringExtra("name"), email,
                    mobileNumber, intent.getStringExtra("city"), userType, orgs);
        }

        token = userType.equals("donor") ? DonorSignUpActivity.token : OrganisationSignUpActivity.token;


    }

    private void initialize(){

        timerTextView = findViewById(R.id.timerTextView);
        verificationCodeEditText = findViewById(R.id.verificationCodeEditText);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        left = 1000;

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) { }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                mProgressDialog.cancel();
                makeToast(e.getMessage());
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {

                super.onCodeSent(verificationId, forceResendingToken);
                mProgressDialog.cancel();
                startTimer(65100);
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(String s) {
                super.onCodeAutoRetrievalTimeOut(s);
            }
        };

    }

    private CountDownTimer getCountDownTimer(long initialMilliSecs){

        left = 1000;

        return new CountDownTimer(initialMilliSecs, 1000) {
            @Override
            public void onTick(long l) {

                Log.i("Hello", "onTick: " + left);
                long sec = l / 1000 - 5;
                left = l;
                if(left < 5000){
                    makeToast("Time out! Try again");
                    finish();
                }
                else {
                    String mTimeToDisplay = sec + ":00";
                    timerTextView.setText(mTimeToDisplay);
                }
            }

            @Override
            public void onFinish() { }
        };

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if(mCountDownTimer!=null)
            mCountDownTimer.cancel();
    }

    private void startTimer(long initialMilliSecs){

        if(mCountDownTimer!=null)
            mCountDownTimer.cancel();

        mCountDownTimer = getCountDownTimer(initialMilliSecs);
        mCountDownTimer.start();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void makeToast(String message){

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

    }

    public void onClickVerifyButton(View view){

        String codeEntered = verificationCodeEditText.getText().toString().trim();

        if(codeEntered.isEmpty())
            makeToast("Please enter the verification code");
        else{

            mCountDownTimer.cancel();
            mProgressDialog.setMessage("Verifying Credentials");
            mProgressDialog.show();

            PhoneAuthCredential mPhoneAuthCredential = PhoneAuthProvider.getCredential(verificationId,codeEntered);
            final FirebaseAuth mAuth = FirebaseAuth.getInstance();

            mAuth.signInWithCredential(mPhoneAuthCredential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {

                    deleteUser(mAuth);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    mProgressDialog.cancel();
                    makeToast(e.getMessage());

                    if(left<=6000)
                    {
                        makeToast("Time out! Try again");
                        finish();
                    }

                    verificationCodeEditText.setText("");
                    startTimer(left);

                }
            });

        }


    }

    private void deleteUser(FirebaseAuth mAuth){

        mAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful())
                    signUpWithEmail();

            }
        });

    }

    private void signUpWithEmail(){

        final FirebaseAuth mAuth = FirebaseAuth.getInstance();

        mAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {

                if(user != null)
                    pushUserData(user, mAuth);
                else
                    pushUserData(organization, mAuth);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                mProgressDialog.cancel();
                makeToast(e.getMessage());
                finish();

            }
        });

    }

    private void pushUserData(Object object, final FirebaseAuth mAuth){

        FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getUid()).setValue(object).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                mAuth.getCurrentUser().sendEmailVerification();
                mAuth.signOut();
                makeToast("Welcome to Annadata");
                finish();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                mProgressDialog.cancel();
                makeToast(e.getMessage());
                finish();

            }
        });

    }

    public void onClickResendCode(View view){

        mProgressDialog.setMessage("Resending Code");
        mProgressDialog.show();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mobileNumber,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks,
                token);

    }

}
