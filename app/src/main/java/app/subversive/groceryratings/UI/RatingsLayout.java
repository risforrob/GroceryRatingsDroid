package app.subversive.groceryratings.UI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
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
    final int rowSpacing = 1;
    final int maxChildren = 7;
    final int visibleChildren = 2;

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
//        setWillNotDraw(false);
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {


        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int desiredHeight = getSuggestedMinimumHeight();
        if (getChildCount() > visibleChildren) {
            desiredHeight =
                    desiredHeight +
                            ((getChildCount() - visibleChildren) * (getChildAt(0).getMeasuredHeight() + rowSpacing));
        }

        int height = resolveSizeAndState(desiredHeight, heightMeasureSpec, 0);
        setMeasuredDimension(widthMeasureSpec, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int count = getChildCount();

        if (count == 0) {
            return;
        }

        int lastBottom = bottom;
        if (count < visibleChildren) {
            lastBottom -= (visibleChildren - count) * (getChildAt(0).getMeasuredHeight() + rowSpacing);
        }

        for (int i = 0; i < count ; i++) {
            final View child = getChildAt(count - i - 1);
            final int childHeight = child.getMeasuredHeight();
            child.layout(left, lastBottom - rowSpacing - childHeight, right, lastBottom-rowSpacing);
            lastBottom = lastBottom - rowSpacing - childHeight;
        }
    }
}
