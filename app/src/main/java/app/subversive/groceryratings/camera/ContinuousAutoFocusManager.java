package app.subversive.groceryratings.camera;

import android.hardware.Camera;

import java.util.List;

/**
 * Created by rob on 10/4/14.
 */
public class ContinuousAutoFocusManager extends AutoFocusManager {

    private ContinuousAutoFocusManager() {};

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

    static AutoFocusManager newInstance(Camera camera) {
        return new ContinuousAutoFocusManager();
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {

    }
}
