package app.subversive.groceryratings.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import app.subversive.groceryratings.Core.Product;
import app.subversive.groceryratings.R;
import app.subversive.groceryratings.Utils;

/**
 * Created by rob on 9/1/14.
 */
public class ProductRatingBar extends LinearLayout {
    private Product product;

    TextView productName, productNumReviews;
    RatingBar productStars;


    public ProductRatingBar(Context context) {
        super(context);
        init(context);
    }

    public ProductRatingBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ProductRatingBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void setProduct(Product product) {
        this.product = product;

        productName.setText(product.getName());
        productStars.setRating(product.getNumStars());

        int nReviews = product.getRatingCount();
        String reviews = String.format((nReviews == 1) ? "%d Review" : "%d Reviews", nReviews);
        productNumReviews.setText(reviews);

    }

    private void init(Context context) {
        setOrientation(HORIZONTAL);
        setBackgroundResource(R.color.blackOverlay);
        Utils.setPaddingDP(this, 8, 4, 8, 4);
        inflate(context, R.layout.rating_bar_contents, this);
        productName = ((TextView) findViewById(R.id.productName));
        productStars = ((RatingBar) findViewById(R.id.productStars));
        productNumReviews = ((TextView) findViewById(R.id.productNumReviews));
    }
}
