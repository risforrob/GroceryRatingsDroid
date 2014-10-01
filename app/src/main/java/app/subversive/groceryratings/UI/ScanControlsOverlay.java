package app.subversive.groceryratings.UI;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import app.subversive.groceryratings.Core.Product;
import app.subversive.groceryratings.R;

/**
 * Created by rob on 9/10/14.
 */
public class ScanControlsOverlay implements Overlay, ObservableScrollView.Callbacks {
    private final static String TAG = ScanControlsOverlay.class.getSimpleName();
    public interface Callbacks {
        public void onCaptureNewProductPhoto();
        public void onScanControlsFinishedHide();
        public void onScanControlsFinishedShow();
    }

    private long animDuration = 200;
    private long delayAmount = 30;


    FrameLayout parent;
    boolean attached, inflated;
    Callbacks handler;

    ObservableScrollView historyScrollView;
    LinearLayout statusBar, unknownBarcode;

    RatingsLayout ratingHistory;
    TextView statusText, btnPhotoNo, btnPhotoYes;

    private final ViewGroup.LayoutParams defaultLP =
            new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    ObjectAnimator animUnknownCodeHide,
                   animUnknownCodeShow,
                   animStatusHide,
                   animStatusShow;

    View[] hiddenRatings;

    private final HashMap<String, ProductRatingBar> cache = new HashMap<String, ProductRatingBar>();

    public ScanControlsOverlay(Callbacks handler) { this.handler = handler; }

    private long delayUntilPrompt = 5000;
    private final Handler timerHandler = new Handler();
    private long timeOfLastReset;
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long timeDiff = System.currentTimeMillis() - timeOfLastReset;

