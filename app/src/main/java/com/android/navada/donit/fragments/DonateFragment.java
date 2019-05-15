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

    private View view;
    private ImageButton itemImageButton,locationButton;
    private EditText itemNameEditText,itemDescriptionEditText,phoneNumberEditText;
    private Spinner itemTypeSpinner;
    private Button submitButton;
    private String itemName,itemDescription,phoneNumber,itemType,imageUrl,name,id;
    public static HashMap<String,Object> donorAddress;
    public static String state,addressText,city,pinCode;
    public static double latitude,longitude;
    private static final int PICK_IMAGE = 100;
    private Uri imageUri;
    private DatabaseReference databaseReference;
    private FirebaseStorage fireBaseStorage;
    private StorageReference photoStorageReference;
    private StorageReference photoRef;
    private ProgressDialog progressDialog;
    private SharedPreferences sharedPreferences;

    public DonateFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Donations");// make change in uid FirebaseAuth.getInstance().getUid()
        fireBaseStorage = FirebaseStorage.getInstance();
        photoStorageReference = fireBaseStorage.getReference().child("donation_photos");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_donate, container, false);
        itemImageButton = view.findViewById(R.id.donation_image_button);
        locationButton = view.findViewById(R.id.location_button);
        itemNameEditText = view.findViewById(R.id.item_name);
        itemDescriptionEditText = view.findViewById(R.id.item_description);
        phoneNumberEditText = view.findViewById(R.id.phone_number);
        itemTypeSpinner = view.findViewById(R.id.item_type_spinner);
        submitButton = view.findViewById(R.id.submit_button);
        locationButton = view.findViewById(R.id.location_button);
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
        sharedPreferences = getContext().getSharedPreferences("com.android.navada.donit", Context.MODE_PRIVATE);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        imageUrl = "";
        itemType = "";
        ArrayAdapter<CharSequence> aa = ArrayAdapter.createFromResource(getContext(), R.array.Donation_Item_Types,android.R.layout.simple_spinner_item);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        itemTypeSpinner.setAdapter(aa);
        itemTypeSpinner.setOnItemSelectedListener(this);
        itemImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent  = new Intent(getContext(), DonationLocationActivity.class);
                startActivity(intent);
            }
        });


    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        itemType = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        itemType = "";
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
            imageUri = data.getData();
            itemImageButton.setImageURI(imageUri);
            photoRef = photoStorageReference.child(imageUri.getLastPathSegment());
        }
    }

    public void addItem(){
        itemName = itemNameEditText.getText().toString();
        itemDescription = itemName + "\n" + itemDescriptionEditText.getText().toString();
        phoneNumber = phoneNumberEditText.getText().toString();
        donorAddress = new HashMap<>();
        donorAddress.put("longitude",longitude);
        donorAddress.put("latitude",latitude);
        donorAddress.put("city",city);
        donorAddress.put("state",state);
        donorAddress.put("pincode",pinCode);
        donorAddress.put("address",addressText);
        if(isNotEmpty()){
            HashMap<String,Object> hm = MainActivity.user;
            name = hm.get("name").toString();
            id = "";
            Long timeStamp = System.currentTimeMillis()/1000;
            DonationItem donationItem = new DonationItem(itemDescription,imageUrl,null,itemType,addressText,city,"Pending",timeStamp+"",name,FirebaseAuth.getInstance().getUid(),phoneNumber,null,null,null);
            donationItem.setDonorAddress(donorAddress);
            donationItem.setChosenOrganizationId("none");
            donationItem.setDonorId(FirebaseAuth.getInstance().getUid());
            databaseReference.push().setValue(donationItem);
            itemNameEditText.setText("");
            itemDescriptionEditText.setText("");
            phoneNumberEditText.setText("");
            itemImageButton.setImageResource(R.drawable.add_image_click);
        }
    }

    public boolean isNotEmpty(){
        if(itemName.isEmpty() || itemDescription.isEmpty() || phoneNumber.isEmpty() || donorAddress.isEmpty() || imageUrl.isEmpty() || itemType.isEmpty()){
            makeToast("Fields Can't Be Empty !");
            return  false;
        }
        return  true;
    }

    private void uploadImage(){
        progressDialog.setMessage("Uploading Image...");
        progressDialog.show();

        UploadTask uploadTask = photoRef.putFile(imageUri);
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    makeToast("unable to upload image");
                    if(progressDialog.isShowing())
                        progressDialog.cancel();
                    throw task.getException();
                }

                return photoRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    imageUrl = downloadUri.toString();
                    makeToast("Image Uploaded");
                    if(progressDialog.isShowing())
                        progressDialog.cancel();
                    addItem();
                } else {
                    if(progressDialog.isShowing())
                        progressDialog.cancel();
                    makeToast("Image not uploaded");
                }
            }
        });

    }

    public void makeToast(String s){
        Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
    }

}
