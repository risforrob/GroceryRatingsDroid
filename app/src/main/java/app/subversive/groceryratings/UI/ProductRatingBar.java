package app.subversive.groceryratings.UI;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationSet;
import android.view.animation.CycleInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
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
public class ProductRatingBar extends FrameLayout implements View.OnClickListener {

    @Override
    public void onClick(View v) {

    }

    static class hideOnEnd extends AnimatorListenerAdapter {
        final View v;
        public hideOnEnd (View v) { this.v = v;  }
        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            v.setVisibility(INVISIBLE);
            v.setAlpha(1f);
        }
    }

    public interface BarcodeCallbacks {
        void onUnknownBarcode(String barcode);
    }


    public enum States {FETCHING, UNKNOWN, SUCCESS, PHOTO, ERROR, THANKS, UPLOADING}
    private States state;

    final long duration = 200;
    private Product product;
    private String barcode;


    TextView productName, productNumReviews, statusText;
//    RatingBar productStars;
    Rater productStars;
    ProgressBar progress;

    View rating, status, displayedView;

    int indexInParent;

    private boolean shouldFlash = true;
    private long flashInterval = 2000L;
    private final Runnable flashDelay = new Runnable() {
        @Override
        public void run() {
            shouldFlash = true;
        }
    };

    final static LayoutParams defaultLayout = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    static { defaultLayout.gravity = Gravity.CENTER; }

    private final AnimatorSet flashAnim;
    {
        ArgbEvaluator eval = new ArgbEvaluator();
        ValueAnimator flash, fade;
        flash = ObjectAnimator.ofInt(this, "backgroundColor", getResources().getColor(R.color.blackOverlay), getResources().getColor(R.color.blackOverlayFlash));
        flash.setEvaluator(eval);
        flash.setInterpolator(new AccelerateInterpolator());
        flash.setDuration(100);

        fade = ObjectAnimator.ofInt(this, "backgroundColor", getResources().getColor(R.color.blackOverlayFlash), getResources().getColor(R.color.blackOverlay));
        fade.setEvaluator(eval);
        fade.setInterpolator(new AccelerateDecelerateInterpolator());
        fade.setDuration(300);

        flashAnim = new AnimatorSet();
        flashAnim.playSequentially(flash, fade);
    }

    private BarcodeCallbacks barcodeCallbacks;

    public void setIndex(int index) {indexInParent = index; }

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

    public static ProductRatingBar fromProduct(Product p, Context c) {
        ProductRatingBar pbar = new ProductRatingBar(c);
        pbar.setProduct(p);
        pbar.showView(pbar.rating, false);

        return pbar;
    }

    public void setBarcodeCallback(BarcodeCallbacks callback) { barcodeCallbacks = callback; }

    public void setProduct(Product product) {
        state = States.SUCCESS;
        this.product = product;

        productName.setText(product.getName());
        productStars.setRating(product.getNumStars());

        int nReviews = product.getRatingCount();
        String reviews;
        if (nReviews == 0) {
            reviews = null;
        } else if (nReviews > 99) {
            reviews = "(99+)";
        } else {
            reviews = String.format("(%d)", nReviews);
        }
        productNumReviews.setText(reviews);
    }

    public void setState(States state) {
        switch(state) {
            case THANKS:
                displayStatus("Thanks for your help!", false, true);
                this.state = state;
                break;
            case UPLOADING:
                displayStatus("Uploading Image.", true, false);
                this.state = state;
                break;
        }
    }

    public void displayStatus(String statusString, boolean showProgress, boolean animated) {
        statusText.setText(statusString);
        progress.setVisibility(showProgress ? VISIBLE : GONE);
        if (!status.isShown() && isShown()) {
            showView(status, animated);
        } else {
            showView(status, false);
        }
    }

    private void showView(View newView, boolean animated) {
        if (animated) {
            if (displayedView != null) {
                displayedView.animate().alpha(0f).setDuration(duration).setListener(new hideOnEnd(displayedView)).start();
            }
            newView.setAlpha(0f);
            newView.setVisibility(VISIBLE);
            newView.animate().alpha(1f).setDuration(duration).start();
        } else {
            if (displayedView != null) {
                displayedView.setVisibility(INVISIBLE);
            }
            newView.setVisibility(VISIBLE);
        }
        displayedView = newView;
    }

    public void loadBarcode(final String barcode) {
        this.barcode = barcode;
        state = States.FETCHING;
        displayStatus("Loading Product", true, false);
        MainWindow.service.getProduct(barcode, new Callback<Product>() {
            @Override
            public void success(Product product, Response response) {
                if (product != null && product.published) {
                    setProduct(product);
                    showView(rating, true);
                } else {
                    state = States.UNKNOWN;
                    displayStatus("Unknown Product", false, true);
                    if (indexInParent == 0 && barcodeCallbacks != null) {
                        barcodeCallbacks.onUnknownBarcode(barcode);
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                state = States.ERROR;
                displayStatus("Error Loading Product", false, true);
            }
        });
    }



    private void init(Context context) {
        setBackgroundColor(getResources().getColor(R.color.blackOverlay));
        Utils.setPaddingDP(this, 8, 4, 8, 4);

        rating = defaultInflate(context, R.layout.rating_bar_contents);
        status = defaultInflate(context, R.layout.rating_bar_status);

        productName = ((TextView) rating.findViewById(R.id.productName));
//        productStars = ((RatingBar) rating.findViewById(R.id.productStars));
        productStars = ((Rater) rating.findViewById(R.id.productStars));
        productNumReviews = ((TextView) rating.findViewById(R.id.productNumReviews));

        statusText = ((TextView) status.findViewById(R.id.tvStatus));
        progress = ((ProgressBar) status.findViewById(R.id.pbLoading));

//        Bitmap b = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888);
//        Canvas c = new Canvas(b);
//        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
//        p.setColor(Color.GREEN);
//        c.drawCircle(15, 15, 15, p);
//        BitmapDrawable bd = new BitmapDrawable(context.getResources(), b);
//        b.setWidth(32);
//        Log.i("foo", String.format("%d %d %d", b.getWidth(), bd.getIntrinsicWidth(), bd.getBitmap().getWidth()));
//        productStars.setProgressDrawableTiled(bd);

//        productStars.setProgressDrawable();
//        productStars.setProgressDrawableTiled();
        setOnClickListener(this);
    }

    private View defaultInflate(Context context, int resource) {
        View view = inflate(context, resource, null);
        view.setVisibility(INVISIBLE);
        addView(view, defaultLayout);
        return view;
    }

    public final Product getProduct() {
        return product;
    }

    public String getBarcode() { return barcode; }

    public void flash() {
        if (    shouldFlash &&
                !flashAnim.isRunning() &&
                !(state == States.FETCHING) &&
                !(state == States.UPLOADING)) {
            shouldFlash = false;
            flashAnim.start();
            this.postDelayed(flashDelay, flashInterval);
        }
    }
}
