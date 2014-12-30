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

package com.google.zxing.client.android;

import app.subversive.groceryratings.camera.CameraManager;

import android.os.Handler;
import android.os.Message;

import app.subversive.groceryratings.R;
import app.subversive.groceryratings.ScanFragment;

/**
 * This class handles all the messaging which comprises the state machine for capture.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class CaptureActivityHandler extends Handler {

    private static final String TAG = CaptureActivityHandler.class.getSimpleName();

    private final ScanFragment activity;
    private DecodeThread decodeThread;
    private boolean paused;

    public CaptureActivityHandler(ScanFragment activity) {
        this.activity = activity;

    }

    public void start() {
        if (decodeThread == null) {
            decodeThread = new DecodeThread(this);
            decodeThread.start();
            paused = true;
            restartPreviewAndDecode();
        }
        if (paused) {
            paused = false;
            restartPreviewAndDecode();
        }
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case R.id.decode_succeeded:
                activity.handleDecode((String) message.obj);
                break;
            case R.id.decode_failed:
                restartPreviewAndDecode();
                break;
        }
    }

    public void pause() {
        paused = true;
        // Be absolutely sure we don't send any queued up messages
        removeMessages(R.id.decode_succeeded);
        removeMessages(R.id.decode_failed);
    }

    public void stop() {
        pause();
        Message.obtain(decodeThread.getHandler(), R.id.quit).sendToTarget();
        try {
            // Wait at most half a second; should be enough time, and onPause() will timeout quickly
            decodeThread.join(500L);
        } catch (InterruptedException e) {
            // continue
        }
        decodeThread = null;
    }
    private void restartPreviewAndDecode() {
        if (decodeThread != null && decodeThread.isAlive() && !paused) {
            CameraManager.requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
        }
    }
}
