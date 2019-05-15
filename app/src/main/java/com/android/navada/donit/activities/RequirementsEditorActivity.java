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
    private EditText editText;
    private Button submitButton;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requirements_editor);
        editText = findViewById(R.id.add_req_edit_text);
        submitButton =findViewById(R.id.submit_button);
        Intent intent = getIntent();
        position = intent.getIntExtra("position",-1);
        if(position!=-1){
            editText.setText(AddRequirementFragment.requirements.get(position));
        }
        else{
            AddRequirementFragment.requirements.add("");
            position = AddRequirementFragment.requirements.size()-1;
            AddRequirementFragment.arrayAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRequirement();
            }
        });
    }

    private void addRequirement(){
        AddRequirementFragment.requirements.set(position,editText.getText().toString());
        AddRequirementFragment.arrayAdapter.notifyDataSetChanged();
        FirebaseDatabase.getInstance().getReference().child("Requirements").child(FirebaseAuth.getInstance().getUid()).setValue(AddRequirementFragment.requirements);
       onBackPressed();
        // save the whole list here
        // need updating
    }
}
