package com.android.navada.donit.activities;

import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.android.navada.donit.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class OrganizationRequirementActivity extends AppCompatActivity {

    private ListView listView;
    private ProgressBar progressBar;
    private Query query;
    private long readRequirementCount;
    private long requirementCount;
    private List<String> requirements;
    private ArrayAdapter<String> adapter;
    private ChildEventListener childEventListener;

    private void initialize(){

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressBar = findViewById(R.id.progressBar);
        listView = findViewById(R.id.requirementList);
        requirements = new ArrayList<>();
        readRequirementCount = 0;
        requirementCount = 0;
        query = FirebaseDatabase.getInstance().getReference().child("Requirements").child(getIntent().getStringExtra("id"));

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                requirements.add(dataSnapshot.getValue().toString());
                readRequirementCount++;

                if(readRequirementCount == requirementCount)
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
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_requirement);

        initialize();

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(requirements.size() == 0){

            progressBar.setVisibility(View.VISIBLE);
            listView.setVisibility(View.INVISIBLE);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    readRequirementCount = 0;
                    requirements = new ArrayList<>();
                    requirementCount = dataSnapshot.getChildrenCount();

                    if(requirementCount == 0)
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

    }

    @Override
    protected void onPause() {
        super.onPause();

        if(childEventListener != null)
            query.removeEventListener(childEventListener);

    }

    private void enableUserInteraction() {

        progressBar.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
        requirements.add(0, "dummy");
        adapter = new ArrayAdapter<>(OrganizationRequirementActivity.this, android.R.layout.simple_list_item_1, requirements);
        listView.setAdapter(adapter);

    }

    public void makeToast(String message){

        Toast.makeText(OrganizationRequirementActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        MainActivity.isRequirementActivity = false;

    }
}
