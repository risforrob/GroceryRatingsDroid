package app.subversive.groceryratings.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import app.subversive.groceryratings.ManagedTimer;

/**
 * Created by rob on 11/16/14.
 */
public class AOFrameLayout extends FrameLayout {
    final String TAG = AOFrameLayout.class.getSimpleName();
    public ManagedTimer.RunnableController mTimer;
    final Long screenTimeout = 60000L;

    public AOFrameLayout(Context context) {
        super(context);
        init(context);
    }

    public AOFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AOFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        setKeepScreenOn(true);
        mTimer = ManagedTimer.getController(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Timer timeout");
                setKeepScreenOn(false);
            }
        }, screenTimeout);
        mTimer.restart();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!getKeepScreenOn()) { setKeepScreenOn(true); }
        mTimer.restart();
        return super.onInterceptTouchEvent(ev);
    }

    public void restartTimer() {
        if (mTimer != null) { mTimer.restart(); }
    }
}
