package app.subversive.groceryratings.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import app.subversive.groceryratings.Core.Rating;
import app.subversive.groceryratings.R;

/**
 * Created by rob on 1/8/15.
 */
public class RatingDisplay extends LinearLayout {
    public RatingDisplay(Context context) {
        super(context);
        init(context);
    }

    public RatingDisplay(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RatingDisplay(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        setOrientation(HORIZONTAL);
        inflate(context, R.layout.review_item, this);
    }

    public void setRating(Rating rating) {
        ((Rater) findViewById(R.id.ratingStars)).setRating(rating.stars);
        ((TextView) findViewById(R.id.tvDate)).setText(rating.getDateString());
        ((TextView) findViewById(R.id.tvReview)).setText(rating.comment);
    }
}
