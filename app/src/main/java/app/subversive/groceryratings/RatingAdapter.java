package app.subversive.groceryratings;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import app.subversive.groceryratings.Core.Rating;
import app.subversive.groceryratings.UI.Rater;

/**
 * Created by rob on 1/11/15.
 */
public class RatingAdapter extends RecyclerView.Adapter<RatingAdapter.ViewHolder> {
    static class ViewHolder extends RecyclerView.ViewHolder {
        Rater mRater;
        TextView tvDate;
        TextView tvComment;

        public ViewHolder(View itemView) {
            super(itemView);
            mRater = (Rater) itemView.findViewById(R.id.ratingStars);
            tvDate = (TextView) itemView.findViewById(R.id.tvDate);
            tvComment = (TextView) itemView.findViewById(R.id.tvReview);
        }
    }

    List<Rating> ratings;

    public RatingAdapter(List<Rating> ratings) {
        this.ratings = ratings;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.review_item, viewGroup, false);
        v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        Utils.setPaddingDP(v, 8, 8, 8, 8);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        Rating r = ratings.get(i);
        viewHolder.mRater.setRating(r.stars);
        viewHolder.tvDate.setText(r.getDateString());
        viewHolder.tvComment.setText(r.comment);
    }

    @Override
    public int getItemCount() {
        return (ratings != null) ? ratings.size() : 0;
    }
}
