package app.subversive.groceryratings.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;

/**
 * Created by rob on 9/1/14.
 */
@RemoteViews.RemoteView
public class RatingsLayout extends ViewGroup {
    final int rowSpacing = 8;

    public RatingsLayout(Context context) {
        super(context);
        init(context);
    }

    public RatingsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RatingsLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {

    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        for (int i = 0; i < getChildCount() ; i++) {
            final View child = getChildAt(i);
            LayoutParams lp = child.getLayoutParams();
            Log.v("","");
        }

        measureChildren(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }



    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int count = getChildCount();

        int lastChildTop = bottom;

        for (int i = 0; i < count ; i++) {
            final View child = getChildAt(i);

            final int childHeight = child.getMeasuredHeight();
            final int childBottom = lastChildTop - rowSpacing;
            final int childTop = childBottom - childHeight;

            lastChildTop = childBottom - childHeight;

            child.layout(left, childTop, right, childBottom);
        }
    }
}
