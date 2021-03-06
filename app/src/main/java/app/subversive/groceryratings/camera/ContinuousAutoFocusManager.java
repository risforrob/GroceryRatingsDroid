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
        Camera.Parameters params = camera.getParameters();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        params.setFocusAreas(null);
        params.setMeteringAreas(null);
        camera.setParameters(params);
    }

    @Override
    void stop() {
    }

    @Override
    protected void pause() {
        camera.cancelAutoFocus();
        Camera.Parameters params = camera.getParameters();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        camera.setParameters(params);
    }

    @Override
    protected void unpause() {
        Camera.Parameters params = camera.getParameters();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        camera.setParameters(params);
    }

    static AutoFocusManager newInstance(Camera camera) {
        return new ContinuousAutoFocusManager();
    }

    @Override
    void onAutoFocusFinished(final FocusFinishedCallback cb) {
        camera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                cb.onFocusFinished();
            }
        });
    }
}
