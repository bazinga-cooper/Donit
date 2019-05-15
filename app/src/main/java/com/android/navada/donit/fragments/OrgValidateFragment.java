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
import com.android.navada.donit.activities.MainActivity;
import com.android.navada.donit.adapters.VolunteerAdapter;
import com.android.navada.donit.pojos.User;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrgValidateFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private Query query;
    private String city;
    private VolunteerAdapter mAdapter;
    private List<User> volunteers;
    private List<String>  userIds;
    private long readCount;
    private long volunteerCount;
    private ChildEventListener childEventListener;
    private AlertDialog mAlertDialog;
    private AlertDialog.Builder mBuilder;

    private void initialize(){

        city = MainActivity.user.get("city").toString();
        query = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("city").equalTo(city);
        volunteers = new ArrayList<>();
        userIds = new ArrayList<>();
        readCount = volunteerCount = 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mBuilder = new android.support.v7.app.AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
        else
            mBuilder = new android.support.v7.app.AlertDialog.Builder(getContext());

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                User user = dataSnapshot.getValue(User.class);
                HashMap<String, String> orgs = user.getOrgs();
                if(orgs != null)
                    for(Map.Entry item : orgs.entrySet()){

                        String key = item.getKey().toString();
                        String value = item.getValue().toString();

                        if(key.equals(MainActivity.user.get("name").toString()) && value.equals("false")) {
                            volunteers.add(user);
                            userIds.add(dataSnapshot.getKey());
                        }

                    }

                readCount++;

                if(readCount == volunteerCount) {
                    mAdapter = new VolunteerAdapter(volunteers);
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

    public OrgValidateFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View view = inflater.inflate(R.layout.fragment_org_validate, container, false);
       recyclerView = view.findViewById(R.id.volunteer_container);
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
                volunteers = new ArrayList<>();
                userIds = new ArrayList<>();
                volunteerCount = dataSnapshot.getChildrenCount();
                if(volunteerCount == 0)
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
        mAdapter = new VolunteerAdapter(volunteers);
        recyclerView.setAdapter(mAdapter);

    }

    private void attachListenerToRecyclerView(){

        mAdapter.setOnClickListener(new VolunteerAdapter.OnClickListener() {
            @Override
            public void onClick(View view, final int position) {

                mAlertDialog = mBuilder.setMessage("Approve user to become a volunteer?")
                        .setTitle("Validate")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                               FirebaseDatabase.getInstance().getReference().child("Users").
                                       child(userIds.get(position)).child("orgs").
                                       child(MainActivity.user.get("name").toString()).setValue("true");
                               userIds.remove(position);
                               volunteers.remove(position);
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
