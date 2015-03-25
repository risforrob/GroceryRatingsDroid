/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.subversive.groceryratings.camera;

import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;

import com.google.zxing.PlanarYUVLuminanceSource;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * This object wraps the Camera service object and expects to be the only one talking to it. The
 * implementation encapsulates the steps needed to take preview-sized images, which are used for
 * both preview and decoding.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class CameraManager {

    private static final String TAG = CameraManager.class.getSimpleName();

    private static final CameraConfigurationManager configManager = new CameraConfigurationManager();
    private static final PreviewCallback previewCallback = new PreviewCallback(configManager);

    private static Camera camera;
    private static AutoFocusManager autoFocusManager;
    private static boolean previewing;
    private static int cameraId = -1;


    public static boolean isFocusSupported, isMeteringSupported;
    private static List<Camera.Area> focusAreas;
    private static List<Camera.Area> meteringAreas;

    private CameraManager() { }


    public static void initializeCamera(Activity activity) {
        CameraUtil.initialize(activity);

        cameraId = CameraConfigurationUtils.findCameraId();
        camera = Camera.open(cameraId);

        Display display = activity.getWindowManager().getDefaultDisplay();
        configManager.initFromCameraParameters(display, camera, cameraId);
        autoFocusManager = AutoFocusFactory.getManager(camera);

        Camera.Parameters parameters = camera.getParameters();
        String parametersFlattened = parameters.flatten(); // Save these, temporarily
        try {
            Camera.Parameters params;
            params = configManager.setDesiredCameraParameters(camera, false);
            camera.setParameters(params);
        } catch (RuntimeException re) {
            // Driver failed
            Log.w(TAG, "Camera rejected parameters. Setting only minimal safe-mode parameters");
            Log.i(TAG, "Resetting to saved camera params: " + parametersFlattened);
            // Reset:
            parameters = camera.getParameters();
            parameters.unflatten(parametersFlattened);
            try {
                camera.setParameters(parameters);
                configManager.setDesiredCameraParameters(camera, true);
            } catch (RuntimeException re2) {
                // Well, darn. Give up
                Log.w(TAG, "Camera rejected even safe-mode parameters! No configuration");
            }
        }

        isFocusSupported = parameters.getMaxNumFocusAreas() > 0;
        isMeteringSupported = parameters.getMaxNumMeteringAreas() > 0;

        if (isFocusSupported) {
            focusAreas = new LinkedList<Camera.Area>();
            focusAreas.add(new Camera.Area(new Rect(), 1));
        }
        if (isMeteringSupported) {
            meteringAreas = new LinkedList<Camera.Area>();
            meteringAreas.add(new Camera.Area(new Rect(), 1));
        }
    }

    public static Point getCameraResolution() {
        if (configManager.getRotation() == 0 ||
                configManager.getRotation() == 180 ) {
            return configManager.getCameraResolution();
        } else {
            Point r = configManager.getCameraResolution();
            return new Point(r.y, r.x);
        }
    }

    public static void setPreviewDisplay(SurfaceHolder holder) throws IOException {
        camera.setPreviewDisplay(holder);
    }


    public static synchronized boolean isOpen() {
        return camera != null;
    }

    /**
     * Asks the camera hardware to begin drawing preview frames to the screen.
     */
    public static synchronized void startPreview() {
        Camera theCamera = camera;
        if (theCamera != null && !previewing) {
            theCamera.startPreview();
            previewing = true;
            autoFocusManager.start();
        }
    }

    public static synchronized void flipPreview() {
        Camera theCamera = camera;
        if (theCamera != null && previewing) {
            autoFocusManager.stop();
            theCamera.stopPreview();
            configManager.flipRotation(theCamera);
            Log.i(TAG, "Do a barrel roll!");
            theCamera.startPreview();
            autoFocusManager.start();

        }
    }

    /**
     * Tells the camera to stop drawing preview frames.
     */
    public static synchronized void stopPreview() {
        autoFocusManager.stop();
        if (camera != null && previewing) {
            camera.stopPreview();
            previewCallback.setHandler(null, 0);
            previewing = false;
        }
    }

    /**
     * Closes the camera driver if still in use.
     */
    public static synchronized void closeDriver() {
        if (camera != null) {
            if (previewing) {
                stopPreview();
            }
            camera.release();
        }
    }

    /**
     * A single preview frame will be returned to the handler supplied. The data will arrive as byte[]
     * in the message.obj field, with width and height encoded as message.arg1 and message.arg2,
     * respectively.
     *
     * @param handler The handler to send the message to.
     * @param message The what field of the message to be sent.
     */


    static long lastRequest = System.currentTimeMillis();

    public static synchronized void requestPreviewFrame(Handler handler, int message) {
        Camera theCamera = camera;
        if (theCamera != null && previewing) {
            Long curr = System.currentTimeMillis();
            Log.d("FPS", String.valueOf(curr - lastRequest));
            lastRequest = curr;
            previewCallback.setHandler(handler, message);
            theCamera.setOneShotPreviewCallback(previewCallback);
        }
    }

    /**
     * A factory method to build the appropriate LuminanceSource object based on the format
     * of the preview buffers, as described by Camera.Parameters.
     *
     * @param data A preview frame.
     * @param width The width of the image.
     * @param height The height of the image.
     * @return A PlanarYUVLuminanceSource instance.
     */
    public static PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
        return new PlanarYUVLuminanceSource(data, width, height, 1, 1,
                width-1, height-1, false);
    }

    public static synchronized void takePicture(
            final Camera.ShutterCallback shutterCallback, final Camera.PictureCallback pictureCallback) {
        if (previewing) {
            previewing = false;
            autoFocusManager.onFocusFinished(new AutoFocusManager.FocusFinishedCallback() {
                @Override
                public void onFocusFinished() {
                    autoFocusManager.stop();
                    camera.takePicture(shutterCallback, null, pictureCallback);
                }
            });
        }
    }

    public static void autoFocus(float x, float y, float radius, int width, int height, Camera.AutoFocusCallback cb) {
        if (!previewing) {
            cb.onAutoFocus(false, camera);
            return;
        }
        Matrix mXform = new Matrix();
        Matrix inverse = new Matrix();
        RectF result = new RectF();
        Rect dims = new Rect(0, 0, width, height);

        CameraUtil.prepareMatrix(inverse, false, configManager.getRotation(), dims);
        inverse.invert(mXform);


        if (isFocusSupported) {
            RectF focusRect = CameraUtil.makeRectF(x, y, radius);
            mXform.mapRect(result, focusRect);
            CameraUtil.clampRectF(result, -1000, -1000, 1000, 1000);
            CameraUtil.rectFToRect(result, focusAreas.get(0).rect);
        }

        if (isMeteringSupported) {
            RectF meterRect = CameraUtil.makeRectF(x, y, radius*1.5f);
            mXform.mapRect(result, meterRect);
            CameraUtil.cropRectF(result, -1000, -1000, 1000, 1000);
            CameraUtil.rectFToRect(result, meteringAreas.get(0).rect);
        }
        autoFocusManager.manualAutoFocus(focusAreas, meteringAreas, cb);
    }
}
