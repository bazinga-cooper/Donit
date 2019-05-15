package com.android.navada.donit.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.android.navada.donit.R;
import com.android.navada.donit.adapters.StoriesAdapter;
import com.android.navada.donit.pojos.DeliveryItem;
import com.android.navada.donit.pojos.Event;
import com.android.navada.donit.pojos.Story;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StoriesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private long readEventCount;
    private long readDeliveryCount;
    private long eventCount;
    private long deliveryCount;
    private List<DeliveryItem> deliveries;
    private List<Event> events;
    private List<Story> stories;
    private Query deliveryQuery;
    private DatabaseReference eventDatabaseReference;
    private ChildEventListener deliveryListener;
    private ChildEventListener eventListener;
    private boolean doneReadingDeliveries;
    private boolean doneReadingEvents;
    private StoriesAdapter mAdapter;

    private void initialize(){

        readEventCount = eventCount = readDeliveryCount = deliveryCount = 0;
        deliveries = new ArrayList<>();
        events = new ArrayList<>();
        stories = new ArrayList<>();
        deliveryQuery = FirebaseDatabase.getInstance().getReference().child("Deliveries").orderByChild("status").equalTo("delivered");
        eventDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Events");

        deliveryListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                DeliveryItem deliveryItem = dataSnapshot.getValue(DeliveryItem.class);
                deliveries.add(deliveryItem);
                readDeliveryCount++;
                if(readDeliveryCount == deliveryCount)
                    doneReadingDeliveries = true;

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

        eventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Event event = dataSnapshot.getValue(Event.class);
                events.add(event);
                readEventCount++;
                if(readEventCount == eventCount)
                    doneReadingEvents = true;

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

    public StoriesFragment() {
        // Required empty public constructor
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
        View view = inflater.inflate(R.layout.fragment_stories, container, false);
        recyclerView = view.findViewById(R.id.stories_container);
        mAdapter = new StoriesAdapter(stories);
        recyclerView.setAdapter(mAdapter);
        progressBar = view.findViewById(R.id.progressBar);
        return view;

    }

    @Override
    public void onResume() {
        super.onResume();

        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);

        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                if(doneReadingDeliveries && doneReadingEvents)
                    enableUserInteraction();
                else
                    handler.postDelayed(this, 1000);

            }
        };

        handler.postDelayed(runnable, 1000);

        deliveryQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                readDeliveryCount = 0;
                deliveryCount = dataSnapshot.getChildrenCount();
                doneReadingDeliveries = false;
                deliveries = new ArrayList<>();

                if(deliveryCount == 0)
                    doneReadingDeliveries = true;
                else
                    deliveryQuery.addChildEventListener(deliveryListener);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                makeToast(databaseError.getMessage());
                doneReadingDeliveries = true;

            }
        });

        eventDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                eventCount = dataSnapshot.getChildrenCount();
                readEventCount = 0;
                doneReadingEvents = false;
                events = new ArrayList<>();

                if(eventCount == 0)
                    doneReadingEvents = true;
                else
                    eventDatabaseReference.addChildEventListener(eventListener);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                makeToast(databaseError.getMessage());
                doneReadingEvents = true;

            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();

        if(deliveryListener != null)
            deliveryQuery.removeEventListener(deliveryListener);

        if(eventListener != null)
            eventDatabaseReference.removeEventListener(eventListener);

    }

    private void enableUserInteraction()
    {

        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        constructStories();

    }

    private void constructStories(){

        stories = new ArrayList<>();

        for(DeliveryItem item : deliveries){
            String addedBy = item.getDelivererName();
            String mainContent = "Donated by " + item.getDonorName() + "\n" + "Delivered to " + item.getOrgName() + "\n";
            Story story = new Story(addedBy, mainContent, item.getDeliveryImageURL(), item.getTimeStamp());
            stories.add(story);
        }

        for(Event event : events)
            stories.add(new Story(event.getOrganization(), event.getName() + "\n" + event.getDescription(), event.getImageURL(), event.getTimeStamp().toString()));

        sortList();

    }

    private void sortList(){

        if(deliveries != null){

            Collections.sort(stories, new Comparator<Story>() {
                @Override
                public int compare(Story o1, Story o2) {
                    return o2.getTimeStamp().compareTo(o1.getTimeStamp());
                }
            });
        }

        buildRecyclerView();
        attachListenerToRecyclerView();

    }

    private void buildRecyclerView(){

        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mAdapter = new StoriesAdapter(stories);
        recyclerView.setAdapter(mAdapter);

    }

    private void attachListenerToRecyclerView(){

        mAdapter.setOnItemClickListener(new StoriesAdapter.OnClickListener() {
            @Override
            public void onClickImage(ImageView view, int position) {

                //Add code to enlarge the image

            }
        });

    }

    public void makeToast(String message){

        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

}
