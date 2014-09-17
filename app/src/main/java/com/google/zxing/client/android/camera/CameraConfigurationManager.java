/*
 * Copyright (C) 2010 ZXing authors
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

package com.google.zxing.client.android.camera;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;


/**
 * A class which deals with reading, parsing, and setting the camera parameters which are used to
 * configure the camera hardware.
 */
final class CameraConfigurationManager {

    private static final String TAG = "CameraConfiguration";

    private Point cameraResolution;
    private int rotation;

    /**
     * Reads, one time, values from the camera that are needed by the app.
     */
    void initFromCameraParameters(Context context, Camera camera, int cameraId) {
        Camera.Parameters parameters = camera.getParameters();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();


        Point theScreenResolution = new Point();
        display.getSize(theScreenResolution);

        Log.i(TAG, "Screen resolution: " + theScreenResolution);
        cameraResolution = CameraConfigurationUtils.findBestPreviewSizeValue(parameters, theScreenResolution);
        Log.i(TAG, "Camera resolution: " + cameraResolution);

        Camera.CameraInfo info = new Camera.CameraInfo();
        camera.getCameraInfo(cameraId, info);

        rotation = CameraConfigurationUtils.findDisplayOrientation(display.getRotation(), info.orientation);
    }

    void setDesiredCameraParameters(Camera camera, boolean safeMode) {
        Camera.Parameters parameters = camera.getParameters();

        Log.i(TAG, "Initial camera parameters: " + parameters.flatten());

        if (safeMode) {
            Log.w(TAG, "In camera config safe mode -- most settings will not be honored");
        }

        CameraConfigurationUtils.setFocus(parameters, true, false, safeMode);

        if (!safeMode) {
            CameraConfigurationUtils.setBarcodeSceneMode(parameters);
            CameraConfigurationUtils.setVideoStabilization(parameters);
            CameraConfigurationUtils.setFocusArea(parameters);
            CameraConfigurationUtils.setMetering(parameters);
        }

        parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);
        parameters.setRotation(rotation);

        Log.i(TAG, "Final camera parameters: " + parameters.flatten());

        camera.setDisplayOrientation(rotation);
        camera.setParameters(parameters);


        Camera.Parameters afterParameters = camera.getParameters();
        Camera.Size afterSize = afterParameters.getPreviewSize();
        if (afterSize!= null && (cameraResolution.x != afterSize.width || cameraResolution.y != afterSize.height)) {
            Log.w(TAG, "Camera said it supported preview size " + cameraResolution.x + 'x' + cameraResolution.y +
                    ", but after setting it, preview size is " + afterSize.width + 'x' + afterSize.height);
            cameraResolution.x = afterSize.width;
            cameraResolution.y = afterSize.height;
        }
    }

    public void flipRotation(Camera camera) {
        rotation = (rotation + 180) % 360;
        camera.setDisplayOrientation(rotation);
        Camera.Parameters params = camera.getParameters();
        params.setRotation(rotation);
        camera.setParameters(params);
    }

    Point getCameraResolution() {
        return cameraResolution;
    }
}
