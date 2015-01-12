package app.subversive.groceryratings.UI;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import app.subversive.groceryratings.R;

/**
 * Created by rob on 12/28/14.
 */
public class TutorialOverlay implements Overlay {
    public interface Callbacks {
        public void onTutorialClicked();
        public void onTutorialClosed();
    }

    FrameLayout parent;
    View root;
    Callbacks mHandler;
    TextView tutorial;


    int[][] tutorialStages = {
            {R.drawable.phone_barcode,  R.string.tutorial1},
            {R.drawable.icon_arrows,    R.string.tutorial2},
            {R.drawable.icon_camera,    R.string.tutorial3}};

    int tutorialStage = 0;

    public TutorialOverlay(Callbacks handler) {
        mHandler = handler;
    }
    private boolean clickEnabled = false;
    final private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (clickEnabled) {
                mHandler.onTutorialClicked();
                clickEnabled = false;
            }
        }
    };

    @Override
    public void attachOverlayToParent(FrameLayout parent) {
        this.parent = parent;
        LayoutInflater.from(parent.getContext()).inflate(R.layout.tutorial_overlay, parent, true);
        root = parent.findViewById(R.id.tutorialRoot);
        tutorial = (TextView) root.findViewById(R.id.tvTutorial);
        root.setVisibility(View.GONE);
        root.setOnClickListener(mClickListener);
    }



    private void beginTutorialAnimation() {
        ObjectAnimator welcomeAnim = AnimUtils.alphaAnim(root.findViewById(R.id.tvWelcome), 0, 1, 1000L);
        ObjectAnimator logoAnim = AnimUtils.alphaAnim(root.findViewById(R.id.tvLogo), 0, 1, 1000L);
        ObjectAnimator dividerAnim = AnimUtils.alphaAnim(root.findViewById(R.id.tutorialDivder), 0, 1, 500L);
        ObjectAnimator tutorialAnim = AnimUtils.alphaAnim(tutorial, 0, 1, 500L);
        ObjectAnimator footerAnim = AnimUtils.alphaAnim(root.findViewById(R.id.tutorialFooter), 0, 1, 500L);

        AnimatorSet anim = new AnimatorSet();
        anim.play(welcomeAnim).after(500L);
        anim.play(logoAnim).after(500L);
        anim.play(dividerAnim).after(1500L);
        anim.play(tutorialAnim).after(1700L);
        anim.play(footerAnim).after(1900L);

        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                clickEnabled = true;
//                root.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        mHandler.onTutorialClicked();
//                    }
//                });
            }
        });

        anim.start();
    }


    @Override
    public void showOverlay(boolean withAnimation) {
        Log.v("tutorial", String.format("showOverlay %d", root.getBottom()-root.getTop()));
        root.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                onParentLayoutComplete();
                beginTutorialAnimation();
                root.removeOnLayoutChangeListener(this);
            }
        });
        root.setVisibility(View.VISIBLE);
    }


    public void nextTutorial() {
        tutorialStage++;
        if (tutorialStage < tutorialStages.length) {
            ObjectAnimator hide = AnimUtils.alphaAnim(tutorial, 1, 0, 200L);
            hide.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    tutorial.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            tutorialStages[tutorialStage][0],
                            0,
                            0);
                    tutorial.setText(tutorialStages[tutorialStage][1]);
                    ObjectAnimator show = AnimUtils.alphaAnim(tutorial, 0, 1, 200L);
                    show.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            clickEnabled = true;
                        }
                    });
                    show.start();
                }
            });
            hide.start();
        } else {
            hideOverlay(true);
        }
    }

    @Override
    public void hideOverlay(boolean withAnimation) {
        if (withAnimation) {
            ObjectAnimator hideAnimation = ObjectAnimator.ofFloat(root, "alpha", 1, 0);
            hideAnimation.setDuration(200L);
            hideAnimation.setInterpolator(new AccelerateInterpolator());
            hideAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    root.setVisibility(View.GONE);
                    mHandler.onTutorialClosed();
                }
            });
            hideAnimation.start();
        } else {
            root.setVisibility(View.GONE);
            mHandler.onTutorialClosed();
        }
    }

    @Override
    public void detachOverlayFromParent() {
        if (root != null && parent != null) {
            parent.removeView(root);
        }
        parent = null;
        root = null;
    }

    @Override
    public void onParentLayoutComplete() {

    }
}