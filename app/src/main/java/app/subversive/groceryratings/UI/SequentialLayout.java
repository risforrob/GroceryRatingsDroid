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
    private int wPadding = 16;
    private int hPadding = 16;

    ArrayList<Integer> rowInnerPadding = new ArrayList<>();
    ArrayList<Integer> rowHeights = new ArrayList<>();



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
            int rowLeft = getPaddingLeft();
            int top = getPaddingTop();
            int availWidth = (r - l) - getPaddingLeft() - getPaddingRight();
//            int rowMaxHeight = 0;
            int rowNum = 0;
            int currRowPadding = rowInnerPadding.get(0);
            for (int i = 0 ; i < getChildCount() ; i++ ) {
                final View child = getChildAt(i);


                if (rowLeft + child.getMeasuredWidth() > availWidth) {
                    rowNum += 1;
                    top = top + rowHeights.get(rowNum);
                    currRowPadding = rowInnerPadding.get(rowNum);
                    rowLeft = getPaddingLeft();
                }

                child.layout(rowLeft, top, rowLeft + child.getMeasuredWidth(), top + child.getMeasuredHeight());
//                rowMaxHeight = Math.max(rowMaxHeight, child.getMeasuredHeight());
                rowLeft = rowLeft + child.getMeasuredWidth() + currRowPadding;
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int desiredHeight = 0;
        int availableWidth = View.MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        int currRowMaxHeight = 0;
        int rowCount = 0;
        int currRowWidth = 0;
        int rowMinWidth = 0;
        int rowChildCount = 0;
        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);

            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            if (childWidth > availableWidth) {
                throw new RuntimeException("SequentialLayout cannot layout a child wider than itself");
            }

            if (currRowWidth + childWidth <= availableWidth) {
                currRowWidth += (childWidth + wPadding);
                rowMinWidth += childWidth;
                currRowMaxHeight = Math.max(currRowMaxHeight, childHeight);
                rowChildCount += 1;
            } else {
                rowInnerPadding.add((availableWidth - rowMinWidth) / (rowChildCount - 1));
                rowHeights.add(currRowMaxHeight + hPadding);
                rowCount += 1;
                currRowWidth = childWidth + wPadding;
                rowMinWidth = childWidth;
                desiredHeight += (currRowMaxHeight + hPadding);
                currRowMaxHeight = childHeight;
                rowChildCount = 1;
            }
        }

        if (rowChildCount > 1) {
            rowInnerPadding.add((availableWidth - rowMinWidth) / (rowChildCount - 1));
        } else {
            rowInnerPadding.add(0);
        }
        rowHeights.add(currRowMaxHeight + hPadding);


        desiredHeight += currRowMaxHeight;
        desiredHeight = desiredHeight + getPaddingTop() + getPaddingBottom();

        setMeasuredDimension(widthMeasureSpec, resolveSizeAndState(desiredHeight, heightMeasureSpec, 0));
    }
}
