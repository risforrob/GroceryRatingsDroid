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
    final int rowSpacing = 1;
    final int maxChildren = 7;

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
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int height = resolveSizeAndState(getRootView().getHeight() + 400, heightMeasureSpec, 0);
        setMeasuredDimension(widthMeasureSpec, height);
    }



    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int count = getChildCount();

        final int mHeight = getRootView().getHeight();

        int lastChildTop = (int) Math.round(mHeight * .65);

        for (int i = 0; i < Math.min(count, maxChildren) ; i++) {
            final View child = getChildAt(i);

            final int childHeight = child.getMeasuredHeight();

            child.layout(left, lastChildTop, right, lastChildTop + childHeight + rowSpacing);
            lastChildTop += childHeight + rowSpacing + 1;
        }

        for (int i = maxChildren ; i < count; i++) {
            removeViewInLayout(getChildAt(i));
        }
    }
}
