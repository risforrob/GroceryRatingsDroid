package app.subversive.groceryratings.camera;

import android.hardware.Camera;

import java.util.List;

/**
 * Created by rob on 10/4/14.
 */
public class AutoFocusFactory {
    static AutoFocusManager getManager(Camera camera) {
        Camera.Parameters params = camera.getParameters();
        List<String> focusModes = params.getSupportedFocusModes();
        AutoFocusManager aman;
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            aman = ContinuousAutoFocusManager.newInstance(camera);
//            aman =  PollingAutoFocusManager.newInstance(camera);
//            aman = PlaceboAutoFocusManager.newInstance(camera);
        } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            aman = PollingAutoFocusManager.newInstance(camera);
        } else {
            aman = PlaceboAutoFocusManager.newInstance(camera);
        }
        aman.camera = camera;
        return aman;
    }
}
