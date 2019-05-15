package com.android.navada.donit.threads;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import com.android.navada.donit.activities.DonorSignUpActivity;
import com.android.navada.donit.pojos.Organization;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class GetOrganizationList extends Thread {

    private DonorSignUpActivity activity;
    private Spinner spinner;
    private ProgressDialog progressDialog;
    private ChildEventListener childEventListener;
    private long readOrgCount;
    private long orgCount;
    private Query query;
    private List<CharSequence> orgList;

    public  GetOrganizationList(DonorSignUpActivity activity){

        this.activity = activity;
        spinner = activity.organizationSpinner;
        progressDialog = activity.mProgressDialog;
        readOrgCount = orgCount = 0;
        query = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("typeOfUser").equalTo("organization");
        orgList = new ArrayList<>();
    }

    @Override
    public void run() {

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Organization organization = dataSnapshot.getValue(Organization.class);
                if(organization.isApproved())
                    orgList.add(organization.getName());
                readOrgCount++;

                if(readOrgCount == orgCount)
                    buildSpinner();

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

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                readOrgCount = 0;
                orgCount = dataSnapshot.getChildrenCount();
                orgList = new ArrayList<>();
                orgList.add(0,"Select an organization");

                if(orgCount == 0)
                    buildSpinner();
                else
                    query.addChildEventListener(childEventListener);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                progressDialog.cancel();
                makeToast(databaseError.getMessage());

            }
        });


    }

    private void buildSpinner(){

        removeListener();
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, orgList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        progressDialog.cancel();

    }

    private void removeListener(){

        if(childEventListener != null)
            query.removeEventListener(childEventListener);

    }

    private void makeToast(String message){

        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();

    }

}
