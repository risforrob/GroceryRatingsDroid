package app.subversive.groceryratings.UI;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import app.subversive.groceryratings.R;

/**
 * Created by rob on 9/8/14.
 */
public class CameraControlsOverlay implements Overlay, Camera.ShutterCallback {
    public interface Callbacks {
        public void onTakePicture();
        public void onConfirmPicture();
        public void onRetryPicture();
        public void onCameraControlsFinishedShow();
        public void onCameraControlsFinishedHide();
    }

    private final String TAG = CameraControlsOverlay.class.getSimpleName();

    private final long animDuration = 100;
    private final long animDelay = 50;
    private final long flashDuration = 300;

    private final long[][] DELAYS = {{0, 1, 2},{1,0,1}};

    private FrameLayout parent;
    private boolean inflated, attached;
    private ImageButton captureButton, retryButton;
    private View flashView;
    private ProgressBar photoPending;
    private Callbacks handler;

    ObjectAnimator
            animCaptureBtnHide,
            animCaptureBtnShow,
            animRetryBtnShow,
            animRetryBtnHide,
            animCameraFlash;


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
            parent.addView(captureButton);
            parent.addView(retryButton);
        }
    }

    private void inflateOverlay() {
        LayoutInflater.from(parent.getContext()).inflate(R.layout.caputure_photo_overlay, parent, true);
        captureButton = (ImageButton) parent.findViewById(R.id.btnTakePicture);
        retryButton = (ImageButton) parent.findViewById(R.id.btnRetakePicture);
        flashView = parent.findViewById(R.id.flashView);
        photoPending = (ProgressBar) parent.findViewById(R.id.cameraPhotoPending);

        captureButton.setOnClickListener(takePictureListener);
        retryButton.setOnClickListener(retryPictureListener);

        captureButton.setVisibility(View.INVISIBLE);
        retryButton.setVisibility(View.INVISIBLE);
        photoPending.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onParentLayoutComplete() {
        initAnim();
    }

    private void initAnim() {
        float parentHeight = parent.getHeight();

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

        animCameraFlash = ObjectAnimator.ofFloat(flashView, "alpha", 1, 0);
        animCameraFlash.setDuration(flashDuration);
        animCameraFlash.setInterpolator(new DecelerateInterpolator());
        animCameraFlash.addListener(new AnimUtils.HideOnEnd(flashView));
    }


    @Override
    public void showOverlay(boolean withAnimation) {
        setCaptureState(false);
        if (!withAnimation) {
            captureButton.setVisibility(View.VISIBLE);
        } else {
            captureButton.setVisibility(View.VISIBLE);
            retryButton.setVisibility(View.GONE);

            AnimatorSet anim = new AnimatorSet();
            anim.play(animCaptureBtnShow);
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
            captureButton.setVisibility(View.GONE);
            retryButton.setVisibility(View.GONE);
        } else {

            animCaptureBtnHide.setStartDelay(DELAYS[buttonClickIndex][0] * animDelay);
            animRetryBtnHide.setStartDelay(DELAYS[buttonClickIndex][1] * animDelay);

            AnimatorSet anim = new AnimatorSet();
            anim.setInterpolator(new AccelerateInterpolator());
            AnimatorSet.Builder builder = anim.play(animCaptureBtnHide);
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

    public void setPendingState(boolean withAnimation) {
        setCameraIconPending(withAnimation);
    }

    private void setCameraIconPending(boolean withAnimation) {
        captureButton.setImageResource(0);
        photoPending.setVisibility(View.VISIBLE);
    }

    private void setCameraIconTakePicture(boolean withAnimation) {
        captureButton.setBackgroundResource(R.drawable.circle);
        captureButton.setImageResource(R.drawable.ic_action_camera);
        captureButton.setOnClickListener(takePictureListener);
        photoPending.setVisibility(View.INVISIBLE);
    }

    private void setCameraIconConfirm(boolean withAnimation) {
        captureButton.setBackgroundResource(R.drawable.circle_green);
        captureButton.setImageResource(R.drawable.ic_action_accept);
        captureButton.setOnClickListener(confirmPictureListener);
        photoPending.setVisibility(View.INVISIBLE);
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

    @Override
    public void onShutter() {
        if (!animCameraFlash.isRunning()) {
            flashView.setVisibility(View.VISIBLE);
            animCameraFlash.start();
        }
    }
}
