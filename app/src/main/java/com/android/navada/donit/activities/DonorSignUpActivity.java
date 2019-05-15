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
import android.widget.Spinner;
import android.widget.Toast;
import com.android.navada.donit.R;
import com.android.navada.donit.threads.GetOrganizationList;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import java.util.concurrent.TimeUnit;

public class DonorSignUpActivity extends AppCompatActivity {

    private EditText nameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText mobileNumberEditText;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    public ProgressDialog mProgressDialog;
    public static PhoneAuthProvider.ForceResendingToken token;
    private String name;
    private String email;
    private String password;
    private String mobileNumber;
    private String city;
    private String organization;
    private Spinner citySpinner;
    public Spinner organizationSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_sign_up);
        
        initialize();
        
    }
    
    private void initialize(){

        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        mobileNumberEditText = findViewById(R.id.mobileNumberEditText);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        citySpinner = findViewById(R.id.city_spinner);
        ArrayAdapter<CharSequence> citySpinnerAdapter = ArrayAdapter.createFromResource(this,R.array.cities,android.R.layout.simple_spinner_item);
        citySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setAdapter(citySpinnerAdapter);

        organizationSpinner  = findViewById(R.id.organization_spinner);

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
        Intent intent = new Intent(DonorSignUpActivity.this, VerificationActivity.class);
        intent.putExtra("name", name);
        intent.putExtra("email", email);
        intent.putExtra("password", password);
        intent.putExtra("city", city);
        if(organization.equals("Select an organization")) {
            organization = "none";
            intent.putExtra("userType","donor");
        }
        else
            intent.putExtra("userType","volunteer");
        intent.putExtra("organization", organization);
        intent.putExtra("mobileNumber", mobileNumber);
        intent.putExtra("verificationId",verificationId);
        startActivity(intent);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void makeToast(String message){

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

    }

    public void onClickSubmitButton(View view){

        name =nameEditText.getText().toString().trim();
        email = emailEditText.getText().toString().trim();
        password = passwordEditText.getText().toString();
        mobileNumber = mobileNumberEditText.getText().toString().trim();
        city = citySpinner.getSelectedItem().toString();
        organization = organizationSpinner.getSelectedItem().toString();

        if(name.isEmpty() || email.isEmpty() || password.isEmpty() || mobileNumber.isEmpty())
            makeToast("Fields cannot be empty!");
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
            makeToast("Enter a valid email!");
        else if(password.length()<6)
            makeToast("Password should be minimum of 6 characters!");
        else if(city.equals("Select your city"))
            makeToast("Please select a city");
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
                            DonorSignUpActivity.this,
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
    protected void onResume() {
        super.onResume();

        mProgressDialog.setMessage("Please wait..");
        mProgressDialog.show();
        GetOrganizationList thread = new GetOrganizationList(this);
        thread.start();

    }
}
