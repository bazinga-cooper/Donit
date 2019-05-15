package com.android.navada.donit.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.android.navada.donit.R;
import com.android.navada.donit.activities.MainActivity;
import com.android.navada.donit.pojos.DeliveryItem;
import com.android.navada.donit.pojos.Organization;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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

public class ProfileFragment extends Fragment {

    private TextView nameTextView;
    private TextView emailTextView;
    private TextView mobileNumberTextView;
    private TextView numberOfDonations;
    private TextView numberOfDeliveries;
    private TextView listTextView;
    private Spinner spinner;
    private Button button;
    private ProgressBar progressBar;
    private LinearLayout linearLayout;
    private boolean doneReadingDonations;
    private boolean doneReadingDeliveries;
    private boolean doneReadingOrgs;
    private Query donationQuery;
    private Query deliveryQuery;
    private ChildEventListener childEventListener;
    private ChildEventListener deliveryListener;
    private long readOrgCount;
    private long orgCount;
    private long deliveryCount;
    private long readDeliveryCount;
    private long deliveredCount;
    private Query orgQuery;
    private List<CharSequence> orgList;
    private HashMap<String, Object> user;
    private HashMap<String, String> userOrgs;
    private List<String> userOrgList;
    private ArrayAdapter<String> adapter;
    private String listText;

    private void initialize(){

        listText = "";
        userOrgList = new ArrayList<>();
        user = MainActivity.user;
        HashMap<Object, Object> object =(HashMap<Object, Object>) user.get("orgs");

        userOrgs = new HashMap<>();

        if(object != null)
        for(Map.Entry element : object.entrySet()) {

            String key = element.getKey().toString();
            String value = element.getValue().toString();

            userOrgs.put(key, value);
            if(value.equals("false"))
                userOrgList.add(key + " : " + "Not approved");
            else
                userOrgList.add(key + " : " + "Approved");
            listText += userOrgList.get(userOrgList.size()-1) + "\n";
        }

        doneReadingDonations = doneReadingDeliveries = doneReadingOrgs = false;
        donationQuery = FirebaseDatabase.getInstance().getReference().child("Donations").orderByChild("donorId").equalTo(FirebaseAuth.getInstance().getUid());
        deliveryQuery = FirebaseDatabase.getInstance().getReference().child("Deliveries").orderByChild("delivererId").equalTo(FirebaseAuth.getInstance().getUid());
        readOrgCount = orgCount = deliveryCount = readDeliveryCount = deliveredCount = 0;
        orgQuery = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("typeOfUser").equalTo("organization");
        orgList = new ArrayList<>();

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

        deliveryListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                DeliveryItem deliveryItem = dataSnapshot.getValue(DeliveryItem.class);
                if(deliveryItem.getStatus().equals("delivered"))
                    deliveredCount++;
                readDeliveryCount++;

                if(readDeliveryCount == deliveryCount){
                    doneReadingDeliveries = true;
                    numberOfDeliveries.setText(String.valueOf(deliveredCount));
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

    public ProfileFragment() {
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
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        linearLayout = view.findViewById(R.id.profileContainer);
        progressBar = view.findViewById(R.id.progressBar);
        nameTextView = view.findViewById(R.id.userName);
        emailTextView = view.findViewById(R.id.userEmail);
        mobileNumberTextView = view.findViewById(R.id.userMobileNumber);
        numberOfDonations = view.findViewById(R.id.number_of_donations);
        numberOfDeliveries = view.findViewById(R.id.number_of_deliveries);
        button = view.findViewById(R.id.submitButton);
        spinner = view.findViewById(R.id.organization_spinner);
        listTextView = view.findViewById(R.id.list);
        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nameTextView.setText(user.get("name").toString());
        emailTextView.setText(user.get("email").toString());
        mobileNumberTextView.setText(user.get("mobileNumber").toString());

        if(listText != null)
            listTextView.setText(listText);

    }

    @Override
    public void onResume() {

        super.onResume();

        linearLayout.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        doneReadingDonations = doneReadingDeliveries = doneReadingOrgs = false;

        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                if(doneReadingDonations && doneReadingDeliveries && doneReadingOrgs)
                    enableUserInteraction();
                else
                    handler.postDelayed(this, 1000);

            }
        };

        handler.postDelayed(runnable, 1000);

        donationQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                numberOfDonations.setText(String.valueOf(dataSnapshot.getChildrenCount()));
                doneReadingDonations = true;

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                makeToast(databaseError.getMessage());
                doneReadingDonations = true;

            }
        });

        deliveryQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                readDeliveryCount = deliveredCount = 0;
                deliveryCount = dataSnapshot.getChildrenCount();
                if(deliveryCount == 0) {
                    numberOfDeliveries.setText(String.valueOf(0));
                    doneReadingDeliveries = true;
                }
                else
                    deliveryQuery.addChildEventListener(deliveryListener);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                makeToast(databaseError.getMessage());
                doneReadingDeliveries = true;

            }

        });

        orgQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                readOrgCount = 0;
                orgCount = dataSnapshot.getChildrenCount();
                orgList = new ArrayList<>();
                orgList.add(0,"Select an organization");

                if(orgCount == 0)
                    buildSpinner();
                else
                    orgQuery.addChildEventListener(childEventListener);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                makeToast(databaseError.getMessage());
                doneReadingOrgs = true;

            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onClickSubmit();

            }
        });


    }

    private void buildSpinner(){

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, orgList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        doneReadingOrgs = true;

    }

    @Override
    public void onPause() {
        super.onPause();

        if(childEventListener != null)
            orgQuery.removeEventListener(childEventListener);

        if(deliveryListener != null)
            deliveryQuery.removeEventListener(deliveryListener);

        if(MainActivity.user.get("orgs") != null)
            MainActivity.user.put("orgs", userOrgs);

    }

    private void onClickSubmit(){

        String org = spinner.getSelectedItem().toString().trim();
        if(org.equals("Select an organization"))
            makeToast("Please select an organization");
        else if(userOrgs.get(org) != null)
            makeToast("Already a volunteer for this organization");
        else{
            userOrgs.put(org, "false");
            userOrgList.add(org + " : " + "Not approved");
            listText += (org + " : " + "Not approved" + "\n");
            listTextView.setText(listText);
            FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).child("orgs").setValue(userOrgs);
            makeToast("Success");
            if(userOrgList.size() == 1){
                FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).child("typeOfUser").setValue("volunteer").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){

                            makeToast("Congo! You are a volunteer now");
                            getActivity().moveTaskToBack(true);
                            android.os.Process.killProcess(android.os.Process.myPid());
                            System.exit(1);

                        }

                    }
                });
            }
        }

    }

    private void enableUserInteraction()
    {

        progressBar.setVisibility(View.GONE);
        linearLayout.setVisibility(View.VISIBLE);

    }

    public void makeToast(String message){

        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

}
