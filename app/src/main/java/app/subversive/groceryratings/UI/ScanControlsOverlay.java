package app.subversive.groceryratings.UI;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
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
    public interface Callbacks {
        public void onCaptureNewProductPhoto();
        public void onScanControlsFinishedHide();
        public void onScanControlsFinishedShow();
    }

    private long animDuration = 200;
    private long delayAmount = 30;
    private long delayUntilPrompt = 5000;

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

    private final Timer displayTimer = new Timer(true);

    private TimerTask getTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                Log.i("Timer", "Show Status");
            }
        };
    }

    public ScanControlsOverlay(Callbacks handler) { this.handler = handler; }

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
//        displayTimer.purge();
//        displayTimer.schedule(getTimerTask(), delayUntilPrompt);
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
        statusBar.setVisibility(View.VISIBLE);


        if (animStatusShow != null) {
            AnimatorSet anim = new AnimatorSet();
            AnimatorSet.Builder builder = anim.play(animStatusShow);

            addProductShowAnimation(builder);

            anim.setInterpolator(new AccelerateInterpolator());
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    handler.onScanControlsFinishedShow();
                }
            });
            anim.start();
        }
    }

    private void addProductShowAnimation(AnimatorSet.Builder builder) {
        if (hiddenRatings == null) {return;}
        for (int i = 0; i < hiddenRatings.length ; i++) {
            final View child = hiddenRatings[i];
            ObjectAnimator animator = ObjectAnimator.ofFloat(
                    child, "y",
                    child.getY(),
                    child.getTop());
            animator.setDuration(animDuration);
            animator.setStartDelay(i * delayAmount);
            builder.with(animator);
        }
        hiddenRatings = null;
    }

    private void addProductHideAnimation(AnimatorSet.Builder builder) {
        int nChildren = ratingHistory.getChildCount()-1;
        if (nChildren < 0) { return; }

        final int scrollAmount = historyScrollView.getScrollY();
        final int scrollBottom = historyScrollView.getBottom();
        final int animAmount = scrollBottom - ratingHistory.getChildAt(0).getTop() + scrollAmount;

        View child = ratingHistory.getChildAt(nChildren);
        Log.i("childY", String.valueOf(child.getY()));
        Log.i("childTop", String.valueOf(child.getTop()));
        while (child.getTop() - scrollAmount > scrollBottom) {
            nChildren--;
            child = ratingHistory.getChildAt(nChildren);
        }

        hiddenRatings = new View[nChildren+1];
        hiddenRatings[nChildren] = child;

        ObjectAnimator animator = ObjectAnimator.ofFloat(
                                        child, "y",
                                        child.getY(),
                                        child.getY() + animAmount);
        animator.setDuration(animDuration);
        builder.with(animator);
        long delay = delayAmount;
        for (int i = nChildren - 1; i >= 0 ; i--) {
            child = ratingHistory.getChildAt(i);
            animator = ObjectAnimator.ofFloat(
                    child, "y",
                    child.getY(),
                    child.getY() + animAmount);

            animator.setDuration(animDuration);
            animator.setStartDelay(delay);

            builder.with(animator);
            delay += delayAmount;
            hiddenRatings[i] = child;
        }
    }

    @Override
    public void hideOverlay(boolean withAnimation) {

        if (animStatusHide == null) {
            animStatusHide = ObjectAnimator.ofFloat(statusBar, "y", 0, -statusBar.getHeight());
            animStatusHide.setDuration(animDuration);
            animStatusHide.addListener(new AnimUtils.HideOnEnd(statusBar));

            animStatusShow = ObjectAnimator.ofFloat(statusBar, "y", -statusBar.getHeight(), 0);
            animStatusShow.setDuration(animDuration);
        }

        AnimatorSet anim = new AnimatorSet();
        AnimatorSet.Builder animBuilder = anim.play(animStatusHide);
        if (unknownBarcode.isShown()) {
            animBuilder.with(animUnknownCodeHide);
        }

        addProductHideAnimation(animBuilder);
        anim.setInterpolator(new AccelerateInterpolator());
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                historyScrollView.setVisibility(View.GONE);
                handler.onScanControlsFinishedHide();
            }
        });
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
}
