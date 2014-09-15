package app.subversive.groceryratings.UI;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

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

    FrameLayout parent;
    boolean attached, inflated;
    Callbacks handler;

    ObservableScrollView historyScrollView;
    LinearLayout statusBar, unknownBarcode;

    RatingsLayout ratingHistory;
    ProgressBar progressBar;
    TextView statusText, btnPhotoNo, btnPhotoYes;

    private final ViewGroup.LayoutParams defaultLP =
            new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    ObjectAnimator animUnknownCodeHide,
                   animUnknownCodeShow,
                   animStatusHide,
                   animStatusShow;

    View[] hiddenRatings;

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
    }

    private void inflateOverlay() {
        LayoutInflater.from(parent.getContext()).inflate(R.layout.scan_barcode_overlay, parent, true);
        historyScrollView = (ObservableScrollView) parent.findViewById(R.id.scrollView);
        ratingHistory = (RatingsLayout) parent.findViewById(R.id.RatingHolder);
        statusBar = (LinearLayout) parent.findViewById(R.id.statusBar);
        unknownBarcode = (LinearLayout) parent.findViewById(R.id.unknownBarcode);
        progressBar = (ProgressBar) parent.findViewById(R.id.progressBar);
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
            animator.setStartDelay(i*delayAmount);
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
//        animator.addListener(new AnimUtils.HideOnEnd(historyScrollView));
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
        unknownBarcode.setVisibility(View.VISIBLE);
        animUnknownCodeShow.start();
    }



    public void hideUnknownBarcode(boolean withAnimation) {
        animUnknownCodeHide.start();
    }

    public void addNewRating(Product product) {
        ProductRatingBar pbar = new ProductRatingBar(parent.getContext());
        pbar.setProduct(product);
        ratingHistory.addView(pbar, 0, defaultLP);
    }

    public void setStatusText(String text, boolean showProgress) {
        statusText.setText(text);
        progressBar.setVisibility(showProgress ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onScrollChanged(int deltaX, int deltaY) {
        hideUnknownBarcode(true);
    }

    public void scrollHistoryToBeginning() { historyScrollView.smoothScrollTo(0,0); }

}