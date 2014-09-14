package app.subversive.groceryratings.UI;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Rect;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import app.subversive.groceryratings.R;

/**
 * Created by rob on 9/8/14.
 */
public class CameraControlsOverlay implements Overlay {
    public interface Callbacks {
        public void onTakePicture();
        public void onConfirmPicture();
        public void onRetryPicture();
        public void onCancelPicture();
        public void onCameraControlsFinishedShow();
        public void onCameraControlsFinishedHide();
    }

    private final long animDuration =200;
    private final long animDelay = 50;

    private final long[][] DELAYS = {{0, 1, 2},{1,0,1}};

    private FrameLayout parent;
    private boolean inflated, attached;
    private ImageButton cancelButton, captureButton, retryButton;
    private Callbacks handler;

    ObjectAnimator
            animCancelBtnShow,
            animCancelBtnHide,
            animCaptureBtnHide,
            animCaptureBtnShow,
            animRetryBtnShow,
            animRetryBtnHide;

    boolean animInitialized;
    private int buttonClickIndex;

    public CameraControlsOverlay(Callbacks handler) {
        this.handler = handler;
    }

    @Override
    public void attachOverlayToParent(FrameLayout parent) {
        this.parent = parent;
        if (attached) {throw new RuntimeException("Overlay is already attached to a parent"); }

        if (!inflated) {
            inflateOverlay();
            inflated = true;
        } else {
            parent.addView(cancelButton);
            parent.addView(captureButton);
            parent.addView(retryButton);
        }
    }

    private void inflateOverlay() {
        LayoutInflater.from(parent.getContext()).inflate(R.layout.caputure_photo_overlay, parent, true);
        cancelButton = (ImageButton) parent.findViewById(R.id.btnCancelTakePicture);
        captureButton = (ImageButton) parent.findViewById(R.id.btnTakePicture);
        retryButton = (ImageButton) parent.findViewById(R.id.btnRetakePicture);

        cancelButton.setOnClickListener(cancelPictureListener);
        captureButton.setOnClickListener(takePictureListener);
        retryButton.setOnClickListener(retryPictureListener);

        cancelButton.setVisibility(View.INVISIBLE);
        captureButton.setVisibility(View.INVISIBLE);
        retryButton.setVisibility(View.INVISIBLE);
    }

    private void initAnim() {
        animInitialized = true;
        float parentHeight = parent.getHeight();

        Log.i("top", String.valueOf(cancelButton.getTop()));
        Log.i("parentHeight", String.valueOf(parentHeight));

        animCancelBtnShow = ObjectAnimator.ofFloat(cancelButton, "y", parentHeight, cancelButton.getTop());
        animCancelBtnShow.setDuration(animDuration);
        animCancelBtnShow.setStartDelay(animDelay);
        animCancelBtnShow.addListener(new AnimUtils.ShowOnStart(cancelButton));

        animCancelBtnHide = ObjectAnimator.ofFloat(cancelButton, "y", cancelButton.getTop(), parentHeight);
        animCancelBtnHide.setDuration(animDuration);
        animCancelBtnHide.addListener(new AnimUtils.HideOnEnd(cancelButton));

        animCaptureBtnShow = ObjectAnimator.ofFloat(captureButton, "y", parentHeight, captureButton.getTop());
        animCaptureBtnShow.setDuration(animDuration);

        animCaptureBtnHide = ObjectAnimator.ofFloat(captureButton, "y", captureButton.getTop(), parentHeight);
        animCaptureBtnHide.setDuration(animDuration);
        animCaptureBtnHide.addListener(new AnimUtils.HideOnEnd(captureButton));

        animRetryBtnShow = ObjectAnimator.ofFloat(retryButton, "y", parentHeight, retryButton.getTop());
        animRetryBtnShow.setDuration(animDuration);

        animRetryBtnHide = ObjectAnimator.ofFloat(retryButton, "y", retryButton.getTop(), parentHeight);
        animRetryBtnHide.setDuration(animDuration);
        animRetryBtnHide.addListener(new AnimUtils.HideOnEnd(retryButton));
    }


