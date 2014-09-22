package app.subversive.groceryratings;

import android.content.Context;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.WindowManager;

import java.lang.annotation.Target;

/**
 * Created by rob on 8/24/14.
 *
 * Example Init
 *         mFlipListener = new FlipListener(getActivity(), new FlipListener.OnFlipListener() {
 *             @Override
 *             public void onFlip() {
 *                 if (cameraManager != null) {
 *                     cameraManager.flipPreview();
 *                 }
 *             }
 *         });
 */
public class FlipListener extends OrientationEventListener {

    public static interface OnFlipListener { public void onFlip(); }

    private OnFlipListener mListener;
    Display mDisplay;
    int mTargetRotation;

    public FlipListener(Context context, OnFlipListener listener) {
        super(context);
        mListener = listener;
        mDisplay = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                        .getDefaultDisplay();
    }

    @Override
    public void enable() {
        super.enable();
        mTargetRotation = getTargetRotation(mDisplay.getRotation());
    }

    public int getTargetRotation(int currentRotation) {
        switch(currentRotation) {
            case Surface.ROTATION_0:   return Surface.ROTATION_180;
            case Surface.ROTATION_90:  return Surface.ROTATION_270;
            case Surface.ROTATION_180: return Surface.ROTATION_0;
            case Surface.ROTATION_270: return Surface.ROTATION_90;
        }
        return ORIENTATION_UNKNOWN;
    }

    @Override
    public void onOrientationChanged(int orientation) {
        if(mTargetRotation == mDisplay.getRotation()) {
            mTargetRotation = getTargetRotation(mTargetRotation);
            mListener.onFlip();
        }
    }
}
