package app.subversive.groceryratings.camera;

import android.hardware.Camera;
import android.util.Log;

import java.util.List;

/**
 * Created by rob on 10/4/14.
 */
public class PlaceboAutoFocusManager extends AutoFocusManager {
    final static String TAG = PlaceboAutoFocusManager.class.getSimpleName();
    private PlaceboAutoFocusManager() {};

    @Override
    void start() {

    }

    @Override
    void stop() {

    }

    @Override
    protected void pause() {

    }

    @Override
    protected void unpause() {

    }



    @Override
    void onAutoFocusFinished(FocusFinishedCallback cb) {
        cb.onFocusFinished();
    }

    @Override
    public void manualAutoFocus(List<Camera.Area> focusArea, List<Camera.Area> meteringArea, Camera.AutoFocusCallback cb) {
    }

    static protected AutoFocusManager newInstance(Camera camera) {
        Log.i(TAG, "Placebo!");
        return new PlaceboAutoFocusManager();
    }
}
