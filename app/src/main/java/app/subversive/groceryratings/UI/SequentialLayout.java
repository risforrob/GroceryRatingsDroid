package app.subversive.groceryratings.UI;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import app.subversive.groceryratings.R;

/**
 * Created by rob on 1/4/15.
 */
public class SequentialLayout extends ViewGroup {
    private int maxRows;
    private boolean justified, centered;
    private int wPadding = 0;
    private int hPadding = 0;

    ArrayList<Integer> rowInnerPadding = new ArrayList<>();
    ArrayList<Integer> rowHeights = new ArrayList<>();
    ArrayList<Integer> rowOffset = new ArrayList<>();



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
        justified = arr.getBoolean(R.styleable.SequentialLayout_justifiedSpacing, true);
        centered = arr.getBoolean(R.styleable.SequentialLayout_centered, false);
        wPadding = arr.getDimensionPixelSize(R.styleable.SequentialLayout_minimumHorizontalPadding, 0);
        hPadding = arr.getDimensionPixelSize(R.styleable.SequentialLayout_verticalPadding, 0);
    }

    private int getLeftStart(int rowIndex) {
        return getPaddingLeft() + ((!justified && centered) ? rowOffset.get(rowIndex) : 0);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (getChildCount() == 0) { return; }

        int rowLeft = getLeftStart(0);
        int top = getPaddingTop();
        int availWidth = (r - l) - getPaddingLeft() - getPaddingRight();
        int rowNum = 0;
        int currRowPadding = rowInnerPadding.get(0);
        for (int i = 0 ; i < getChildCount() ; i++ ) {
            final View child = getChildAt(i);

            if (rowLeft + child.getMeasuredWidth() > availWidth) {
                rowNum += 1;
                if (maxRows > 0 && rowNum >= maxRows) {
                    break;
                }
                top = top + rowHeights.get(rowNum) + hPadding;
                currRowPadding = rowInnerPadding.get(rowNum);
                rowLeft = getLeftStart(rowNum);
            }

            child.layout(rowLeft, top, rowLeft + child.getMeasuredWidth(), top + child.getMeasuredHeight());
            rowLeft = rowLeft + child.getMeasuredWidth() + currRowPadding;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (getChildCount() == 0) {
            setMeasuredDimension(0, 0);
            return;
        }
        int desiredHeight = 0;
        int availableWidth = View.MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        int currRowMaxHeight = 0;
        int rowCount = 1;
        int currRowWidth = 0;
        int rowMinWidth = 0;
        int rowChildCount = 0;
        rowInnerPadding.clear();
        rowHeights.clear();
        rowOffset.clear();
        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);

            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            if (childWidth > availableWidth) {
                throw new RuntimeException("SequentialLayout cannot layout a child wider than itself");
            }

            if (currRowWidth + childWidth > availableWidth) {

                // wrap to the next line. Finish computations for current line
                if (justified) {
                    rowInnerPadding.add((availableWidth - rowMinWidth) / (rowChildCount - 1));
                } else {
                    rowInnerPadding.add(wPadding);

                    if (centered) {
                        rowOffset.add((availableWidth - rowMinWidth - ((rowChildCount - 1) * wPadding))/2);
                    }
                }

                rowHeights.add(currRowMaxHeight);
                desiredHeight += (currRowMaxHeight);

                rowChildCount = 0;
                currRowWidth = 0;
                rowMinWidth = 0;
                currRowMaxHeight = 0;
                if (maxRows > 0 && rowCount == maxRows) {
                    break;
                } else {
                    rowCount += 1;
                }
            }

            currRowWidth += (childWidth + ((rowChildCount == 0) ? wPadding : 0));
            rowMinWidth += childWidth;
            currRowMaxHeight = Math.max(currRowMaxHeight, childHeight);
            rowChildCount += 1;
        }

        if (rowChildCount > 1) {
            if (justified) {
                rowInnerPadding.add((availableWidth - rowMinWidth) / (rowChildCount - 1));
            } else {
                rowInnerPadding.add(wPadding);
                if (centered) {
                    rowOffset.add((availableWidth - rowMinWidth - ((rowChildCount - 1) * wPadding))/2);
                }
            }
        } else {
            rowInnerPadding.add(0);
            if (centered) {
                rowOffset.add((availableWidth - rowMinWidth - ((rowChildCount - 1) * wPadding))/2);
            }
        }
        rowHeights.add(currRowMaxHeight);


        desiredHeight += currRowMaxHeight;
        desiredHeight = desiredHeight + ((rowCount - 1) * hPadding) + getPaddingTop() + getPaddingBottom();

        setMeasuredDimension(widthMeasureSpec, resolveSizeAndState(desiredHeight, heightMeasureSpec, 0));
    }
}
