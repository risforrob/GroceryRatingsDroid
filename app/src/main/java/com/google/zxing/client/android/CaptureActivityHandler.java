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
    private final DecodeThread decodeThread;
    private State state;

    private enum State {
        PREVIEW,
        SUCCESS,
        PAUSED,
        DONE
    }

    public CaptureActivityHandler(ScanFragment activity) {
        this.activity = activity;
        decodeThread = new DecodeThread(this);
        decodeThread.start();
        state = State.SUCCESS;

        // Start ourselves capturing previews and decoding.
        CameraManager.startPreview();
        restartPreviewAndDecode();
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case R.id.pause_scan:
                state = State.PAUSED;
                removeMessages(R.id.decode_succeeded);
                removeMessages(R.id.decode_failed);
                break;
            case R.id.restart_preview:
                restartPreviewAndDecode();
                break;
            case R.id.decode_succeeded:
                state = State.SUCCESS;
                activity.handleDecode((String) message.obj); //, barcode, scaleFactor);
                break;
            case R.id.decode_failed:
                if (state != State.PAUSED) {
                    // We're decoding as fast as possible, so when one decode fails, start another.
                    state = State.PREVIEW;
                    CameraManager.requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
                }
                break;
        }
    }

    public void quitSynchronously() {
        state = State.DONE;
        CameraManager.stopPreview();
        Message quit = Message.obtain(decodeThread.getHandler(), R.id.quit);
        quit.sendToTarget();
        try {
            // Wait at most half a second; should be enough time, and onPause() will timeout quickly
            decodeThread.join(500L);
        } catch (InterruptedException e) {
            // continue
        }

        // Be absolutely sure we don't send any queued up messages
        removeMessages(R.id.decode_succeeded);
        removeMessages(R.id.decode_failed);
    }

    private void restartPreviewAndDecode() {
        if (state == State.SUCCESS || state == State.PAUSED) {
            state = State.PREVIEW;
            CameraManager.requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
        }
    }

}