            if (timeDiff > delayUntilPrompt) {
                showScanPrompt();
            } else {
                timerHandler.postDelayed(this, timeDiff);
            }
        }
    };

    @Override
    public void attachOverlayToParent(FrameLayout parent) {
        this.parent = parent;
        if (attached) {throw new RuntimeException("Overlay is already attached to a parent"); }

        if (!inflated) {
            inflateOverlay();
            inflated = true;
        } else {
            parent.addView(historyScrollView);
            parent.addView(statusBar);
            parent.addView(unknownBarcode);
        }

//        displayTimer.schedule(getTimerTask(), delayUntilPrompt);
    }

    public void resetPromptTimer() {
        timeOfLastReset = System.currentTimeMillis();
        if (statusBar.isShown()) {
            hideScanPrompt();
        }
    }

    public void cancelTimer() {
        timerHandler.removeCallbacks(timerRunnable);
    }

    public void startTimer() {
        timerHandler.postDelayed(timerRunnable, delayUntilPrompt);
    }

    public void showScanPrompt() {
        statusBar.setVisibility(View.VISIBLE);
        if (animStatusShow == null) {
            animStatusShow = ObjectAnimator.ofFloat(statusBar, "y", -statusBar.getHeight(), 0);
            animStatusShow.setDuration(animDuration);
        }
        animStatusShow.start();
        Log.i(TAG, "ShowScanPrompt");
    }

    public void hideScanPrompt() {
        if (animStatusHide == null) {
            animStatusHide = ObjectAnimator.ofFloat(statusBar, "y", 0, -statusBar.getHeight());
            animStatusHide.setDuration(animDuration);
            animStatusHide.addListener(new AnimUtils.HideOnEnd(statusBar));
        }
        animStatusHide.start();
        startTimer();
    }

    private void inflateOverlay() {
        LayoutInflater.from(parent.getContext()).inflate(R.layout.scan_barcode_overlay, parent, true);
        historyScrollView = (ObservableScrollView) parent.findViewById(R.id.scrollView);
        ratingHistory = (RatingsLayout) parent.findViewById(R.id.RatingHolder);
        statusBar = (LinearLayout) parent.findViewById(R.id.statusBar);
        unknownBarcode = (LinearLayout) parent.findViewById(R.id.unknownBarcode);
        statusText = (TextView) parent.findViewById(R.id.statusText);
        btnPhotoNo = (TextView) parent.findViewById(R.id.tvNoScanBarcode);
        btnPhotoYes = (TextView) parent.findViewById(R.id.tvYesScanBarcode);

        historyScrollView.addCallbacks(this);

        btnPhotoNo.setOnClickListener(noPhotoListener);
        btnPhotoYes.setOnClickListener(yesPhotoListener);

        historyScrollView.setVisibility(View.INVISIBLE);
        statusBar.setVisibility(View.INVISIBLE);


        animUnknownCodeHide = ObjectAnimator.ofFloat(unknownBarcode, "x", 0f, -parent.getResources().getDisplayMetrics().widthPixels);
        animUnknownCodeHide.setDuration(animDuration);
        animUnknownCodeHide.addListener(new AnimUtils.HideOnEnd(unknownBarcode));

        animUnknownCodeShow = ObjectAnimator.ofFloat(unknownBarcode,"x", parent.getResources().getDisplayMetrics().widthPixels, 0f);
        animUnknownCodeShow.setDuration(animDuration);


    }

    @Override
    public void showOverlay(boolean withAnimation) {

        historyScrollView.setVisibility(View.VISIBLE);

        if (withAnimation) {
            ObjectAnimator[] animators = addProductShowAnimation();

            AnimatorSet anim = new AnimatorSet();
            anim.setInterpolator(new AccelerateInterpolator());
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    handler.onScanControlsFinishedShow();
                }
            });
            anim.playTogether(animators);
            anim.start();
        }
    }

    private ObjectAnimator[] addProductShowAnimation() {
        ObjectAnimator[] animators = new ObjectAnimator[hiddenRatings.length];

        for (int i = 0; i < hiddenRatings.length ; i++) {
            final View child = hiddenRatings[i];
            ObjectAnimator animator = ObjectAnimator.ofFloat(
                    child, "y",
                    child.getY(),
                    child.getTop());
            animator.setDuration(animDuration);
            animator.setStartDelay(i * delayAmount);
            animators[i] = animator;
        }
        hiddenRatings = null;
        return animators;
    }

    private ObjectAnimator[] addProductHideAnimation() {
        int nChildren = ratingHistory.getChildCount()-1;
        if (nChildren < 0) { return null; }

        final int scrollAmount = historyScrollView.getScrollY();
        final int scrollBottom = historyScrollView.getBottom();
        final int animAmount = scrollBottom - ratingHistory.getChildAt(0).getTop() + scrollAmount;

        View child = ratingHistory.getChildAt(nChildren);
        while (child.getTop() - scrollAmount > scrollBottom) {
            nChildren--;
            child = ratingHistory.getChildAt(nChildren);
        }

        hiddenRatings = new View[nChildren+1];
        hiddenRatings[nChildren] = child;
        ObjectAnimator[] animators = new ObjectAnimator[nChildren+1];

                ObjectAnimator animator = ObjectAnimator.ofFloat(
                                        child, "y",
                                        child.getY(),
                                        child.getY() + animAmount);
        animator.setDuration(animDuration);
        animators[0] = animator;

        long delay = delayAmount;
        for (int i = 1; i <= nChildren ; i++) {
            child = ratingHistory.getChildAt(nChildren - i);
            animator = ObjectAnimator.ofFloat(
                    child, "y",
                    child.getY(),
                    child.getY() + animAmount);

            animator.setDuration(animDuration);
            animator.setStartDelay(delay);
            animators[i] = animator;
            delay += delayAmount;
            hiddenRatings[nChildren-i] = child;
        }
        return animators;
    }

    @Override
    public void hideOverlay(boolean withAnimation) {
        cancelTimer();

        AnimatorSet anim = new AnimatorSet();
        ObjectAnimator[] animators = addProductHideAnimation();
        anim.setInterpolator(new AccelerateInterpolator());
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                historyScrollView.setVisibility(View.GONE);
                handler.onScanControlsFinishedHide();
            }
        });

        if (unknownBarcode.isShown()) {
            AnimatorSet.Builder builder = anim.play(animUnknownCodeHide);
            if (animators != null) {
                for (ObjectAnimator a : animators) {
                    builder.with(a);
                }
            }
        } else if (animators != null) {
            anim.playTogether(animators);
        }
        anim.start();
    }

    @Override
    public void detachOverlayFromParent() {
        parent.removeView(historyScrollView);
        parent.removeView(statusBar);
        parent.removeView(unknownBarcode);

        parent = null;
        attached = false;
    }

    View.OnClickListener yesPhotoListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            handler.onCaptureNewProductPhoto();
        }
    };

    View.OnClickListener noPhotoListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            hideUnknownBarcode(true);
        }
    };

    public void showUnknownBarcode(boolean withAnimation) {
        if (animUnknownCodeShow.isRunning()) {
            return;
        } else if (animUnknownCodeHide.isRunning()) {
            return;
        } else {
            unknownBarcode.setVisibility(View.VISIBLE);
            animUnknownCodeShow.start();
        }
    }

    public void hideUnknownBarcode(boolean withAnimation) {
        animUnknownCodeHide.start();
    }

    private void moveProductBarToTop(ProductRatingBar pbar) {
        ratingHistory.removeView(pbar);
        ratingHistory.addView(pbar, 0);
        updateIndicies();
    }

    public void addNewProductBar(String barcode, ProductRatingBar pbar) {
        cache.put(barcode, pbar);

        int numChildren = ratingHistory.getChildCount();
        int maxChildren = ratingHistory.maxChildren;

        for (int i = maxChildren-1 ; i < numChildren ; i++ ) {
            ProductRatingBar child = (ProductRatingBar) ratingHistory.getChildAt(i);
            child.setIndex(-1);
            ratingHistory.removeView(child);
            cache.remove(child.getBarcode());
        }

        ratingHistory.addView(pbar, 0, defaultLP);
        updateIndicies();
    }

    private void updateIndicies() {
        int numChildren = ratingHistory.getChildCount();

        for (int i = 0; i < numChildren ; i++) {
            ((ProductRatingBar) ratingHistory.getChildAt(i)).setIndex(i);
        }
    }

    public void addNewRating(String barcode, ProductRatingBar.BarcodeCallbacks callback) {
        ProductRatingBar pbar = cache.get(barcode);
        if (pbar != null) {
            moveProductBarToTop(pbar);
        } else {
            pbar = new ProductRatingBar(parent.getContext());
            pbar.setBarcodeCallback(callback);
            pbar.loadBarcode(barcode);
            addNewProductBar(barcode, pbar);
        }
    }

    @Override
    public void onScrollChanged(int deltaX, int deltaY) {
        hideUnknownBarcode(true);
    }

    public void scrollHistoryToBeginning() { historyScrollView.smoothScrollTo(0,0); }

    public final ProductRatingBar getProductBar(int index) {
        if (index < ratingHistory.getChildCount()) {
            return ((ProductRatingBar) ratingHistory.getChildAt(index));
        }   else {
            return null;
        }
    }

    public final List<Product> getAllProducts() {
        LinkedList<Product> products = new LinkedList<Product>();
        int numChildren = ratingHistory.getChildCount();
        Product p;
        for (int i = 0 ; i < numChildren ; i++) {
            p = ((ProductRatingBar) ratingHistory.getChildAt(i)).getProduct();
            if (p != null) {
                products.add(p);
            }
        }
        return products;
    }

    public void flashTop() {
        if (!unknownBarcode.isShown()) {
            getProductBar(0).flash();
        }
    }
}
