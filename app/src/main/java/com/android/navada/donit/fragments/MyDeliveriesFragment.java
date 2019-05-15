package com.android.navada.donit.fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.android.navada.donit.R;
import com.android.navada.donit.adapters.MyDeliveriesAdapter;
import com.android.navada.donit.pojos.DeliveryItem;
import com.android.navada.donit.pojos.Story;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import static android.app.Activity.RESULT_OK;

public class MyDeliveriesFragment extends Fragment {

    private Query query;
    private long deliveryCount,readDeliveryCount;
    private ProgressBar progressBar;
    private RecyclerView mRecyclerView;
    private List<DeliveryItem> deliveries;
    private List<String> deliveryIds;
    private Uri mImageUri;
    private AlertDialog mAlertDialog;
    private AlertDialog.Builder mBuilder;
    private StorageReference mDeliveryPhotoReference;
    private ChildEventListener mChildEventListener;
    private MyDeliveriesAdapter mAdapter;
    private ImageView mImageView;
    private ProgressDialog mProgressDialog;

    public MyDeliveriesFragment() {
        // Required empty public constructor
    }

    private void initialize(){

        deliveryCount = readDeliveryCount = 0;
        deliveries = new ArrayList<>();
        deliveryIds = new ArrayList<>();
        query = FirebaseDatabase.getInstance().getReference().child("Deliveries").orderByChild("delivererId").equalTo(FirebaseAuth.getInstance().getUid());
        mDeliveryPhotoReference = FirebaseStorage.getInstance().getReference().child("delivery_photos");

        mProgressDialog = new ProgressDialog(getContext());
        mProgressDialog.setCancelable(false);

        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                DeliveryItem deliveryItem = dataSnapshot.getValue(DeliveryItem.class);

                if(!deliveryItem.getStatus().equals("cancelled"))
                    deliveries.add(deliveryItem);

                if(deliveryItem.getStatus().equals("cancelled"))
                    deliveryCount--;

                deliveryIds.add(dataSnapshot.getKey());

                readDeliveryCount++;

                if(readDeliveryCount == deliveryCount) {
                    sortList();
                    enableUserInteraction();
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mBuilder = new android.support.v7.app.AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
        else
            mBuilder = new android.support.v7.app.AlertDialog.Builder(getContext());


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
        View view = inflater.inflate(R.layout.fragment_my_deliveries, container, false);
        progressBar = view.findViewById(R.id.progressBar);
        mRecyclerView = view.findViewById(R.id.deliveries_container);
        mAdapter = new MyDeliveriesAdapter(deliveries);
        mRecyclerView.setAdapter(mAdapter);

        return  view;
    }

    @Override
    public void onResume() {

        super.onResume();

        if(deliveries.size() == 0) {

            progressBar.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.INVISIBLE);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    readDeliveryCount = 0;
                    deliveries.clear();
                    deliveryIds.clear();
                    mImageUri = null;
                    mImageView = null;

                    deliveryCount = dataSnapshot.getChildrenCount();

                    Log.i("Hello", "onDataChange: " + deliveryCount);

                    if (deliveryCount == 0)
                        enableUserInteraction();
                    else
                        query.addChildEventListener(mChildEventListener);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                    makeToast(databaseError.getMessage());
                    enableUserInteraction();

                }
            });

        }

    }

    @Override
    public void onPause() {

        super.onPause();

        if(mChildEventListener!=null)
            query.removeEventListener(mChildEventListener);

        if(mAlertDialog!=null)
            if(mAlertDialog.isShowing())
                mAlertDialog.cancel();
    }

    private void buildRecyclerView(){

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mAdapter = new MyDeliveriesAdapter(deliveries);
        mRecyclerView.setAdapter(mAdapter);
        attachListenerToRecyclerView();

    }

    private void attachListenerToRecyclerView(){

        mAdapter.setOnItemClickListener(new MyDeliveriesAdapter.OnClickListener() {
            @Override
            public void onClick(final View view, final int position) {

                String status = deliveries.get(position).getStatus();

                if(status.equals("pending")) {

                    mAlertDialog = mBuilder.setMessage("What's the status of delivery?")
                            .setTitle("Status")
                            .setPositiveButton("DELIVERED", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    if(mImageUri == null)
                                        makeToast("Please upload the image");

                                    else {
                                        DeliveryItem mDeliveryDetails = deliveries.get(position);

                                        String nextStatus = "delivered";
                                        mDeliveryDetails.setStatus(nextStatus);

                                        Long timeStamp = System.currentTimeMillis() / 1000;

                                        DatabaseReference deliveryDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Deliveries").child(deliveryIds.get(position));
                                        deliveryDatabaseReference.child("status").setValue("delivered");
                                        deliveryDatabaseReference.child("timeStamp").setValue(String.valueOf(timeStamp));

                                        FirebaseDatabase.getInstance().getReference().child("Donations").child(mDeliveryDetails.getDonationId()).child("status").setValue("delivered");

                                        Uri uri = mImageUri;

                                        if (uri != null) {

                                            deliveries.get(position).setDeliveryImageURL(uri.toString());
                                            addImageToFireStorage(uri, position);
                                            mAdapter.notifyItemChanged(position);

                                        }
                                    }

                                }
                            })

                            .setNegativeButton("CANCEL DELIVERY", new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int which) {

                                    FirebaseDatabase.getInstance().getReference().child("Deliveries").child(deliveryIds.get(position)).removeValue();
                                    FirebaseDatabase.getInstance().getReference().child("Donations").child(deliveries.get(position).getDonationId()).child("status").setValue("Pending");
                                    deliveries.remove(position);
                                    deliveryIds.remove(position);
                                    mImageView = null;
                                    mImageUri = null;
                                    mAdapter.notifyItemRemoved(position);
                                    mRecyclerView.swapAdapter(mAdapter,true);

                                }
                            })
                            .create();

                    mAlertDialog.show();

                }
            }

            @Override
            public void onClickAddDeliveryImage(View view,final int position) {

                String status = deliveries.get(position).getStatus();

                if(status.equals("pending")) {

                    mImageView = view.findViewById(R.id.delivery_image);
                    Intent gallery = new Intent(Intent.ACTION_PICK);
                    gallery.setDataAndType(MediaStore.Images.Media.INTERNAL_CONTENT_URI, "image/*");
                    startActivityForResult(gallery, position);
                    mAdapter.notifyItemChanged(position);

                }

            }

            @Override
            public void onClickImage(ImageView view, int position) {

            }
        });

    }

    private void addImageToFireStorage(Uri imageUri, final int position){

        mProgressDialog.setMessage("Uploading Image...");
        mProgressDialog.show();
        final StorageReference photoRef =
                mDeliveryPhotoReference.child(deliveries.get(position).getDonationId() + ".jpeg");

        UploadTask uploadTask = photoRef.putFile(imageUri);

        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task){

                if (!task.isSuccessful()) {

                    makeToast("Image was not uploaded! Please Try Again");
                    if(mProgressDialog.isShowing())
                        mProgressDialog.dismiss();

                }

                if(mProgressDialog.isShowing())
                    mProgressDialog.dismiss();

                return photoRef.getDownloadUrl();
            }

        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {

                if (task.isSuccessful()) {

                    Uri downloadUri = task.getResult();
                    String imageUrl = downloadUri.toString();
                    FirebaseDatabase.getInstance().getReference().child("Deliveries").child(deliveryIds.get(position)).child("deliveryImageUrl").setValue(imageUrl);
                    FirebaseDatabase.getInstance().getReference().child("Donations").child(deliveries.get(position).getDonationId()).child("deliveryImageUrl").setValue(imageUrl);

                    DeliveryItem deliveryItem = deliveries.get(position);
                    Long timeStamp = System.currentTimeMillis() / 1000;
                    Story story = new Story(deliveryItem.getDelivererName(), "Donated by : " + deliveryItem.getDonorName() + "\n" + "Delivered to : " + deliveryItem.getOrgName(), imageUrl, String.valueOf(timeStamp));
                    FirebaseDatabase.getInstance().getReference().child("Stories").push().setValue(story);

                    if(mProgressDialog.isShowing())
                        mProgressDialog.dismiss();


                } else {

                    if(mProgressDialog.isShowing())
                        mProgressDialog.dismiss();
                    makeToast("Image was not uploaded! Please Try Again");

                }
            }
        });


    }

    private void enableUserInteraction()
    {

        mRecyclerView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        buildRecyclerView();

    }

    public void onActivityResult(final int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode < deliveries.size()) {

            mImageUri = data.getData();

            deliveries.get(requestCode).setDeliveryImageURL(mImageUri.toString());

            if(mImageView!=null)
                mImageView.setImageURI(mImageUri);

            mAdapter.notifyDataSetChanged();

        }

    }

    private void makeToast(String message){

        Toast.makeText(getContext(),message,Toast.LENGTH_SHORT).show();

    }

    private void sortList(){

        if(deliveries != null){

            Collections.sort(deliveries, new Comparator<DeliveryItem>() {
                @Override
                public int compare(DeliveryItem o1, DeliveryItem o2) {
                    return o2.getTimeStamp().compareTo(o1.getTimeStamp());
                }
            });
        }

    }

}
