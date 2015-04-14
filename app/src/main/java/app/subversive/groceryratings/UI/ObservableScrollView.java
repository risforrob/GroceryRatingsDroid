package app.subversive.groceryratings.UI;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by rob on 9/4/14.
 */
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ScrollView;

import java.util.ArrayList;

/**
 * A custom ScrollView that can accept a scroll listener.
 * Borrowed from google IO 2014
 */

public class ObservableScrollView extends ScrollView {
    public interface Callbacks {
        void onScrollChanged(int deltaX, int deltaY);
        void onTouchUp(float x, float y);
    }
    GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            for (Callbacks c : mCallbacks) {
                c.onTouchUp(e.getX(), e.getY());
            }
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    };

    GestureDetector gd = new GestureDetector(getContext(), gestureListener);

    private ArrayList<Callbacks> mCallbacks = new ArrayList<Callbacks>();

    public ObservableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        for (Callbacks c : mCallbacks) {
            c.onScrollChanged(l - oldl, t - oldt);
        }
    }


    public void addCallbacks(Callbacks listener) {
        if (!mCallbacks.contains(listener)) {
            mCallbacks.add(listener);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        gd.onTouchEvent(ev);
        return super.onTouchEvent(ev);
    }
}
