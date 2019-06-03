package com.android.navada.donit.fragments;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.navada.donit.R;
import com.android.navada.donit.activities.MainActivity;
import com.android.navada.donit.pojos.DeliveryItem;
import com.android.navada.donit.pojos.DonationItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 */
public class FeedFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private DatabaseReference mDonorSpotDatabaseReference;
    private ChildEventListener mDonorSpotChildEventListener;
    private View mView;
    private ArrayList<DonationItem> mDonationItems;
    private ArrayList<String> mDonationIds;
    private long mDonationCount = 0, mReadDonationCount =0;
    private boolean mDoneReadingDonations;
    private ProgressBar mProgressBar;
    private AlertDialog mAlertDialog;
    private AlertDialog.Builder mBuilder;
    private ChildEventListener mChildEventListener;
    private ProgressDialog mProgressDialog;
    private HashMap<String,String> mOrgs;
    private GoogleMap mMap;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private Boolean mLocationPermissionGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST = 1234;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final float DEFAULT_ZOOM = 15;
    private boolean mOnMapReadyOk = false;
    private boolean mOnMarkerAddOk = true;
    public String mSelectedImageUrl;
    public static Double donationLat,donationLng,orgLat,orgLng;
    private DatabaseReference mDatabaseReference;
    private boolean mLoadCollectAndDeliver;



    public FeedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Deliveries");
        mDonorSpotDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Donations");
        mDonorSpotChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                ObjectMapper objectMapper = new ObjectMapper();
                HashMap<String,Object> data = (HashMap<String, Object>)dataSnapshot.getValue();
                DonationItem donationItem = objectMapper.convertValue(data, DonationItem.class);

                if(donationItem.getStatus().equals("Pending")){
                    mOrgs = (HashMap<String, String>) MainActivity.user.get("orgs");
                    Set set = mOrgs.entrySet();
                    Iterator iterator = set.iterator();
                    while(iterator.hasNext()){
                        Map.Entry mEntry = (Map.Entry) iterator.next();
                        if(donationItem.getChosenOrganizationId().equals(mEntry.getKey())&&mEntry.getValue().toString().equals("true")){
                            mDonationItems.add(donationItem);
                            mDonationIds.add(dataSnapshot.getKey());
                        }
                    }
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
        mView =  inflater.inflate(R.layout.fragment_feed, container, false);
        mProgressBar = mView.findViewById(R.id.progressBar);

        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mDonationItems = new ArrayList<>();
        mDonationIds = new ArrayList<>();
        mProgressDialog = new ProgressDialog(getContext());
        mProgressDialog.setCancelable(false);
        getLocationPermission();
        getDeviceLocation();
        noPendingDeliveries();
        if (!mLoadCollectAndDeliver) {

            mDonorSpotDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    mDonationCount = dataSnapshot.getChildrenCount();

                    if (mDonationCount != 0)
                        mDonorSpotDatabaseReference.addChildEventListener(mDonorSpotChildEventListener);
                    else
                        mDoneReadingDonations = true;

                    if (mDoneReadingDonations)
                        enableUserInteraction();


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            getDeviceLocation();
            getLocationPermission();
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        if(mDonorSpotChildEventListener !=null)
            mDonorSpotDatabaseReference.removeEventListener(mDonorSpotChildEventListener);
    }
    public void enableUserInteraction(){
        addMarker();
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        String[] marketTag = marker.getTag().toString().split(" ");
        final int position = Integer.parseInt(marketTag[0]);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        final String addressOfMarker = mDonationItems.get(position).getAddress(),Donationinfo = mDonationItems.get(position).getDescription();
        final String name = mDonationItems.get(position).getDonorName();
        donationLat = marker.getPosition().latitude;
        donationLng = marker.getPosition().longitude;
        orgLat = mDonationItems.get(position).getOrgLat();
        orgLng = mDonationItems.get(position).getOrgLng();
        final String imageUrl = mDonationItems.get(position).getDonationImageUrl();
        final String contactNumber = mDonationItems.get(position).getDonorContactNumber();
        String type = mDonationItems.get(position).getCategory();
        ImageView imageView = new ImageView(getContext());
        Picasso.get().load(imageUrl).resize(150,150).into(imageView);
        alertDialogBuilder.setMessage("Address of Donor : \n"+addressOfMarker+"\n"+"Doantion Type:\n"+type+"\nDonation Info: \n"+Donationinfo+"\nContact Number:\n"+contactNumber);
        alertDialogBuilder.setPositiveButton("Confirm PickUp", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Long timeStamp = System.currentTimeMillis()/1000;
                DeliveryItem deliveryItem = new DeliveryItem("pending",timeStamp.toString(), mDonationIds.get(position),name,contactNumber,addressOfMarker, mDonationItems.get(position).getDeliveryAddress(),imageUrl,null, FirebaseAuth.getInstance().getUid(),MainActivity.user.get("name").toString(), mDonationItems.get(position).getChosenOrganizationId());
                deliveryItem.setDonorLat(marker.getPosition().latitude+"");
                deliveryItem.setDonorLng(marker.getPosition().longitude+"");
                deliveryItem.setOrgLat(mDonationItems.get(position).getOrgLat()+"");
                deliveryItem.setOrgLng(mDonationItems.get(position).getOrgLng()+"");
                Double orgLat = mDonationItems.get(position).getOrgLat();
                Double orgLng = mDonationItems.get(position).getOrgLng();
                String destinationAddress = getAddress(orgLat,orgLng);
                deliveryItem.setDestinationAddress(destinationAddress);
                mDatabaseReference.push().setValue(deliveryItem);
                mDonationItems.get(position).setDelivereName(MainActivity.user.get("name").toString());
                mDonationItems.get(position).setStatus("picked");
                mDonorSpotDatabaseReference.child(mDonationIds.get(position)).setValue(mDonationItems.get(position));
                loadCollectAndDeliver();
            }
        }).setView(imageView);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        return false;
    }


    private void initMap() {
        Toast.makeText(getContext(), "Map is ready", Toast.LENGTH_SHORT).show();
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.feedMap);
        mapFragment.getMapAsync(this);
    }
    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(getActivity(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(getActivity(),
                    COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(getActivity(), permissions, LOCATION_PERMISSION_REQUEST);
            }
        }else {
            ActivityCompat.requestPermissions(getActivity(), permissions, LOCATION_PERMISSION_REQUEST);
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mOnMapReadyOk = true;
        if (mLocationPermissionGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
        }
        mMap.setOnMarkerClickListener(this);
    }

    private void moveCamera(LatLng latLng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        try {
            if (mLocationPermissionGranted) {
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful() && task.getResult()!=null) {
                            Location currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                        } else {
                            Toast.makeText(getContext(), "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Toast.makeText(getActivity(), "Location Error", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                    initMap();
                }
        }
    }

    public void addMarker(){
        for(int i = 0; i< mDonationItems.size(); i++){
            HashMap<String,Object> address = mDonationItems.get(i).getDonorAddress();
            Marker mMarker =  mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(address.get("latitude").toString()),Double.parseDouble(address.get("longitude").toString()))));
            mMarker.setTag(i+" "+ mDonationIds.get(i));
        }
    }

    public void loadCollectAndDeliver(){
            getFragmentManager().beginTransaction().replace(R.id.frame_container,new CollectAndDeliverFragment(),null).commit();

    }

    public void noPendingDeliveries(){
        Query mQuery = FirebaseDatabase.getInstance().getReference().
                child("Deliveries").
                orderByChild("status").equalTo("pending");

        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.getChildrenCount()!=0) {
                 mLoadCollectAndDeliver = true;
                    for(DataSnapshot db : dataSnapshot.getChildren()){
                        DeliveryItem di = db.getValue(DeliveryItem.class);
                        orgLat = Double.parseDouble(di.getOrgLat());
                        orgLng = Double.parseDouble(di.getOrgLng());
                        donationLat = Double.parseDouble(di.getDonorLat());
                        donationLng = Double.parseDouble(di.getDonorLng());

                        loadCollectAndDeliver();

                    }
                }else{
                    mLoadCollectAndDeliver = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "dataBaseError", Toast.LENGTH_SHORT).show();
            }
        });


    }
    public String getAddress(Double lat,Double lng){
        String address = "";
        Geocoder mgeocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> mListAddress = mgeocoder.getFromLocation(lat,lng,1);
            if(mListAddress != null && mListAddress.size() > 0){
                address = "";
                if(mListAddress.get(0).getThoroughfare() != null){
                    address += mListAddress.get(0).getThoroughfare().toString() + " ";
                }
                if(mListAddress.get(0).getSubAdminArea() != null){
                    address += mListAddress.get(0).getSubAdminArea().toString() + " ";
                }
                if(mListAddress.get(0).getLocality() != null){
                    address += mListAddress.get(0).getLocality().toString()+" ";
                }
                if(mListAddress.get(0).getAdminArea() != null) {
                    address += mListAddress.get(0).getAdminArea().toString()+" ";
                }
                if(mListAddress.get(0).getPostalCode() != null){
                    address += mListAddress.get(0).getPostalCode().toString();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;

    }
}


