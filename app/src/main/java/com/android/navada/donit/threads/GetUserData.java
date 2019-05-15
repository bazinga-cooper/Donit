package com.android.navada.donit.threads;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.widget.Toast;
import com.android.navada.donit.activities.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;

public class GetUserData extends Thread {

    private MainActivity activity;
    private ProgressDialog mProgressDialog;
    public boolean done;

    public GetUserData(MainActivity activity){

        done = false;
        this.activity = activity;
        mProgressDialog = activity.progressDialog;
    }

    @Override
    public void run() {
        super.run();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getUid());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                MainActivity.user = (HashMap<String, Object>) dataSnapshot.getValue();
                done = true;
                mProgressDialog.cancel();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                mProgressDialog.cancel();
                makeToast(databaseError.getMessage());

            }
        });

    }

    private void makeToast(String message){

        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();

    }

}
