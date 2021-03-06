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
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import app.subversive.groceryratings.Core.Variant;
import app.subversive.groceryratings.Core.VariantLoader;
import app.subversive.groceryratings.R;
import app.subversive.groceryratings.Utils;


/**
 * Created by rob on 9/1/14.
 */
public class ProductRatingBar extends FrameLayout {
    static class hideOnEnd extends AnimatorListenerAdapter {
        View v;

        public hideOnEnd(View v) {
            this.v = v;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
                v.setVisibility(INVISIBLE);
                v.setAlpha(1f);
//                v = null;
                animation.removeAllListeners();
        }
    }

    public enum States {FETCHING, UNKNOWN, SUCCESS, PHOTO, ERROR, THANKS, UPLOADING, CREATED}

    private States state;

    final long duration = 200;

    TextView productName, productNumReviews, statusText;
    Rater productStars;
    ProgressBar progress;

    View rating, status, displayedView;
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

    private boolean shouldFlash = true;
    private long flashInterval = 2000L;
    private final Runnable flashDelay = new Runnable() {
        @Override
        public void run() {
            shouldFlash = true;
        }
    };

    final static LayoutParams defaultLayout = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

    static {
        defaultLayout.gravity = Gravity.CENTER;
    }

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

    public void setLoader(VariantLoader loader) {
        setState(States.FETCHING, false);
        mLoader = loader;
        mLoader.registerObserver(mObserver);
        onLoaderUpdated();
    }

    public void setVariant(Variant variant) {
        setState(States.SUCCESS, false);

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

    private void setState(States state, boolean animateToState) {
        this.state = state;
        switch (state) {
            case UNKNOWN:
                displayStatus("Unknown Product", false, animateToState);
                break;
            case FETCHING:
                displayStatus("Loading Product", true, animateToState);
                break;
            case THANKS:
                displayStatus("Thanks for your help!", false, animateToState);
                break;
            case UPLOADING:
                displayStatus("Uploading Image.", true, animateToState);
                break;
            case ERROR:
                displayStatus("Error Loading Product", false, animateToState);
                break;
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
        if (displayedView == rating) {
            return;
        } else if (animated) {
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


    private void init(Context context) {
        setBackgroundResource(R.drawable.pbar_background);
        Utils.setPaddingDP(this, 8, 4, 8, 4);

        rating = defaultInflate(context, R.layout.rating_bar_contents);
        status = defaultInflate(context, R.layout.rating_bar_status);

        productName = ((TextView) rating.findViewById(R.id.productName));
        productStars = ((Rater) rating.findViewById(R.id.productStars));
        productNumReviews = ((TextView) rating.findViewById(R.id.productNumReviews));

        statusText = ((TextView) status.findViewById(R.id.tvStatus));
        progress = ((ProgressBar) status.findViewById(R.id.pbLoading));

        state = States.CREATED;
    }

    private View defaultInflate(Context context, int resource) {
        View view = inflate(context, resource, null);
        view.setVisibility(INVISIBLE);
        addView(view, defaultLayout);
        return view;
    }

    public void flash() {
        Log.d("FLASH", String.valueOf(shouldFlash));
        Log.d("FLASH", String.valueOf(flashAnim.isRunning()));
        Log.d("FLASH", String.valueOf(state == States.FETCHING));
        Log.d("FLASH", String.valueOf(state == States.UPLOADING));
        Log.d("FLASH", String.valueOf(state == States.CREATED));

        if (shouldFlash &&
                !flashAnim.isRunning() &&
                (state != States.FETCHING) &&
                (state != States.UPLOADING) &&
                (state != States.CREATED)) {
            shouldFlash = false;
            flashAnim.start();
            this.postDelayed(flashDelay, flashInterval);
        }
    }

    private void onLoaderUpdated() {
        switch (mLoader.getState()) {
            case LOADED:
                if (mLoader.getVariant() == null) {
                    setState(States.UNKNOWN, true);
                } else {
                    setVariant(mLoader.getVariant());
                }
                break;
            case FETCHING:
                setState(States.FETCHING, false);
                break;
            case ERROR:
                setState(States.ERROR, true);
                break;
            case PROGRESS_IMAGE:
                setState(States.UPLOADING, true);
                break;
            case SUCCESS_ADD:
                setState(States.THANKS, true);
                break;
        }
    }
}

