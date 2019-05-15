package com.android.navada.donit.fragments;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.android.navada.donit.adapters.MyDonationsAdapter;
import com.android.navada.donit.pojos.DonationItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MyDonationsFragment extends Fragment {

    private Query query;
    private ChildEventListener mChildEventListener;
    private long userDonationCount, readCount;
    private ProgressBar progressBar;
    private List<DonationItem> donations;
    private List<String> donationsPushIds;
    private RecyclerView mRecyclerView;
    private MyDonationsAdapter mAdapter;
    private android.support.v7.app.AlertDialog mAlertDialog;
    private android.support.v7.app.AlertDialog.Builder mBuilder;

    public MyDonationsFragment() {
        // Required empty public constructor
    }

    private void initialize(){

        donations = new ArrayList<>();
        donationsPushIds = new ArrayList<>();
        userDonationCount = readCount = 0;
        query = FirebaseDatabase.getInstance().getReference().child("Donations").orderByChild("donorId").equalTo(FirebaseAuth.getInstance().getUid());

        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                DonationItem donationDetails = dataSnapshot.getValue(DonationItem.class);

                if(!donationDetails.getStatus().equals("cancelled")) {
                    donations.add(donationDetails);
                    donationsPushIds.add(dataSnapshot.getKey());
                    readCount++;
                }

                if(donationDetails.getStatus().equals("cancelled"))
                    userDonationCount--;

                if(readCount == userDonationCount) {
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
        View view = inflater.inflate(R.layout.fragment_my_donations, container, false);
        progressBar = view.findViewById(R.id.progressBar);
        mRecyclerView = view.findViewById(R.id.donations_container);
        mAdapter = new MyDonationsAdapter(donations);
        mRecyclerView.setAdapter(mAdapter);

        return  view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(donations.size() == 0) {

            progressBar.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.INVISIBLE);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    readCount = 0;
                    donations.clear();
                    donationsPushIds.clear();

                    userDonationCount = dataSnapshot.getChildrenCount();

                    if (userDonationCount == 0)
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

        if(mChildEventListener != null)
            query.removeEventListener(mChildEventListener);


        if(mAlertDialog!=null)
            if(mAlertDialog.isShowing())
                mAlertDialog.cancel();

    }

    private void enableUserInteraction()
    {

        progressBar.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);

        buildRecyclerView();

    }

    private void buildRecyclerView(){

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mAdapter = new MyDonationsAdapter(donations);
        mRecyclerView.setAdapter(mAdapter);
        attachListenerToRecyclerView();

    }

    private void attachListenerToRecyclerView() {

        mAdapter.setOnItemClickListener(new MyDonationsAdapter.OnClickListener() {

            @Override
            public void onClick(final int position) {

                if(donations.get(position).getStatus().equals("Pending")){

                    mAlertDialog = mBuilder.setMessage("Do you want to cancel the donation?").
                            setTitle("Cancel Donation").
                            setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    FirebaseDatabase.getInstance().getReference().child("Donations")
                                            .child(donationsPushIds.get(position))
                                            .removeValue();
                                    donations.remove(position);
                                    donationsPushIds.remove(position);
                                    mAdapter.notifyItemRemoved(position);
                                    mRecyclerView.swapAdapter(mAdapter,true);

                                }
                            }).setNegativeButton("No",null).create();

                    mAlertDialog.show();

                }
            }

            @Override
            public void onClickImage(ImageView view, int position) {

                //Add code to enlarge the image

            }

        });

    }

    private void makeToast(String message){

        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();

    }

    private void sortList(){

        if(donations != null) {
            Collections.sort(donations, new Comparator<DonationItem>() {

                @Override
                public int compare(DonationItem o1, DonationItem o2) {
                    return o2.getTimeStamp().compareTo(o1.getTimeStamp());
                }

            });
        }

    }
}
