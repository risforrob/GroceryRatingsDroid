package app.subversive.groceryratings.UI;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Created by rob on 9/12/14.
 */
public class AnimUtils {
    public static long StatusSwapDuration = 200;
    public static class HideOnEnd extends AnimatorListenerAdapter {
        private View view;
        public HideOnEnd(View view) {
            super();
            this.view = view;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            view.setVisibility(View.GONE);
        }
    }
    public static class ShowOnStart extends AnimatorListenerAdapter {
        private View view;

        public ShowOnStart(View view) {
            super();
            this.view = view;
        }

        @Override
        public void onAnimationStart(Animator animation) {
            super.onAnimationStart(animation);
            view.setVisibility(View.VISIBLE);
//            view = null;
        }
    }

    public static ObjectAnimator alphaAnim(View animRoot, int start, int end, long duration) {
        animRoot.setVisibility(View.VISIBLE);
        animRoot.setAlpha(start);
        ObjectAnimator anim = ObjectAnimator.ofFloat(animRoot, "alpha", start, end);
        anim.setDuration(duration);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        return anim;
    }
}
