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

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import app.subversive.groceryratings.ScanFragment;

/**
 * This thread does all the heavy lifting of decoding the images.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
final class DecodeThread extends Thread {

    private final Map<DecodeHintType,Object> hints;
    private Handler handler;
    private final Handler resultHandler;
    private final CountDownLatch handlerInitLatch;

    DecodeThread(Handler resultHandler) {

        this.resultHandler = resultHandler;
        handlerInitLatch = new CountDownLatch(1);

        // The prefs can't change while the thread is running, so pick them up once here.
        Collection<BarcodeFormat> decodeFormats = EnumSet.noneOf(BarcodeFormat.class);
        decodeFormats.addAll(DecodeFormatManager.PRODUCT_FORMATS);
        //decodeFormats.addAll(DecodeFormatManager.INDUSTRIAL_FORMATS);

        hints = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);

        //todo add charset hints
//        if (characterSet != null) {
//            hints.put(DecodeHintType.CHARACTER_SET, characterSet);
//        }
        Log.i("DecodeThread", "Hints: " + hints);
    }

    Handler getHandler() {
        try {
            handlerInitLatch.await();
        } catch (InterruptedException ie) {
            // continue?
        }
        return handler;
    }

    @Override
    public void run() {
        Looper.prepare();
        handler = new DecodeHandler(resultHandler, hints);
        handlerInitLatch.countDown();
        Looper.loop();
    }

}
