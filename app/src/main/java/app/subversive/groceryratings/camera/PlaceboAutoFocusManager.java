package app.subversive.groceryratings.camera;

import android.hardware.Camera;

import java.util.List;

/**
 * Created by rob on 10/4/14.
 */
public class PlaceboAutoFocusManager extends AutoFocusManager {

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
    public void setAutoFocusArea(List<Camera.Area> focusArea, List<Camera.Area> meteringArea) {
        // do nothing for setAutoFocus
    }

    static protected AutoFocusManager newInstance(Camera camera) {
        return new PlaceboAutoFocusManager();
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {

    }
}
