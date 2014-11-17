package app.subversive.groceryratings.UI;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

/**
 * Created by rob on 11/8/14.
 */
public class StatusManager {
    class Status {
        private View view;
        private ObjectAnimator animator; //showAnim, hideAnim, animator;
        private Status(final View view) {
            this.view = view;
            root.addView(view, statusLayoutParams);

            view.setVisibility(View.INVISIBLE);

            animator = ObjectAnimator.ofFloat(view, "y", 0);
            animator.setDuration(AnimUtils.StatusSwapDuration);
            animator.setInterpolator(adinterp);
        }

        public void show(boolean animated) {
            showStatus(this, animated);
        }

        public void hide(boolean animated) {
            hideStatus(this, animated);
        }
    }

    FrameLayout root;
    Status currentStatus, nextStatus;

    final private TimeInterpolator adinterp = new AccelerateDecelerateInterpolator();
    final private FrameLayout.LayoutParams statusLayoutParams =
            new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER | Gravity.TOP);


    public StatusManager(FrameLayout root) {
        this.root = root;
    }

    public Status createStatus(View statusView) {
        return new Status(statusView);
    }

    private void showStatus(final Status status, boolean animated) {
        if (status == currentStatus) {
        } else if (animated) {
            AnimatorSet anim = new AnimatorSet();
//            anim.setDuration(AnimUtils.StatusSwapDuration);
//            anim.setInterpolator(adinterp);
            if (currentStatus != null) {
                currentStatus.animator.setFloatValues(currentStatus.view.getY(), -currentStatus.view.getHeight());
                status.animator.setFloatValues(-status.view.getHeight(), 0);
//                anim.playSequentially(currentStatus.hideAnim, status.showAnim);
                anim.playSequentially(currentStatus.animator, status.animator);
                anim.addListener(new AnimUtils.HideOnEnd(currentStatus.view));
            } else {
                status.animator.setFloatValues(-status.view.getHeight(), 0);
                anim.play(status.animator);
            }
            status.view.setVisibility(View.VISIBLE);
            currentStatus = status;
            anim.start();

        } else {
            if (currentStatus != null) {
                currentStatus.animator.cancel();
                currentStatus.view.setVisibility(View.GONE);
            }
            status.view.setVisibility(View.VISIBLE);
            currentStatus = status;
            nextStatus = null;
        }
    }

    private void hideStatus(Status status, boolean animated) {
        if (status != currentStatus || currentStatus == null) {
            status.view.setVisibility(View.GONE);
        } else if (animated) {
            status.animator.setFloatValues(status.view.getY(), -status.view.getHeight());
            status.animator.start();
            currentStatus = null;
        } else {
            status.view.setVisibility(View.GONE);
            currentStatus = null;
        }
    }

    public void hideStatus(boolean animated) {
        hideStatus(currentStatus, animated);
    }

    public ObjectAnimator getStatusHideAnimator() {
        if (currentStatus == null) {
            return null;
        } else {
            if (currentStatus.animator.isStarted()) {
                currentStatus.animator.cancel();
            }
            currentStatus.animator.setFloatValues(currentStatus.view.getY(), -currentStatus.view.getHeight());
            currentStatus.animator.removeAllListeners();
            currentStatus.animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    animation.removeListener(this);
                    currentStatus = null;
                }
            });
            return currentStatus.animator;
        }
    }
}
