package app.subversive.groceryratings.UI;

import android.widget.FrameLayout;

/**
 * Created by rob on 9/8/14.
 */
public interface Overlay {
    public void attachOverlayToParent(FrameLayout parent);
    public void showOverlay(boolean withAnimation);
    public void hideOverlay(boolean withAnimation);
    public void detachOverlayFromParent();
    public void onParentLayoutComplete();
}