    @Override
    public void showOverlay(boolean withAnimation) {
        setCaptureState(false);
        if (!withAnimation) {
            cancelButton.setVisibility(View.VISIBLE);
            captureButton.setVisibility(View.VISIBLE);
        } else {
            if (!animInitialized) {
                initAnim();
            }
            captureButton.setVisibility(View.VISIBLE);
            retryButton.setVisibility(View.GONE);

            AnimatorSet anim = new AnimatorSet();
            anim.play(animCaptureBtnShow).with(animCancelBtnShow);
            anim.setInterpolator(new DecelerateInterpolator());
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    handler.onCameraControlsFinishedShow();
                }
            });
            anim.start();
        }
    }

    @Override
    public void hideOverlay(boolean withAnimation) {
        if (!withAnimation) {
            cancelButton.setVisibility(View.GONE);
            captureButton.setVisibility(View.GONE);
            retryButton.setVisibility(View.GONE);
        } else {
            if (!animInitialized) {
                initAnim();
            }

            animCancelBtnHide.setStartDelay(DELAYS[buttonClickIndex][0] * animDelay);
            animCaptureBtnHide.setStartDelay(DELAYS[buttonClickIndex][1] * animDelay);
            animRetryBtnHide.setStartDelay(DELAYS[buttonClickIndex][2] * animDelay);

            AnimatorSet anim = new AnimatorSet();
            anim.setInterpolator(new AccelerateInterpolator());
            AnimatorSet.Builder builder;
            if (buttonClickIndex == 0) {
                builder = anim.play(animCancelBtnHide).with(animCaptureBtnHide);
            } else {
                builder = anim.play(animCaptureBtnHide).with(animCancelBtnHide);
            }

            if (retryButton.isShown()) {
                builder.with(animRetryBtnHide);
            }


            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    handler.onCameraControlsFinishedHide();
                    animRetryBtnHide.setStartDelay(0);
                }
            });
            anim.start();
        }
    }

    @Override
    public void detachOverlayFromParent() {
        hideOverlay(false);

        parent.removeView(cancelButton);
        parent.removeView(captureButton);
        parent.removeView(retryButton);
        parent = null;
        attached = false;
    }

    public void setCaptureState(boolean withAnimation) {
        setCameraIconTakePicture(withAnimation);
        hideRetryButton(withAnimation);
    }

    public void setConfirmState(boolean withAnimation) {
        setCameraIconConfirm(withAnimation);
        showRetryButton(withAnimation);
    }

    private void setCameraIconTakePicture(boolean withAnimation) {
        captureButton.setBackgroundResource(R.drawable.circle);
        captureButton.setImageResource(R.drawable.ic_action_camera);
        captureButton.setOnClickListener(takePictureListener);
    }

    private void setCameraIconConfirm(boolean withAnimation) {
        captureButton.setBackgroundResource(R.drawable.circle_green);
        captureButton.setImageResource(R.drawable.ic_action_accept);
        captureButton.setOnClickListener(confirmPictureListener);
    }

    private void hideRetryButton(boolean withAnimation) {
        if (withAnimation) {
            animRetryBtnHide.start();
        } else {
            retryButton.setVisibility(View.GONE);
        }
    }

    private void showRetryButton(boolean withAnimation) {
        retryButton.setVisibility(View.VISIBLE);
        if (withAnimation) {
            animRetryBtnShow.start();
        }
    }

    private final View.OnClickListener cancelPictureListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onCancelPicture();
        }
    };

    private final View.OnClickListener takePictureListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onTakePicture();
        }
    };

    private final View.OnClickListener retryPictureListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onRetryPicture();
        }
    };

    private final View.OnClickListener confirmPictureListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onConfirmPicture();
        }
    };

    private void onCancelPicture() {
        buttonClickIndex = 0;
        handler.onCancelPicture();
    }

    private void onTakePicture() {
        buttonClickIndex = 1;
        handler.onTakePicture();
    }

    private void onRetryPicture() {
        setCaptureState(true);
        handler.onRetryPicture();
    }

    private void onConfirmPicture() {
        buttonClickIndex = 1;
        handler.onConfirmPicture();
    }
}
