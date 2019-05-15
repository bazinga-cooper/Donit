package com.android.navada.donit.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.android.navada.donit.R;
import com.android.navada.donit.activities.MainActivity;
import com.android.navada.donit.activities.OrganizationRequirementActivity;
import com.android.navada.donit.pojos.Organization;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;

public class RequirementsFragment extends Fragment {

    private ListView listView;
    private ProgressBar progressBar;
    private HashMap<String, String> orgs;
    private ArrayList<String> orgList;
    private Query query;
    private ChildEventListener childEventListener;
    private long readOrgCount;
    private long orgCount;
    private ArrayAdapter<String> adapter;

    private void initialize(){

        orgCount = readOrgCount = 0;
        orgList = new ArrayList<>();
        orgs = new HashMap<>();
        query = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("city").equalTo(MainActivity.user.get("city").toString());

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Organization organization = dataSnapshot.getValue(Organization.class);
                if(organization.isApproved()) {
                    orgList.add(organization.getName());
                    orgs.put(organization.getName(), dataSnapshot.getKey());
                }
                readOrgCount++;

                if(readOrgCount == orgCount)
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


    public RequirementsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initialize();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_requirements, container, false);
        listView = view.findViewById(R.id.orgList);
        progressBar = view.findViewById(R.id.progressBar);
        return view;

    }

    @Override
    public void onResume() {
        super.onResume();

        if(orgList.size() == 0){

            listView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    orgList = new ArrayList<>();
                    orgs = new HashMap<>();
                    readOrgCount = 0;
                    orgCount = dataSnapshot.getChildrenCount();
                    if(orgCount == 0)
                        enableUserInteraction();
                    else
                        query.addChildEventListener(childEventListener);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                    makeToast(databaseError.getMessage());

                }
            });

        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String selectedOrgId = orgs.get(orgList.get(position));
                MainActivity.isRequirementActivity = true;
                Intent intent  = new Intent(getActivity(), OrganizationRequirementActivity.class);
                intent.putExtra("id", selectedOrgId);
                startActivity(intent);

            }
        });

    }

    private void enableUserInteraction()
    {

        progressBar.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
        buildList();

    }

    private void buildList(){

        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, orgList);
        listView.setAdapter(adapter);

    }

    @Override
    public void onPause() {
        super.onPause();

        if(childEventListener != null)
            query.removeEventListener(childEventListener);

    }

    public void makeToast(String message){

        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }


}
