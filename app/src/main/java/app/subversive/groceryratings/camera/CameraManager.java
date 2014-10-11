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

import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import com.google.zxing.PlanarYUVLuminanceSource;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import app.subversive.groceryratings.MainWindow;
import app.subversive.groceryratings.UI.AutoFocusIndicator;

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
    private static boolean initialized;
    private static boolean previewing;
    private static int cameraId = -1;


    public static boolean isFocusSupported, isMeteringSupported;
    private static List<Camera.Area> focusAreas;
    private static List<Camera.Area> meteringAreas;

    private CameraManager() { }

    /**
     * Opens the camera driver and initializes the hardware parameters.
     *
     * @param holder The surface object which the camera will draw preview frames into.
     * @throws IOException Indicates the camera driver failed to open.
     */
    public static synchronized void openDriver(SurfaceHolder holder, Display display) throws IOException {
        Camera theCamera = camera;
        if (theCamera == null) {

            cameraId = CameraConfigurationUtils.findCameraId();
            if (cameraId < 0) {
                throw new RuntimeException("No valid camera");
            }

            theCamera = Camera.open(cameraId);
            camera = theCamera;
        }

        theCamera.setPreviewDisplay(holder);

        if (!initialized) {
            initialized = true;
            configManager.initFromCameraParameters(display, theCamera, cameraId);
            autoFocusManager = AutoFocusFactory.getManager(theCamera); //new AutoFocusManager(theCamera);
        }

        Camera.Parameters parameters = theCamera.getParameters();
        String parametersFlattened = parameters.flatten(); // Save these, temporarily
        try {
            Camera.Parameters params;
            params = configManager.setDesiredCameraParameters(theCamera, false);
            theCamera.setParameters(params);
        } catch (RuntimeException re) {
            // Driver failed
            Log.w(TAG, "Camera rejected parameters. Setting only minimal safe-mode parameters");
            Log.i(TAG, "Resetting to saved camera params: " + parametersFlattened);
            // Reset:
            parameters = theCamera.getParameters();
            parameters.unflatten(parametersFlattened);
            try {
                theCamera.setParameters(parameters);
                configManager.setDesiredCameraParameters(theCamera, true);
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

    public static synchronized boolean isOpen() {
        return camera != null;
    }

    /**
     * Closes the camera driver if still in use.
     */
    public static synchronized void closeDriver() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
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

    public static synchronized void takePicture(Camera.PictureCallback callback) {
        if (previewing) {
            previewing = false;
            autoFocusManager.stop();
            camera.takePicture(null, null, callback);
        }
    }

    private static void constrainRect(Rect r, int xmin, int xmax, int ymin, int ymax) {
        int offx = 0;
        int offy = 0;
        if (r.left < xmin) {
            offx = xmin - r.left;
        } else if (r.right > xmax) {
            offx = xmax - r.right;
        }

        if (r.top < ymin) {
            offy = ymin - r.top;
        } else if (r.bottom > ymax) {
            offy = ymax - r.bottom;
        }

        r.offset(offx, offy);
    }

    private static int getCameraCoord(float v) {
        return Math.round((v*2000)-1000);
    }

    private static void setArea(float camcx, float camcy, float mult, Camera.Area area) {
        Point res = configManager.getCameraResolution();
        int radius = Math.round(Math.max(res.x, res.y) / 8 * mult);
        int cx = getCameraCoord(camcx);
        int cy = getCameraCoord(camcy);
        area.rect.set(cx-radius, cy-radius, cx+radius, cy+radius);
        constrainRect(area.rect, -1000, 1000, -1000, 1000);
    }

    public static void autoFocus(float nx, float ny, Camera.AutoFocusCallback cb) {
        float camcx = nx;
        float camcy = ny;
        switch (configManager.getRotation()) {
            case 90:
                camcx = ny;
                camcy = -nx;
                break;
            case 180:
                camcx = -nx;
                camcy = -ny;
                break;
            case 270:
                camcx = -ny;
                camcy = nx;
                break;
        }
        if (isFocusSupported) {
            setArea(camcx, camcy, 1.0f, focusAreas.get(0));
        }

        if (isMeteringSupported) {
            setArea(camcx, camcy, 1.5f, meteringAreas.get(0));
        }
        autoFocusManager.manualAutoFocus(focusAreas, meteringAreas, cb);
    }
}
