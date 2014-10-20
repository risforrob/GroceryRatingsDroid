/*
 * Copyright (C) 2012 ZXing authors
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

import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import app.subversive.groceryratings.ManagedTimer;

abstract class AutoFocusManager {
    private final String TAG = AutoFocusManager.class.getSimpleName();
    protected Camera camera;

    final ManagedTimer.RunnableController controller = ManagedTimer.getController(new Runnable() {
        @Override
        public void run() {
            resetAutoFocus();
        }
    }, 5000L);

    abstract void start();
    abstract void stop();
    protected abstract void pause();
    protected abstract void unpause();

    public void manualAutoFocus(List<Camera.Area> focusArea, List<Camera.Area> meteringArea, final Camera.AutoFocusCallback cb) {
        camera.cancelAutoFocus();
        controller.restart();
        pause();
        Camera.Parameters params = camera.getParameters();
        params.setFocusAreas(focusArea);
        params.setMeteringAreas(meteringArea);
        if (params.isAutoExposureLockSupported()) { params.setAutoExposureLock(false); }
        if (params.isAutoWhiteBalanceLockSupported()) { params.setAutoWhiteBalanceLock(false); }
        camera.setParameters(params);
        camera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                Camera.Parameters params = camera.getParameters();
                if (params.isAutoExposureLockSupported()) {
                    params.setAutoExposureLock(true);
                }

                if (params.isAutoWhiteBalanceLockSupported()) {
                    params.setAutoWhiteBalanceLock(true);
                }
                camera.setParameters(params);
                cb.onAutoFocus(success, camera);
            }
        });
    }

    private void resetAutoFocus() {
        Camera.Parameters params = camera.getParameters();
        params.setFocusAreas(null);
        params.setMeteringAreas(null);
        if (params.isAutoExposureLockSupported()) {
            params.setAutoExposureLock(false);
        }

        if (params.isAutoWhiteBalanceLockSupported()) {
            params.setAutoWhiteBalanceLock(false);
        }
        camera.setParameters(params);
        unpause();
    }

    private void lockExposureWhiteBalance(Camera.Parameters params) {
        if (params == null) {
            params = camera.getParameters();
        }
        if (params.isAutoExposureLockSupported()) {
            params.setAutoExposureLock(true);
        }

        if (params.isAutoWhiteBalanceLockSupported()) {
            params.setAutoWhiteBalanceLock(true);
        }

    }
}