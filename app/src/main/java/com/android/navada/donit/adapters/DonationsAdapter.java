package com.android.navada.donit.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.navada.donit.R;
import com.android.navada.donit.pojos.DonationItem;
import com.squareup.picasso.Picasso;
import java.util.List;

public class DonationsAdapter extends RecyclerView.Adapter<DonationsAdapter.DonationViewHolder> {

    private List<DonationItem> donations;
    private OnClickListener mListener;

    public DonationsAdapter(List<DonationItem> donations){

        this.donations = donations;

    }

    public interface OnClickListener{

        void onClick(int position);

    }

    public void setOnItemClickListener(OnClickListener listener){

        mListener = listener;

    }

    static class DonationViewHolder extends RecyclerView.ViewHolder{

        private TextView descriptionTextView;
        private ImageView donationImageView;
        private TextView itemType,donationAddress;

        DonationViewHolder(View view, final OnClickListener listener){

            super(view);
            descriptionTextView = view.findViewById(R.id.description);
            donationImageView = view.findViewById(R.id.donation_image);
            itemType = view.findViewById(R.id.item_type);
            donationAddress = view.findViewById(R.id.donation_address);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(listener!=null)
                        listener.onClick(getAdapterPosition());

                }
            });


        }

    }

    @NonNull
    @Override
    public DonationViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.donations_item,viewGroup,false);
        return new DonationViewHolder(view, mListener);

    }

    @Override
    public void onBindViewHolder(@NonNull DonationViewHolder viewHolder, int i) {

        DonationItem donationItem = donations.get(i);

        viewHolder.descriptionTextView.setText(donationItem.getDescription());
        viewHolder.donationAddress.setText(donationItem.getAddress());
        viewHolder.itemType.setText(donationItem.getCategory());
        String donationImageUrl = donationItem.getDonationImageUrl();

        if(donationImageUrl != null)
            Picasso.get().load(donationImageUrl).into(viewHolder.donationImageView);
        else
            viewHolder.donationImageView.setImageResource(R.drawable.no_image);

    }

    @Override
    public int getItemCount() {
        return donations.size();
    }

}
