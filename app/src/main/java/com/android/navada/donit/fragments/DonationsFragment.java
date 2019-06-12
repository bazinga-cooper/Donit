package com.android.navada.donit.fragments;


import android.app.ProgressDialog;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.navada.donit.R;
import com.android.navada.donit.activities.MainActivity;
import com.android.navada.donit.adapters.DonationsAdapter;
import com.android.navada.donit.pojos.DonationItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class DonationsFragment extends Fragment {
    private DatabaseReference mDonorSpotDatabaseReference;
    private ChildEventListener mDonorSpotChildEventListener;
    private View mView;
    private ArrayList<DonationItem> mDonationItems;
    private ArrayList<String> mDonationIds;
    private long mDonationCount =0, mReadDonationCount =0;
    private boolean mDoneReadingDonations;
    private ProgressBar mProgressBar;
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

        mDonationItems = new ArrayList<>();
        mReadDonationCount = mDonationCount = 0;
        mDonationIds = new ArrayList<>();

        super.onCreate(savedInstanceState);
        mDonorSpotDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Donations");
        mDonorSpotChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                ObjectMapper objectMapper = new ObjectMapper();
                HashMap<String,Object> data = (HashMap<String,Object>) dataSnapshot.getValue();
                DonationItem donationItem = objectMapper.convertValue(data, DonationItem.class);
                if(donationItem.getStatus().equals("Pending")&&donationItem.getChosenOrganizationId().equals("none")){
                    mDonationItems.add(donationItem);
                    mDonationIds.add(dataSnapshot.getKey());
                    }
                    mReadDonationCount++;


                if (mReadDonationCount == mDonationCount)
                    mDoneReadingDonations = true;
                if (mDoneReadingDonations)
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
        mView = inflater.inflate(R.layout.fragment_donations, container, false);
        mProgressBar = mView.findViewById(R.id.progressBar);
        mRecyclerView = mView.findViewById(R.id.donations_container);
        mAdapter = new DonationsAdapter(mDonationItems);
        mRecyclerView.setAdapter(mAdapter);
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mDonationItems = new ArrayList<>();
        mDonationIds = new ArrayList<>();
        mProgressDialog = new ProgressDialog(getContext());
        mProgressDialog.setCancelable(false);
        mProgressBar.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);

        mDonorSpotDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mReadDonationCount = 0;

                mDonationCount = dataSnapshot.getChildrenCount();

                mDonationIds.clear();
                mDonationItems.clear();

                if(mDonationCount !=0)
                    mDonorSpotDatabaseReference.addChildEventListener(mDonorSpotChildEventListener);
                else
                    mDoneReadingDonations = true;

                if(mDoneReadingDonations)
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
        if(mDonorSpotChildEventListener !=null)
            mDonorSpotDatabaseReference.removeEventListener(mDonorSpotChildEventListener);
    }
    public void enableUserInteraction(){
        mRecyclerView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);
        buildRecyclerView();

    }

    private void buildRecyclerView(){

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mAdapter = new DonationsAdapter(mDonationItems);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new DonationsAdapter.OnClickListener() {
            @Override
            public void onClick(int position) {
                DonationItem donationItem = mDonationItems.get(position);
                donationItem.setChosenOrganizationId(MainActivity.user.get("name").toString());
                mDonorSpotDatabaseReference.child(mDonationIds.get(position)).child("chosenOrganizationId").setValue(MainActivity.user.get("name"));
                mDonorSpotDatabaseReference.child(mDonationIds.get(position)).child("orgLat").setValue(MainActivity.user.get("latitude"));
                mDonorSpotDatabaseReference.child(mDonationIds.get(position)).child("orgLng").setValue(MainActivity.user.get("longitude"));
                mDonationItems.remove(donationItem);
                mAdapter.notifyItemRemoved(position);
                mRecyclerView.swapAdapter(mAdapter,true);
                Toast.makeText(getContext(), "You have chosen the Donation", Toast.LENGTH_SHORT).show();
            }
        });

    }

}
