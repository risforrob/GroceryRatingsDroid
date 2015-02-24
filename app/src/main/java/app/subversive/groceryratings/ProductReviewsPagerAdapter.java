package app.subversive.groceryratings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import app.subversive.groceryratings.Core.Rating;
import app.subversive.groceryratings.Core.TasteTag;
import app.subversive.groceryratings.Core.Variant;
import app.subversive.groceryratings.UI.Rater;
import app.subversive.groceryratings.UI.SequentialLayout;
import app.subversive.groceryratings.UI.TagDisplay;

/**
 * Created by rob on 2/22/15.
 */
public class ProductReviewsPagerAdapter extends RecyclingPagerAdapter {
    List<Rating> ratings;
    public ProductReviewsPagerAdapter(List<Rating> ratings) {
        this.ratings = ratings;
    }

    @Override
    public void removeAllChildren(View v) {
        ((SequentialLayout) v.findViewById(R.id.layoutTags)).removeAllViews();
    }

    @Override
    View inflateView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.full_review, null);
    }

    @Override
    void loadData(int position, View view) {
        Rating rating = ratings.get(position);
        ((Rater) view.findViewById(R.id.ratingStars)).setRating(rating.stars);
        ((TextView) view.findViewById(R.id.tvDate)).setText(rating.getDateString());
        ((TextView) view.findViewById(R.id.tvReview)).setText(rating.comment);

        SequentialLayout tagLayout = (SequentialLayout) view.findViewById(R.id.layoutTags);

        if (rating.tags != null) {
            for(TasteTag tag : rating.tags) {
                TagDisplay td = new TagDisplay(tagLayout.getContext());
                td.setText(tag.value);
                td.setValue(null);
                tagLayout.addView(td);
            }
        }
    }

    @Override
    public int getCount() {
        return (ratings != null) ? ratings.size() : 0;
    }
}
