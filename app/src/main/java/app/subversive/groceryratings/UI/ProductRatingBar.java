package app.subversive.groceryratings.UI;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import app.subversive.groceryratings.Adapters.TemplateAdapter;
import app.subversive.groceryratings.Core.Variant;
import app.subversive.groceryratings.Core.VariantLoader;
import app.subversive.groceryratings.R;
import app.subversive.groceryratings.Utils;


/**
 * Created by rob on 9/1/14.
 */
public class ProductRatingBar extends FrameLayout implements View.OnClickListener {

    public static class Binder implements TemplateAdapter.ViewBinder<VariantLoader> {

        @Override
        public View inflateView(ViewGroup parent) {
            return new ProductRatingBar(parent.getContext());
        }

        @Override
        public void bindView(View view, VariantLoader loader) {
            ProductRatingBar pbar = (ProductRatingBar) view;
            pbar.setLoader(loader);
            if (loader.getVariant() != null) {
                pbar.setVariant(loader.getVariant());
            }
        }
    }

    VariantLoader mLoader;
    DataSetObserver mObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            onLoaderUpdated();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
        }
    };

    private void onLoaderUpdated() {
        if (mLoader.getVariant() != null) {
            // valid product
            setVariant(mLoader.getVariant());
        } else {
            // no product
            switch (mLoader.getState()) {
                case LOADED:
                    setState(States.UNKNOWN, true);
                    showUnknown();
                    break;
                case FETCHING:
                    setState(States.FETCHING, false);
                    break;
                case ERROR:
                    setState(States.ERROR, true);
                    break;

            }
        }
    }

    @Override
    public void onClick(View v) {
        if (mLoader.getVariant() != null && loadRatingsCB != null) {
//            loadRatingsCB.onLoadRatingDetails(indexInParent);
        }
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

    public interface LoadRatingDetailsCallback {
        void onLoadRatingDetails(int index);
    }


    public enum States {FETCHING, UNKNOWN, SUCCESS, PHOTO, ERROR, THANKS, UPLOADING}
    private States state;

    final long duration = 200;
//    private Variant variant;
//    private String barcode;


    TextView productName, productNumReviews, statusText;
    Rater productStars;
    ProgressBar progress;

    View rating, status, displayedView;

//    int indexInParent;


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
    private LoadRatingDetailsCallback loadRatingsCB;

//    public void setIndex(int index) {indexInParent = index; }

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

//    public static ProductRatingBar fromProduct(Variant p, Context c) {
//        ProductRatingBar pbar = new ProductRatingBar(c);
//        pbar.setVariant(p);
//        pbar.showView(pbar.rating, false);
//        return pbar;
//    }

//    public void setBarcodeCallback(BarcodeCallbacks callback) { barcodeCallbacks = callback; }
    public void setRatingDetailsCallback(LoadRatingDetailsCallback callback) { loadRatingsCB = callback; }

    public void setLoader(VariantLoader loader) {
        setState(States.FETCHING, false);
        mLoader = loader;
        mLoader.registerObserver(mObserver);
    }

    public void setVariant(Variant variant) {
        state = States.SUCCESS;

        productName.setText(variant.getName());
        productStars.setRating(variant.getNumStars());

        int nReviews = variant.getRatingCount();
        String reviews;
        if (nReviews == 0) {
            reviews = null;
        } else if (nReviews > 99) {
            reviews = "(99+)";
        } else {
            reviews = String.format("(%d)", nReviews);
        }
        productNumReviews.setText(reviews);
        showView(rating, true);
    }

    public void setState(States state, boolean animateToState) {
        switch(state) {
            case UNKNOWN:
                displayStatus("Unknown Product", false, animateToState);
                break;
            case FETCHING:
                displayStatus("Loading Product", true, animateToState);
                break;
            case THANKS:
                displayStatus("Thanks for your help!", false, animateToState);
                this.state = state;
                break;
            case UPLOADING:
                displayStatus("Uploading Image.", true, animateToState);
                this.state = state;
                break;
            case ERROR:
                displayStatus("Error Loading Product", false, animateToState);
                this.state = state;
                break;
        }
    }

    private void showUnknown() {
        if (barcodeCallbacks != null) {
            barcodeCallbacks.onUnknownBarcode(mLoader.getBarcode());
        }
    }

    private void displayStatus(String statusString, boolean showProgress, boolean animated) {
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

//    private void loadBarcode(final String barcode) {
//        this.barcode = barcode;
//        state = States.FETCHING;
//        displayStatus("Loading Product", true, false);
//        GRClient.getService().getProduct(barcode, new Callback<Variant>() {
//            @Override
//            public void success(Variant variant, Response response) {
//                if (variant != null && variant.published) {
//                    setVariant(variant);
//                    showView(rating, true);
//                } else {
//                    state = States.UNKNOWN;
//                    displayStatus("Unknown Product", false, true);
//                    if (indexInParent == 0 && barcodeCallbacks != null) {
//                        barcodeCallbacks.onUnknownBarcode(barcode);
//                    }
//                }
//            }
//
//            @Override
//            public void failure(RetrofitError error) {
//                state = States.ERROR;
//                displayStatus("Error Loading Product", false, true);
//            }
//        });
//    }



    private void init(Context context) {
        setBackgroundColor(getResources().getColor(R.color.blackOverlay));
        Utils.setPaddingDP(this, 8, 4, 8, 4);

        rating = defaultInflate(context, R.layout.rating_bar_contents);
        status = defaultInflate(context, R.layout.rating_bar_status);

        productName = ((TextView) rating.findViewById(R.id.productName));
        productStars = ((Rater) rating.findViewById(R.id.productStars));
        productNumReviews = ((TextView) rating.findViewById(R.id.productNumReviews));

        statusText = ((TextView) status.findViewById(R.id.tvStatus));
        progress = ((ProgressBar) status.findViewById(R.id.pbLoading));

        setOnClickListener(this);
    }

    private View defaultInflate(Context context, int resource) {
        View view = inflate(context, resource, null);
        view.setVisibility(INVISIBLE);
        addView(view, defaultLayout);
        return view;
    }

//    public final Variant getVariant() {
//        return variant;
//    }

//    public String getBarcode() { return barcode; }

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
