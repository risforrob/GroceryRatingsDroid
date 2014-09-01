package app.subversive.groceryratings.UI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import app.subversive.groceryratings.Core.Product;
import app.subversive.groceryratings.R;

/**
 * Created by rob on 9/1/14.
 */
public class ProductRatingBar {
    private Product product;
    private View view;

    public ProductRatingBar(Product product) {
        this.product = product;
    }

    public View getView(Context context) {
        View v = LayoutInflater.from(context).inflate(R.layout.rating_bar_contents, null);
        ((TextView) v.findViewById(R.id.productName)).setText(product.getName());
        ((RatingBar) v.findViewById(R.id.productStars)).setRating(product.getNumStars());

        int nReviews = product.getRatingCount();
        String reviews = String.format((nReviews == 1) ? "%d Review" : "%d Reviews", nReviews);
        ((TextView) v.findViewById(R.id.productNumReviews)).setText(reviews);

        return v;
    }
}
