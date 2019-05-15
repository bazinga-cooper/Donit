package com.android.navada.donit.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.navada.donit.R;
import com.android.navada.donit.pojos.Organization;
import java.util.List;

public class OrganizationAdapter extends RecyclerView.Adapter<OrganizationAdapter.OrganizationViewHolder> {

    private List<Organization> organizations;
    private OnClickListener listener;

    public OrganizationAdapter(List<Organization> organizations){

        this.organizations = organizations;

    }

    public interface OnClickListener{

        void onClick(View view, int position);

    }

    public void setOnClickListener(OnClickListener listener){

        this.listener = listener;

    }

    static class OrganizationViewHolder extends RecyclerView.ViewHolder{

        private TextView nameTextView;
        private TextView addressTextView;
        private TextView contactNumberTextView;
        private TextView emailTextView;

        OrganizationViewHolder(final View view, final OnClickListener listener){

            super(view);

            nameTextView = view.findViewById(R.id.nameTextView);
            emailTextView = view.findViewById(R.id.emailTextView);
            contactNumberTextView = view.findViewById(R.id.numberTextView);
            addressTextView = view.findViewById(R.id.addressTextView);

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
    public OrganizationViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.organization_item, viewGroup, false);
        return new OrganizationViewHolder(view, listener);

    }

    @Override
    public void onBindViewHolder(@NonNull OrganizationViewHolder volunteerViewHolder, int i) {

        Organization organization = organizations.get(i);

        volunteerViewHolder.nameTextView.setText(organization.getName());
        volunteerViewHolder.contactNumberTextView.setText(organization.getMobileNumber());
        volunteerViewHolder.emailTextView.setText(organization.getEmail());
        volunteerViewHolder.addressTextView.setText(organization.getAddress());


    }

    @Override
    public int getItemCount() {
        return organizations.size();
    }
}
