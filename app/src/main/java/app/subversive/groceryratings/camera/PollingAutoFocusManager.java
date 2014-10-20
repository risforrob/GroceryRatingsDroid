package app.subversive.groceryratings.camera;

import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;
import java.util.concurrent.RejectedExecutionException;

/**
 * Created by rob on 10/4/14.
 */
public class PollingAutoFocusManager extends AutoFocusManager implements Camera.AutoFocusCallback {

    private static final String TAG = PollingAutoFocusManager.class.getSimpleName();

    private static final long AUTO_FOCUS_INTERVAL_MS = 2000L;

    private boolean stopped;
    private boolean focusing;
    private AutoFocusTask outstandingTask;
    private FocusFinishedCallback focusFinishedCallback;

    private PollingAutoFocusManager() {};

    static protected AutoFocusManager newInstance(Camera camera) {
        PollingAutoFocusManager cman = new PollingAutoFocusManager();
        Camera.Parameters params = camera.getParameters();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        params.setFocusAreas(null);
        params.setMeteringAreas(null);
        camera.setParameters(params);
        return cman;
    }

    public void onAutoFocus(boolean success, Camera theCamera) {
        focusing = false;
        if (focusFinishedCallback != null) {
            focusFinishedCallback.onFocusFinished();
            focusFinishedCallback = null;
        }
        autoFocusAgainLater();
    }

    private void autoFocusAgainLater() {
        if (!stopped && outstandingTask == null) {
            AutoFocusTask newTask = new AutoFocusTask();
            try {
                newTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                outstandingTask = newTask;
            } catch (RejectedExecutionException ree) {
                Log.w(TAG, "Could not request auto focus", ree);
            }
        }
    }

    @Override
    public void start() {
        Log.i(TAG, "START");
        stopped = false;
        focusing = false;
        run();
    }

    public void run() {
        Log.i(TAG, "RUN");
        outstandingTask = null;
        if (!stopped && !focusing) {
            try {
                focusing = true;
                camera.autoFocus(this);
            } catch (RuntimeException re) {
                // Have heard RuntimeException reported in Android 4.0.x+; continue?
                Log.w(TAG, "Unexpected exception while focusing", re);
                // Try again later to keep cycle going
                autoFocusAgainLater();
            }
        }
    }

    private void cancelOutstandingTask() {
        if (outstandingTask != null) {
            if (outstandingTask.getStatus() != AsyncTask.Status.FINISHED) {
                outstandingTask.cancel(true);
            }
            outstandingTask = null;
        }
    }

    @Override
    public void stop() {
        Log.i(TAG, "STOP");
        stopped = true;
        cancelOutstandingTask();
        // Doesn't hurt to call this even if not focusing
        try {
            camera.cancelAutoFocus();
        } catch (RuntimeException re) {
            // Have heard RuntimeException reported in Android 4.0.x+; continue?
            Log.w(TAG, "Unexpected exception while cancelling focusing", re);
        }
    }

    @Override
    protected void pause() {
        Log.i(TAG,"PAUSE");
        stop();
    }

    @Override
    protected void unpause() {
        Log.i(TAG, "UNPAUSE");
        start();
    }

    @Override
    void onAutoFocusFinished(FocusFinishedCallback cb) {
        if (focusing) {
            focusFinishedCallback = cb;
        } else {
            cb.onFocusFinished();
        }
    }

    private final class AutoFocusTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(AUTO_FOCUS_INTERVAL_MS);
            } catch (InterruptedException e) {
                // continue
            }
            if (!isCancelled()) {
                run();
            }
            return null;
        }
    }
}