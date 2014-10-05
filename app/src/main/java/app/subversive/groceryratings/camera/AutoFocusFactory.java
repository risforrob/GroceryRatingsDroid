package app.subversive.groceryratings.camera;

import android.hardware.Camera;

import java.util.List;

/**
 * Created by rob on 10/4/14.
 */
public class AutoFocusFactory {
    static AutoFocusManager getManager(Camera camera) {
        Camera.Parameters params = camera.getParameters();
        List<String> focusModes = params.getSupportedFlashModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
//            return ContinuousAutoFocusManager.newInstance(camera);
            return PollingAutoFocusManager.newInstance(camera);
        } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            return PollingAutoFocusManager.newInstance(camera);
        } else {
            return PlaceboAutoFocusManager.newInstance(camera);
        }
    }
}
