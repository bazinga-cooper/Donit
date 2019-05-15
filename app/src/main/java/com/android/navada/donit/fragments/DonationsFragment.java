package com.android.navada.donit.fragments;


import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
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

import com.android.navada.donit.R;
import com.android.navada.donit.activities.MainActivity;
import com.android.navada.donit.adapters.DonationsAdapter;
import com.android.navada.donit.adapters.MyDeliveriesAdapter;
import com.android.navada.donit.pojos.DeliveryItem;
import com.android.navada.donit.pojos.DonationItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DonationsFragment extends Fragment {
    private DatabaseReference donorSpotDatabaseReference;
    private ChildEventListener donorSpotChildEventListener;
    private View view;
    private ArrayList<DonationItem> donationItems;
    private ArrayList<String> donationIds;
    long donationCount=0,readDonationCount=0;
    boolean doneReadingDonations;
    private ProgressBar progressBar;
    private RecyclerView mRecyclerView;
    private AlertDialog mAlertDialog;
    private AlertDialog.Builder mBuilder;
    private ChildEventListener mChildEventListener;
    private DonationsAdapter mAdapter;
    private ProgressDialog mProgressDialog;



    public DonationsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        donationItems = new ArrayList<>();
        readDonationCount = donationCount = 0;
        donationIds = new ArrayList<>();

        super.onCreate(savedInstanceState);
        donorSpotDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Donations");
        donorSpotChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                ObjectMapper objectMapper = new ObjectMapper();
                HashMap<String,Object> data = (HashMap<String,Object>) dataSnapshot.getValue();

                Log.i("Hello", "onChildAdded: " + dataSnapshot.getValue());
                    DonationItem donationItem = objectMapper.convertValue(data, DonationItem.class);
                    if(donationItem.getStatus().equals("Pending")&&donationItem.getChosenOrganizationId().equals("none")){
                        donationItems.add(donationItem);
                        donationIds.add(dataSnapshot.getKey());
                        Log.d("hi",donationItem.toString());
                    }
                    readDonationCount++;


                if (readDonationCount == donationCount)
                    doneReadingDonations = true;
                if (doneReadingDonations)
                    enableUserInteraction();


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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         view = inflater.inflate(R.layout.fragment_donations, container, false);
        progressBar = view.findViewById(R.id.progressBar);
        mRecyclerView = view.findViewById(R.id.donations_container);
        mAdapter = new DonationsAdapter(donationItems);
        mRecyclerView.setAdapter(mAdapter);
         return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        donationItems = new ArrayList<>();
        donationIds = new ArrayList<>();
        mProgressDialog = new ProgressDialog(getContext());
        mProgressDialog.setCancelable(false);
            progressBar.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.INVISIBLE);

        donorSpotDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                readDonationCount = 0;

                donationCount = dataSnapshot.getChildrenCount();

                donationIds.clear();
                donationItems.clear();

                if(donationCount!=0)
                    donorSpotDatabaseReference.addChildEventListener(donorSpotChildEventListener);
                else
                    doneReadingDonations = true;

                if(doneReadingDonations)
                    enableUserInteraction();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    @Override
    public void onPause() {
        super.onPause();
        if(donorSpotChildEventListener!=null)
            donorSpotDatabaseReference.removeEventListener(donorSpotChildEventListener);
    }
    public void enableUserInteraction(){
        mRecyclerView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        buildRecyclerView();

    }

    private void buildRecyclerView(){

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mAdapter = new DonationsAdapter(donationItems);
        mRecyclerView.setAdapter(mAdapter);
        Log.d("donations",""+donationItems);
        mAdapter.setOnItemClickListener(new DonationsAdapter.OnClickListener() {
            @Override
            public void onClick(int position) {
                    DonationItem di = donationItems.get(position);
                    di.setChosenOrganizationId(MainActivity.user.get("name").toString());
                    donorSpotDatabaseReference.child(donationIds.get(position)).child("chosenOrganizationId").setValue(MainActivity.user.get("name"));
                donorSpotDatabaseReference.child(donationIds.get(position)).child("orgLat").setValue(MainActivity.user.get("latitude"));
                donorSpotDatabaseReference.child(donationIds.get(position)).child("orgLng").setValue(MainActivity.user.get("longitude"));
                donationItems.remove(di);
                mAdapter.notifyItemRemoved(position);
                mRecyclerView.swapAdapter(mAdapter,true);
            }
        });

    }

}
