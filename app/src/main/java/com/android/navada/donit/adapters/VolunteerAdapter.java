package com.android.navada.donit.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.navada.donit.R;
import com.android.navada.donit.pojos.User;
import java.util.List;

public class VolunteerAdapter extends RecyclerView.Adapter<VolunteerAdapter.VolunteerViewHolder> {

    private List<User> volunteers;
    private OnClickListener listener;

    public VolunteerAdapter(List<User> volunteers){

        this.volunteers = volunteers;

    }

    public interface OnClickListener{

        void onClick(View view, int position);

    }

    public void setOnClickListener(OnClickListener listener){

        this.listener = listener;

    }

    static class VolunteerViewHolder extends RecyclerView.ViewHolder{

        private TextView nameTextView;
        private TextView contactNumberTextView;
        private TextView emailTextView;

        VolunteerViewHolder(final View view, final OnClickListener listener){

            super(view);

            nameTextView = view.findViewById(R.id.nameTextView);
            emailTextView = view.findViewById(R.id.emailTextView);
            contactNumberTextView = view.findViewById(R.id.numberTextView);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(listener != null)
                        listener.onClick(view, getAdapterPosition());

                }
            });

        }

    }

    @NonNull
    @Override
    public VolunteerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.volunteer_item, viewGroup, false);
        return new VolunteerViewHolder(view, listener);

    }

    @Override
    public void onBindViewHolder(@NonNull VolunteerViewHolder volunteerViewHolder, int i) {

        User user = volunteers.get(i);

        volunteerViewHolder.nameTextView.setText(user.getName());
        volunteerViewHolder.contactNumberTextView.setText(user.getMobileNumber());
        volunteerViewHolder.emailTextView.setText(user.getEmail());


    }

    @Override
    public int getItemCount() {
        return volunteers.size();
    }
}
