package app.subversive.groceryratings.UI;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

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

    public TutorialOverlay(Callbacks handler) {
        mHandler = handler;
    }

    @Override
    public void attachOverlayToParent(FrameLayout parent) {
        this.parent = parent;
        LayoutInflater.from(parent.getContext()).inflate(R.layout.tutorial_overlay, parent, true);
        root = parent.findViewById(R.id.tutorialRoot);
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.onTutorialClicked();
            }
        });
        root.setVisibility(View.GONE);
    }

    @Override
    public void showOverlay(boolean withAnimation) {
        root.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideOverlay(boolean withAnimation) {
        root.setVisibility(View.GONE);
        if (withAnimation) {
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
