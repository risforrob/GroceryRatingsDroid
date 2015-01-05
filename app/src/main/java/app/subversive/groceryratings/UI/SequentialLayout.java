package app.subversive.groceryratings.UI;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import app.subversive.groceryratings.R;

/**
 * Created by rob on 1/4/15.
 */
public class SequentialLayout extends ViewGroup {
    private int maxRows;
    private int childrenToLayout;

    public SequentialLayout(Context context) {
        this(context, null);
    }

    public SequentialLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SequentialLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray arr = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.SequentialLayout, 0, 0);
        maxRows = arr.getInt(R.styleable.SequentialLayout_numRows, Integer.MAX_VALUE);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {
            int leftWithPadding = l + getPaddingLeft();
            int rowLeft = leftWithPadding;
            int top = t + getPaddingTop();
            int rowMaxHeight = 0;

            for (int i = 0 ; i < getChildCount() ; i++ ) {
                final View child = getChildAt(i);
                final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                int childLeft = rowLeft+lp.leftMargin;
                int childTop = top + lp.topMargin;

                if (rowLeft + child.getMeasuredWidth() > r) {
                    top = top + rowMaxHeight;
                    rowMaxHeight = 0;
                    rowLeft = leftWithPadding;
                }
                child.layout(childLeft, childTop, childLeft + child.getMeasuredWidth(), childTop + child.getMeasuredHeight());
                rowMaxHeight = Math.max(rowMaxHeight, child.getMeasuredHeight() + lp.topMargin);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int desiredHeight = 0;
        int availableWidth = View.MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        int currRowMaxHeight = 0;
        int rowCount = 1;
        int currRowWidth = 0;
        for (int i = 0 ; i < getChildCount() ; i++ ) {
            final View child = getChildAt(i);
            measureChildWithMargins(
                    child,
                    widthMeasureSpec,
                    getPaddingLeft()+getPaddingRight(),
                    heightMeasureSpec,
                    getPaddingTop()+getPaddingBottom());

            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            if (childWidth > availableWidth) {
                throw new RuntimeException("SequentialLayout cannot layout a child wider than itself");
            }

            if (currRowWidth + childWidth <= availableWidth) {
                currRowWidth += childWidth;
                currRowMaxHeight = Math.max(currRowMaxHeight, childHeight);
            } else {
                rowCount += 1;
                currRowWidth = childWidth;
                desiredHeight += currRowMaxHeight;
                currRowMaxHeight = childHeight;
            }
        }
        desiredHeight += currRowMaxHeight;
        desiredHeight = desiredHeight + getPaddingTop() + getPaddingBottom();

        setMeasuredDimension(widthMeasureSpec, resolveSizeAndState(desiredHeight, heightMeasureSpec, 0));
    }
}
