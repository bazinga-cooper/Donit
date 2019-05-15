package com.android.navada.donit.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.android.navada.donit.R;
import com.android.navada.donit.activities.MainActivity;
import com.android.navada.donit.pojos.Event;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import static android.app.Activity.RESULT_OK;

public class AddEventFragment extends Fragment {
    
    private EditText eventNameEditText;
    private EditText eventDescriptionEditText;
    private ImageButton eventImageButton;
    private Button addEventButton;
    private String eventName;
    private String eventDescription;
    private StorageReference mStorageReference;
    private Uri imageUri;
    private static final int PICK_IMAGE = 100;
    private StorageReference photoRef;
    private ProgressDialog mProgressDialog;


    public AddEventFragment() {
        // Required empty public constructor
    }
    
    private void initialize(){

        mProgressDialog = new ProgressDialog(getContext());
        mProgressDialog.setCancelable(false);
        mStorageReference = FirebaseStorage.getInstance().getReference().child("notification_photos");
        
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
      
        initialize();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_event, container, false);
        eventNameEditText = view.findViewById(R.id.event_name);
        eventDescriptionEditText = view.findViewById(R.id.event_description);
        eventImageButton = view.findViewById(R.id.event_image);
        addEventButton = view.findViewById(R.id.add_event_button);
        return view;
    }

    @Override
    public void onResume() {
        
        super.onResume();

        addEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addEvent();
            }
        });

        eventImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        
    }

    public void selectImage(){

        Intent gallery = new Intent(Intent.ACTION_PICK);
        gallery.setDataAndType(MediaStore.Images.Media.INTERNAL_CONTENT_URI, "image/jpeg");
        startActivityForResult(gallery, PICK_IMAGE);

    }



    public void addEvent(){
        
        eventName = eventNameEditText.getText().toString().trim();
        eventDescription = eventDescriptionEditText.getText().toString().trim();
    
        
        if (eventName.isEmpty() || eventDescription.isEmpty()) 
            makeToast("Fields cannot be empty!!");
        
        else if(imageUri == null)
            makeToast("Please upload an image!");
        
        else{
            
            mProgressDialog.setMessage("Uploading Data...");
            mProgressDialog.show();
            
            UploadTask uploadTask = photoRef.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task){
                    
                    if (!task.isSuccessful()) {
                        if(mProgressDialog.isShowing())
                            mProgressDialog.dismiss();
                        Toast.makeText(getContext(), "Unable to upload data!Please try again", Toast.LENGTH_SHORT).show();
                    }
                    
                    if(mProgressDialog.isShowing())
                        mProgressDialog.cancel();
                    
                    return photoRef.getDownloadUrl();
                }
                
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    
                    if (task.isSuccessful()) {
                        
                        Uri downloadUri = task.getResult();
                        String imageUrl = downloadUri.toString();
                        
                        addToDatabase(imageUrl);
                        
                        if(mProgressDialog.isShowing())
                            mProgressDialog.dismiss();
                        
                    } else {
                        
                        if(mProgressDialog.isShowing())
                            mProgressDialog.dismiss();
                        Toast.makeText(getContext(), "Unable to upload data!Please try again", Toast.LENGTH_SHORT).show();
                        
                    }
                }
            });
        }

    }

    public void addToDatabase(String imageUrl){

        Long timeStamp = System.currentTimeMillis() / 1000;
        Event event = new Event(eventName, eventDescription, imageUrl, timeStamp, MainActivity.user.get("name").toString());
        FirebaseDatabase.getInstance().getReference().child("Events").push().setValue(event);
        makeToast("Success!!");
        eventNameEditText.setText("");
        eventDescriptionEditText.setText("");
        eventImageButton.setImageResource(R.drawable.add_image_click);
        
    }

    public void makeToast(String message){
        
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        
        super.onActivityResult(requestCode, resultCode, data);
        
        if(resultCode == RESULT_OK && requestCode == PICK_IMAGE){

            imageUri = data.getData();
            
            if(imageUri != null) {
                eventImageButton.setImageURI(imageUri);
                photoRef = mStorageReference.child(imageUri.getLastPathSegment());
            }

        }
        
    }

}
