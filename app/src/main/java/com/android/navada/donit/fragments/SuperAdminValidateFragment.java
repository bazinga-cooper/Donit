package com.android.navada.donit.fragments;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.android.navada.donit.R;
import com.android.navada.donit.adapters.OrganizationAdapter;
import com.android.navada.donit.pojos.Organization;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class SuperAdminValidateFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private Query query;
    private OrganizationAdapter mAdapter;
    private List<Organization> organizations;
    private List<String>  orgIds;
    private long readCount;
    private long organizationCount;
    private ChildEventListener childEventListener;
    private AlertDialog mAlertDialog;
    private AlertDialog.Builder mBuilder;

    private void initialize(){

        query = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("approved").equalTo(false);
        organizations = new ArrayList<>();
        orgIds = new ArrayList<>();
        readCount = organizationCount = 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mBuilder = new android.support.v7.app.AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
        else
            mBuilder = new android.support.v7.app.AlertDialog.Builder(getContext());

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                organizations.add(dataSnapshot.getValue(Organization.class));
                orgIds.add(dataSnapshot.getKey());

                readCount++;

                if(readCount == organizationCount) {
                    mAdapter = new OrganizationAdapter(organizations);
                    recyclerView.setAdapter(mAdapter);
                    attachListenerToRecyclerView();
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

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initialize();

    }

    public SuperAdminValidateFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_super_admin_validate, container, false);
        recyclerView = view.findViewById(R.id.organization_container);
        buildRecyclerView();
        progressBar = view.findViewById(R.id.progressBar);
        return view;

    }

    @Override
    public void onResume() {
        super.onResume();

        recyclerView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                readCount = 0;
                organizations = new ArrayList<>();
                orgIds = new ArrayList<>();
                organizationCount = dataSnapshot.getChildrenCount();
                if(organizationCount == 0)
                    enableUserInteraction();
                else
                    query.addChildEventListener(childEventListener);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                makeToast(databaseError.getMessage());
                enableUserInteraction();

            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();

        if(childEventListener != null)
            query.removeEventListener(childEventListener);

    }

    private void enableUserInteraction(){

        recyclerView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);

    }

    private void buildRecyclerView(){

        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mAdapter = new OrganizationAdapter(organizations);
        recyclerView.setAdapter(mAdapter);

    }

    private void attachListenerToRecyclerView(){

        mAdapter.setOnClickListener(new OrganizationAdapter.OnClickListener() {
            @Override
            public void onClick(View view, final int position) {

                mAlertDialog = mBuilder.setMessage("Approve organization?")
                        .setTitle("Validate")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                FirebaseDatabase.getInstance().getReference().child("Users").
                                        child(orgIds.get(position)).child("approved").setValue(true);
                                orgIds.remove(position);
                                organizations.remove(position);
                                mAdapter.notifyItemRemoved(position);
                                recyclerView.swapAdapter(mAdapter,true);

                            }
                        }).create();

                mAlertDialog.show();

            }
        });

    }


    public void makeToast(String message){

        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

}
