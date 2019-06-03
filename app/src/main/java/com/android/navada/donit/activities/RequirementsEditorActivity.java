package com.android.navada.donit.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.navada.donit.R;
import com.android.navada.donit.fragments.AddRequirementFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class RequirementsEditorActivity extends AppCompatActivity {
    private EditText mEditText;
    private Button mSubmitButton;
    private int mPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requirements_editor);
        mEditText = findViewById(R.id.add_req_edit_text);
        mSubmitButton =findViewById(R.id.submit_button);
        Intent intent = getIntent();
        mPosition = intent.getIntExtra("position",-1);
        if(mPosition !=-1){
            mEditText.setText(AddRequirementFragment.requirements.get(mPosition));
        }
        else{
            AddRequirementFragment.requirements.add("");
            mPosition = AddRequirementFragment.requirements.size()-1;
            AddRequirementFragment.arrayAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRequirement();
            }
        });
    }

    private void addRequirement(){
        AddRequirementFragment.requirements.set(mPosition, mEditText.getText().toString());
        AddRequirementFragment.arrayAdapter.notifyDataSetChanged();
        FirebaseDatabase.getInstance().getReference().child("Requirements").child(FirebaseAuth.getInstance()
                .getUid()).setValue(AddRequirementFragment.requirements);
        onBackPressed();
        // save the whole list here
        // need updating
    }
}
