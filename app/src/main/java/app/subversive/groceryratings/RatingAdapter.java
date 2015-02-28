package app.subversive.groceryratings;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.subversive.groceryratings.Core.Rating;
import app.subversive.groceryratings.UI.Rater;

/**
 * Created by rob on 1/11/15.
 */
public class RatingAdapter extends RecyclerView.Adapter<RatingAdapter.ViewHolder> {
    public interface ItemClickListener {
        public void onItemClick(int i);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        Rater mRater;
        TextView tvDate;
        TextView tvComment;
        ImageView image;

        public ViewHolder(View itemView) {
            super(itemView);
            mRater = (Rater) itemView.findViewById(R.id.ratingStars);
            tvDate = (TextView) itemView.findViewById(R.id.tvDate);
            tvComment = (TextView) itemView.findViewById(R.id.tvReview);
            image = (ImageView) itemView.findViewById(R.id.ratingImage);
        }
    }

    List<Rating> ratings;
    final Map<ViewHolder, Integer> viewIndicies = new HashMap<>();
    ItemClickListener mListener;

    public RatingAdapter(List<Rating> ratings) {
        this.ratings = ratings;
    }
    public void setItemClickListener(ItemClickListener listener) { mListener = listener;}

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.review_item, viewGroup, false);
        v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        Utils.setPaddingDP(v, 8, 8, 8, 8);
        final ViewHolder holder = new ViewHolder(v);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(viewIndicies.get(holder));
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        Rating r = ratings.get(i);
        viewHolder.mRater.setRating(r.stars);
        viewHolder.tvDate.setText(r.getDateString());
        viewHolder.tvComment.setText(r.comment);
        Log.i("RatingAdapter", String.format("%s", r.user.pictureURL));
        if (r.user.pictureURL != null && !r.user.pictureURL.isEmpty()) {
            Picasso.with(viewHolder.image.getContext()).load(r.user.pictureURL).into(viewHolder.image);
        }
        viewIndicies.put(viewHolder, i);
    }

    @Override
    public int getItemCount() {
        return (ratings != null) ? ratings.size() : 0;
    }
}
