package com.android.navada.donit.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import com.android.navada.donit.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import java.util.concurrent.TimeUnit;

public class OrganisationSignUpActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText nameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText mobileNumberEditText;
    private Spinner spinner;
    private EditText causeEditText;
    private EditText addressEditText;
    private Intent intent;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    public static PhoneAuthProvider.ForceResendingToken token;
    public ProgressDialog mProgressDialog;
    private String name;
    private String email;
    private String password;
    private String mobileNumber;
    private String cause;
    private String type;

    //Update these once you get the address
    public static String address = "";
    public static String city = "";
    public static Double latitude,longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organisation_sign_up);

        initialize();

    }

    private void initialize(){

        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        mobileNumberEditText = findViewById(R.id.mobileNumberEditText);
        causeEditText = findViewById(R.id.causeEditText);
        addressEditText = findViewById(R.id.addressEditText);
        spinner = findViewById(R.id.spinner);
        mProgressDialog = new ProgressDialog(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.orgs,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

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
                askUserToEnterCode(verificationId, forceResendingToken);

            }

            @Override
            public void onCodeAutoRetrievalTimeOut(String s) {
                super.onCodeAutoRetrievalTimeOut(s);
            }

        };

    }

    private void askUserToEnterCode(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken){

        mProgressDialog.cancel();
        token = forceResendingToken;
        intent = new Intent(OrganisationSignUpActivity.this, VerificationActivity.class);
        intent.putExtra("name", name);
        intent.putExtra("email", email);
        intent.putExtra("password", password);
        intent.putExtra("city", city);
        intent.putExtra("cause", cause);
        intent.putExtra("address", address);
        intent.putExtra("type", type);
        intent.putExtra("mobileNumber", mobileNumber);
        intent.putExtra("userType","organization");
        intent.putExtra("verificationId",verificationId);
        intent.putExtra("latitude",latitude);
        intent.putExtra("longitude",longitude);
        startActivity(intent);

    }


    private void makeToast(String message){

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);

    }

    public void onClickSubmitButton(View view){

        name = nameEditText.getText().toString().trim();
        email = emailEditText.getText().toString().trim();
        password = passwordEditText.getText().toString();
        mobileNumber = mobileNumberEditText.getText().toString().trim();
        cause = causeEditText.getText().toString().trim();
        type = spinner.getSelectedItem().toString();
        address = addressEditText.getText().toString().trim();

        if(name.isEmpty() || email.isEmpty() || password.isEmpty() || mobileNumber.isEmpty() || cause.isEmpty())
            makeToast("Fields cannot be empty!");
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
            makeToast("Enter a valid email!");
        else if(password.length()<6)
            makeToast("Password should be minimum of 6 characters!");
        else if(type.equals("Select a type"))
            makeToast("Please select your organization type");
        else if(city.isEmpty() || address.isEmpty())
            makeToast("Please locate your address on map!");
        else{
            PhoneNumberUtil mPhoneNumberUtil = PhoneNumberUtil.getInstance();
            try {
                Phonenumber.PhoneNumber mPhoneNumber = mPhoneNumberUtil.parse(mobileNumber,"IN");
                if(mPhoneNumberUtil.isValidNumber(mPhoneNumber))
                {
                    mobileNumber = mPhoneNumberUtil.format(mPhoneNumber,PhoneNumberUtil.PhoneNumberFormat.E164);

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            mobileNumber,
                            60,
                            TimeUnit.SECONDS,
                            OrganisationSignUpActivity.this,
                            mCallbacks);
                    mProgressDialog.setMessage("Sending Verification Code...");
                    mProgressDialog.show();
                }
                else
                    makeToast("Please enter a valid mobile number!");
            } catch (NumberParseException e) {
                makeToast(e.getMessage());
            }
        }

    }

    @Override
    public void onClick(View v) {

    }
    public void getLocation(){
        Intent i = new Intent(this, OrganizationLocationActivity.class);
        startActivity(i);
    }
    @Override
    protected void onResume() {
        super.onResume();
        ImageButton ib = findViewById(R.id.location_button);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();
            }
        });
    }


}
