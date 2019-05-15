package com.android.navada.donit.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.navada.donit.R;
import com.android.navada.donit.pojos.DeliveryItem;
import com.squareup.picasso.Picasso;
import java.util.List;

public class MyDeliveriesAdapter extends RecyclerView.Adapter<MyDeliveriesAdapter.DeliveryViewHolder> {

    private List<DeliveryItem> deliveries;
    private OnClickListener mListener;

    public MyDeliveriesAdapter(List<DeliveryItem> deliveries){

        this.deliveries = deliveries;

    }

    public interface  OnClickListener{

        void onClick(View view,int position);
        void onClickAddDeliveryImage(View view,int position);
        void onClickImage(ImageView view,int position);

    }

    public void setOnItemClickListener(OnClickListener clickListener){

        mListener = clickListener;

    }

    static class DeliveryViewHolder extends RecyclerView.ViewHolder{

        private TextView donorNameTextView;
        private TextView donorContactNumberTextView;
        private TextView sourceAddressTextView;
        private TextView destinationAddressTextView;
        private TextView statusTextView;
        private ImageView donationImageView;
        private ImageView deliveryImageView;

        DeliveryViewHolder(View view, final OnClickListener mListener){

            super(view);

            donorNameTextView = view.findViewById(R.id.donor_name_text);
            donorContactNumberTextView = view.findViewById(R.id.donor_contact_number_text);
            sourceAddressTextView = view.findViewById(R.id.donor_address);
            destinationAddressTextView = view.findViewById(R.id.detination_address);
            statusTextView = view.findViewById(R.id.status);
            donationImageView = view.findViewById(R.id.donation_image);
            deliveryImageView = view.findViewById(R.id.delivery_image);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(mListener!=null)
                        mListener.onClick(view,getAdapterPosition());

                }
            });

            deliveryImageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    if(mListener!=null)
                        mListener.onClickAddDeliveryImage(view, getAdapterPosition());

                    return  true;

                }
            });

            deliveryImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(mListener!=null)
                        mListener.onClickImage(deliveryImageView, getAdapterPosition());

                }
            });

            donationImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(mListener!=null)
                        mListener.onClickImage(donationImageView, getAdapterPosition());

                }
            });

        }

    }


    @NonNull
    @Override
    public MyDeliveriesAdapter.DeliveryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.delivery_item, viewGroup, false);
        return new DeliveryViewHolder(view, mListener);

    }

    @Override
    public void onBindViewHolder(@NonNull MyDeliveriesAdapter.DeliveryViewHolder viewHolder, int i) {

        DeliveryItem mDeliveryDetails = deliveries.get(i);

        viewHolder.donorNameTextView.setText(mDeliveryDetails.getDonorName());
        viewHolder.donorContactNumberTextView.setText(mDeliveryDetails.getDonorContactNumber());
        viewHolder.sourceAddressTextView.setText(mDeliveryDetails.getSourceAddress());
        viewHolder.destinationAddressTextView.setText(mDeliveryDetails.getDestinationAddess());
        viewHolder.statusTextView.setText(deliveries.get(i).getStatus().toUpperCase());

        if(mDeliveryDetails.getDonationImageURL() != null)
            Picasso.get().load(mDeliveryDetails.getDonationImageURL()).into(viewHolder.donationImageView);
        else
            viewHolder.donationImageView.setImageResource(R.drawable.no_image);

        if(mDeliveryDetails.getDeliveryImageURL() != null)
            Picasso.get().load(mDeliveryDetails.getDeliveryImageURL()).into(viewHolder.deliveryImageView);
        else {
            if (mDeliveryDetails.getStatus().trim().equals("delivered"))
                viewHolder.deliveryImageView.setImageResource(R.drawable.no_image);
            else
                viewHolder.deliveryImageView.setImageResource(R.drawable.add_image_click);
        }

    }

    @Override
    public int getItemCount() {
        return deliveries.size();
    }

}
