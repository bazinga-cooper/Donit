package com.android.navada.donit.fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.navada.donit.R;
import com.android.navada.donit.activities.DonationLocationActivity;
import com.android.navada.donit.activities.MainActivity;
import com.android.navada.donit.pojos.DonationItem;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import static android.app.Activity.RESULT_OK;

public class DonateFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private View mView;
    private ImageButton mItemImageButton, mLocationButton;
    private EditText mItemNameEditText, mItemDescriptionEditText, mPhoneNumberEditText;
    private Spinner mItemTypeSpinner;
    private Button mSubmitButton;
    private String mItemName, mItemDescription, mPhoneNumber, mItemType, mImageUrl,mName, mId;
    public static HashMap<String,Object> donorAddress;
    public static String state,addressText,city,pinCode;
    public static double latitude,longitude;
    private static final int PICK_IMAGE = 100;
    private Uri mImageUri;
    private DatabaseReference mDatabaseReference;
    private FirebaseStorage mFireBaseStorage;
    private StorageReference mPhotoStorageReference;
    private StorageReference mPhotoRef;
    private ProgressDialog mProgressDialog;
    private SharedPreferences mSharedPreferences;

    public DonateFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Donations");// make change in uid FirebaseAuth.getInstance().getUid()
        mFireBaseStorage = FirebaseStorage.getInstance();
        mPhotoStorageReference = mFireBaseStorage.getReference().child("donation_photos");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView =  inflater.inflate(R.layout.fragment_donate, container, false);
        mItemImageButton = mView.findViewById(R.id.donation_image_button);
        mLocationButton = mView.findViewById(R.id.location_button);
        mItemNameEditText = mView.findViewById(R.id.item_name);
        mItemDescriptionEditText = mView.findViewById(R.id.item_description);
        mPhoneNumberEditText = mView.findViewById(R.id.phone_number);
        mItemTypeSpinner = mView.findViewById(R.id.item_type_spinner);
        mSubmitButton = mView.findViewById(R.id.submit_button);
        mLocationButton = mView.findViewById(R.id.location_button);
        mProgressDialog = new ProgressDialog(getContext());
        mProgressDialog.setCancelable(false);
        mSharedPreferences = getContext().getSharedPreferences("com.android.navada.donit", Context.MODE_PRIVATE);

        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mImageUrl = "";
        mItemType = "";
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(getContext(), R.array.Donation_Item_Types,android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mItemTypeSpinner.setAdapter(arrayAdapter);
        mItemTypeSpinner.setOnItemSelectedListener(this);
        mItemImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
        mLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent  = new Intent(getContext(), DonationLocationActivity.class);
                startActivity(intent);
            }
        });


    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mItemType = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        mItemType = "";
    }

    private void openGallery(){
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        gallery.setType("image/*");
        startActivityForResult(gallery,PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            mImageUri = data.getData();
            mItemImageButton.setImageURI(mImageUri);
            mPhotoRef = mPhotoStorageReference.child(mImageUri.getLastPathSegment());
        }
    }

    public void addItem(){
        mItemName = mItemNameEditText.getText().toString();
        mItemDescription = mItemName + "\n" + mItemDescriptionEditText.getText().toString();
        mPhoneNumber = mPhoneNumberEditText.getText().toString();
        donorAddress = new HashMap<>();
        donorAddress.put("longitude",longitude);
        donorAddress.put("latitude",latitude);
        donorAddress.put("city",city);
        donorAddress.put("state",state);
        donorAddress.put("pincode",pinCode);
        donorAddress.put("address",addressText);
        if(isNotEmpty()){
            HashMap<String,Object> hm = MainActivity.user;
            mName = hm.get("name").toString();
            mId = "";
            Long timeStamp = System.currentTimeMillis()/1000;
            DonationItem donationItem =
                    new DonationItem(mItemDescription, mImageUrl,null,
                            mItemType,addressText,city,"Pending",timeStamp+"",
                            mName,FirebaseAuth.getInstance().getUid(), mPhoneNumber,null,
                            null,null);
            donationItem.setDonorAddress(donorAddress);
            donationItem.setChosenOrganizationId("none");
            donationItem.setDonorId(FirebaseAuth.getInstance().getUid());
            mDatabaseReference.push().setValue(donationItem);
            mItemNameEditText.setText("");
            mItemDescriptionEditText.setText("");
            mPhoneNumberEditText.setText("");
            mItemImageButton.setImageResource(R.drawable.add_image_click);
        }
    }

    public boolean isNotEmpty(){
        if(mItemName.isEmpty() || mItemDescription.isEmpty() || mPhoneNumber.isEmpty() ||
                donorAddress.isEmpty() || mImageUrl.isEmpty() || mItemType.isEmpty()){
            makeToast("Fields Can't Be Empty !");
            return  false;
        }
        return  true;
    }

    private void uploadImage(){
        mProgressDialog.setMessage("Uploading Image...");
        mProgressDialog.show();

        UploadTask uploadTask = mPhotoRef.putFile(mImageUri);
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    makeToast("unable to upload image");
                    if(mProgressDialog.isShowing())
                        mProgressDialog.cancel();
                    throw task.getException();
                }

                return mPhotoRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    mImageUrl = downloadUri.toString();
                    makeToast("Image Uploaded");
                    if(mProgressDialog.isShowing())
                        mProgressDialog.cancel();
                    addItem();
                } else {
                    if(mProgressDialog.isShowing())
                        mProgressDialog.cancel();
                    makeToast("Image not uploaded");
                }
            }
        });

    }

    public void makeToast(String s){
        Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
    }

}
