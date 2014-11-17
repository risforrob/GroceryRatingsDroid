package app.subversive.groceryratings.UI;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;

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
        }
    }
}
