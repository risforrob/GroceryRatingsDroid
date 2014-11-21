package app.subversive.groceryratings.UI;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import app.subversive.groceryratings.Core.Product;
import app.subversive.groceryratings.ManagedTimer;
import app.subversive.groceryratings.R;
import app.subversive.groceryratings.ScanFragment;

/**
 * Created by rob on 9/10/14.
 */
public class ScanControlsOverlay implements Overlay, ObservableScrollView.Callbacks {
    private final static String TAG = ScanControlsOverlay.class.getSimpleName();
    public interface Callbacks {
        public void onCaptureNewProductPhoto();
        public void onScanControlsFinishedHide();
        public void onScanControlsFinishedShow();
        public void onTouchUp(float x, float y);
    }

    private long animDuration = 200;
    private long delayAmount = 30;

    private int touchOffsetX, touchOffsetY;


    FrameLayout parent;
    boolean attached, inflated;
    Callbacks handler;

    ObservableScrollView historyScrollView;
    LinearLayout unknownBarcode;
    TextView statusBar;
    RatingsLayout ratingHistory;

    StatusManager mStatusManager;
    private final ViewGroup.LayoutParams defaultLP =
            new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    View[] hiddenRatings;

    StatusManager.Status statusPrompt, statusUnknown;

    private final HashMap<String, ProductRatingBar> cache = new HashMap<String, ProductRatingBar>();
    public ScanControlsOverlay(Callbacks handler) { this.handler = handler; }

    ManagedTimer.RunnableController controller = ManagedTimer.getController(new Runnable() {
        @Override
        public void run() {
            showScanPrompt();
        }
    }, 10000L);

    public void setTouchOffset(int x, int y) {
        touchOffsetX = x;
        touchOffsetY = y;
    }

    @Override
    public void attachOverlayToParent(FrameLayout parent) {
        this.parent = parent;
        if (attached) {throw new RuntimeException("Overlay is already attached to a parent"); }

        if (!inflated) {
            inflateOverlay();
            inflated = true;

        } else {
            parent.addView(historyScrollView);
        }

        mStatusManager = new StatusManager(parent);
        statusPrompt = mStatusManager.createStatus(statusBar);
        statusUnknown = mStatusManager.createStatus(unknownBarcode);
    }

    @Override
    public void onParentLayoutComplete() {
        ratingHistory.setMinimumHeight(parent.getHeight());
//        ratingHistory.invalidate();
        setupAnimation();
    }

    private void setupAnimation() {

    }

    public void resetPromptTimer() {
        controller.restart();
        statusPrompt.hide(true);
    }

    public void cancelTimer() {
        controller.cancel();
    }

    public void startTimer() {
        controller.restart();
    }

    public void showScanPrompt() {
        statusPrompt.show(true);
        Log.i(TAG, "ShowScanPrompt");
    }

    private void inflateOverlay() {
        LayoutInflater.from(parent.getContext()).inflate(R.layout.scan_barcode_overlay, parent, true);
        historyScrollView = (ObservableScrollView) parent.findViewById(R.id.scrollView);
        ratingHistory = (RatingsLayout) parent.findViewById(R.id.RatingHolder);
        historyScrollView.addCallbacks(this);
        historyScrollView.setVisibility(View.INVISIBLE);



        statusBar = (TextView) LayoutInflater.from(parent.getContext()).inflate(R.layout.status_scan_prompt, parent, false);
        statusBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });



        unknownBarcode = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.unknown_barcode, parent, false);
        TextView btnPhotoNo = (TextView) unknownBarcode.findViewById(R.id.tvNoScanBarcode);
        TextView btnPhotoYes = (TextView) unknownBarcode.findViewById(R.id.tvYesScanBarcode);
        btnPhotoNo.setOnClickListener(noPhotoListener);
        btnPhotoYes.setOnClickListener(yesPhotoListener);
        unknownBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public void showOverlay(boolean withAnimation) {
        startTimer();
        historyScrollView.setVisibility(View.VISIBLE);

        if (withAnimation) {

            AnimatorSet anim = new AnimatorSet();
            anim.setInterpolator(new AccelerateInterpolator());
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    handler.onScanControlsFinishedShow();
                }
            });
            anim.playTogether(addProductShowAnimation());
            anim.start();
        } else {
            if (hiddenRatings != null) {
                for (View v : hiddenRatings) {
                    v.setTranslationY(0);
                }
            }
            historyScrollView.scrollTo(0,0);
        }
    }

    private List<Animator> addProductShowAnimation() {
        LinkedList<Animator> animators = new LinkedList<Animator>();

        if (hiddenRatings == null || hiddenRatings.length == 0) {
            return animators;
        }

        for (int i = 0; i < hiddenRatings.length ; i++) {
            final View child = hiddenRatings[i];
            ObjectAnimator animator = ObjectAnimator.ofFloat(
                    child, "y",
                    child.getY(),
                    child.getTop());
            animator.setDuration(animDuration);
            animator.setStartDelay(i * delayAmount);
            animators.add(animator);
        }
        hiddenRatings = null;
        return animators;
    }

    private List<Animator> addProductHideAnimation() {
        LinkedList<Animator> animators = new LinkedList<Animator>();
        int nChildren = ratingHistory.getChildCount()-1;
        if (nChildren < 0) { return animators; }

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

                ObjectAnimator animator = ObjectAnimator.ofFloat(
                                        child, "y",
                                        child.getY(),
                                        child.getY() + animAmount);
        animator.setDuration(animDuration);
        animators.add(animator);

        long delay = delayAmount;
        for (int i = 1; i <= nChildren ; i++) {
            child = ratingHistory.getChildAt(nChildren - i);
            animator = ObjectAnimator.ofFloat(
                    child, "y",
                    child.getY(),
                    child.getY() + animAmount);

            animator.setDuration(animDuration);
            animator.setStartDelay(delay);
            animators.add(animator);
            delay += delayAmount;
            hiddenRatings[nChildren-i] = child;
        }
        return animators;
    }

    @Override
    public void hideOverlay(boolean withAnimation) {
        cancelTimer();

        AnimatorSet anim = new AnimatorSet();
//        ObjectAnimator[] animators = addProductHideAnimation();
        anim.setInterpolator(new AccelerateInterpolator());
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                historyScrollView.setVisibility(View.GONE);
                handler.onScanControlsFinishedHide();
            }
        });

        LinkedList<Animator> animators = new LinkedList<Animator>();
        Animator mStatusAnim = mStatusManager.getStatusHideAnimator();
        if (mStatusAnim != null) {
            animators.add(mStatusAnim);
        }
        animators.addAll(addProductHideAnimation());

        anim.playTogether(animators);
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
        resetPromptTimer();
        statusUnknown.show(withAnimation);
    }

    public void hideUnknownBarcode(boolean withAnimation) {
        statusUnknown.hide(withAnimation);
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

    @Override
    public void onTouchUp(float x, float y) { handler.onTouchUp(x + touchOffsetX, y + touchOffsetY); }

    public void scrollHistoryToBeginning() { historyScrollView.smoothScrollTo(0,0); }

    public final ProductRatingBar getProductBar(int index) {
        if (index < ratingHistory.getChildCount()) {
            return ((ProductRatingBar) ratingHistory.getChildAt(index));
        }   else {
            return null;
        }
    }

    public void flushHistory() {
        ratingHistory.removeAllViews();
    }

    public List<Product> getAllProducts() {
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
