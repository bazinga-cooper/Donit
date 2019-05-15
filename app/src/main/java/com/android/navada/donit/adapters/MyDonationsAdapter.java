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

public class MyDonationsAdapter extends RecyclerView.Adapter<MyDonationsAdapter.DonationViewHolder> {

    private List<DonationItem> donations;
    private OnClickListener mListener;

    public MyDonationsAdapter(List<DonationItem> donations){

        this.donations = donations;

    }

    public interface OnClickListener{

        void onClick(int position);
        void onClickImage(ImageView view, int position);

    }

    public void setOnItemClickListener(OnClickListener listener){

        mListener = listener;

    }

    static class DonationViewHolder extends RecyclerView.ViewHolder{

        private TextView descriptionTextView;
        private TextView statusTextView;
        private TextView delivererNameTextView;
        private TextView delivererContactNumberTextView;
        private ImageView donationImageView;
        private ImageView deliveryImageView;

        DonationViewHolder(View view, final OnClickListener listener){

            super(view);
            descriptionTextView = view.findViewById(R.id.description);
            statusTextView = view.findViewById(R.id.status_of_delivery);
            delivererNameTextView = view.findViewById(R.id.deliverer_name);
            delivererContactNumberTextView = view.findViewById(R.id.deliverer_contact_number);
            donationImageView = view.findViewById(R.id.donation_image);
            deliveryImageView = view.findViewById(R.id.delivery_image);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(listener!=null)
                        listener.onClick(getAdapterPosition());

                }
            });

            donationImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(listener!=null)
                        listener.onClickImage(donationImageView, getAdapterPosition());

                }
            });

            deliveryImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(listener!=null)
                        listener.onClickImage(deliveryImageView, getAdapterPosition());

                }
            });

        }

    }

    @NonNull
    @Override
    public DonationViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.donation_item,viewGroup,false);
        return new DonationViewHolder(view, mListener);

    }

    @Override
    public void onBindViewHolder(@NonNull DonationViewHolder viewHolder, int i) {

        DonationItem donationItem = donations.get(i);

        viewHolder.descriptionTextView.setText(donationItem.getDescription());
        viewHolder.statusTextView.setText(donationItem.getStatus().trim().toUpperCase());

        if(!donationItem.getStatus().trim().equals("pending")) {

            viewHolder.delivererNameTextView.setText(donationItem.getDelivereName());
            viewHolder.delivererContactNumberTextView.setText(donationItem.getDelivererContactNumber());

        }

        String donationImageUrl = donationItem.getDonationImageUrl();
        String deliveryImageUrl = donationItem.getDeliveryImageUrl();

        if(donationImageUrl != null)
            Picasso.get().load(donationImageUrl).into(viewHolder.donationImageView);
        else
            viewHolder.donationImageView.setImageResource(R.drawable.no_image);

        if(deliveryImageUrl != null)
            Picasso.get().load(deliveryImageUrl).into(viewHolder.deliveryImageView);
        else
            viewHolder.deliveryImageView.setImageResource(R.drawable.no_image);

    }

    @Override
    public int getItemCount() {
        return donations.size();
    }

}
