package app.subversive.groceryratings.UI;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import app.subversive.groceryratings.Core.Product;
import app.subversive.groceryratings.MainWindow;
import app.subversive.groceryratings.R;
import app.subversive.groceryratings.Utils;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by rob on 9/1/14.
 */
public class ProductRatingBar extends FrameLayout {
    static class hideOnEnd extends AnimatorListenerAdapter {
        final View v;
        public hideOnEnd (View v) { this.v = v;  }
        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            v.setVisibility(INVISIBLE);
        }
    }

    final long duration = 200;


    private Product product;


    TextView productName, productNumReviews;
    RatingBar productStars;

    View rating, loading;

    final static LayoutParams defaultLayout = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    static {
        defaultLayout.gravity = Gravity.CENTER;
    }

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

    public void debugLoadBarcode() {
        showLoading(false);
        new AsyncTask<Void, Void, Product>() {
            @Override
            protected void onPostExecute(Product product) {
                super.onPostExecute(product);
                setProduct(product);
                showRating(true);
            }

            @Override
            protected Product doInBackground(Void... params) {
                SystemClock.sleep(2000);
                Product p = new Product("A Product named Bob", (int) Math.round(Math.random()*5), (int) Math.round(Math.random()*20));
                return p;
            }
        }.execute();
    }

    public void loadBarcode(String barcode) {
        showLoading(false);
        MainWindow.service.getProduct(barcode, new Callback<Product>() {
            @Override
            public void success(Product product, Response response) {
                setProduct(product);
                showRating(true);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void crossfade(View vFrom, View vTo) {
        vTo.setAlpha(0.f);
        vTo.setVisibility(VISIBLE);
        vFrom.animate().alpha(0f).setDuration(duration).setListener(new hideOnEnd(vFrom)).start();
        vTo.animate().alpha(1f).setDuration(duration).start();
    }

    public void showRating(boolean animated) {
        if (animated) {
            crossfade (loading, rating);
        } else {
            rating.setVisibility(VISIBLE);
            loading.setVisibility(INVISIBLE);
        }
    }

    public void showLoading(boolean animated) {
        rating.setVisibility(INVISIBLE);
        loading.setVisibility(VISIBLE);
    }

    private void init(Context context) {
        setBackgroundResource(R.color.blackOverlay);
        Utils.setPaddingDP(this, 8, 4, 8, 4);

        rating = inflate(context, R.layout.rating_bar_contents, null);
        loading = inflate(context, R.layout.loading, null);

        rating.setVisibility(INVISIBLE);
        loading.setVisibility(INVISIBLE);

        addView(rating, defaultLayout);
        addView(loading, defaultLayout);

        productName = ((TextView) rating.findViewById(R.id.productName));
        productStars = ((RatingBar) rating.findViewById(R.id.productStars));
        productNumReviews = ((TextView) rating.findViewById(R.id.productNumReviews));
    }
}
