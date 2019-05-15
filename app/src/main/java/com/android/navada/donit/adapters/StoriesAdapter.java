package com.android.navada.donit.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.navada.donit.R;
import com.android.navada.donit.pojos.Story;
import com.squareup.picasso.Picasso;
import java.util.List;

public class StoriesAdapter extends RecyclerView.Adapter<StoriesAdapter.StoryViewHolder> {

    private List<Story> stories;
    private OnClickListener mListener;

    public StoriesAdapter(List<Story> stories){
        this.stories = stories;
    }

    public interface  OnClickListener{

        void onClickImage(ImageView view, int position);

    }

    public void setOnItemClickListener(OnClickListener clickListener){

        mListener = clickListener;

    }

    static class StoryViewHolder extends RecyclerView.ViewHolder{

        private TextView addedByTextView;
        private TextView mainContentTextView;
        private ImageView storyImageView;

        StoryViewHolder(View view,final OnClickListener listener){

            super(view);
            addedByTextView = view.findViewById(R.id.addedByTextView);
            mainContentTextView = view.findViewById(R.id.mainContentTextView);
            storyImageView = view.findViewById(R.id.story_image);

            storyImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(listener != null)
                        listener.onClickImage(storyImageView, getAdapterPosition());

                }
            });

        }

    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.story_item, viewGroup, false);
        return new StoriesAdapter.StoryViewHolder(view, mListener);

    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder viewHolder, int i) {

        Story story = stories.get(i);
        String addedBy = "Added By " + story.getAddedBy();
        viewHolder.addedByTextView.setText(addedBy);
        viewHolder.mainContentTextView.setText(story.getMainContent());
        Log.i("Hello", "onBindViewHolder: " + story.getImageURL());
        Picasso.get().load(story.getImageURL()).into(viewHolder.storyImageView);

    }

    @Override
    public int getItemCount() {
        return stories.size();
    }
}
