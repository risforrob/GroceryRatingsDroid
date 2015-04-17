package app.subversive.groceryratings.UI;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;

import app.subversive.groceryratings.Utils;
import app.subversive.groceryratings.VariantLoaderAdapter;

/**
 * Created by rob on 9/1/14.
 */
public class RatingsLayout extends ViewGroup {
    private final static String TAG = RatingsLayout.class.getSimpleName();
    int rowSpacing;
    final int visibleChildren = 2;

    private final ViewGroup.LayoutParams defaultLP =
            new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    VariantLoaderAdapter mAdapter;
    HashMap<View, VariantLoaderAdapter.ViewHolder> mHolders = new HashMap<>();
    ArrayDeque<VariantLoaderAdapter.ViewHolder> mExtraHolders = new ArrayDeque<>();


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
        rowSpacing = Utils.dp2px(3);
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



    private void onAdapterChanged() {
        Log.d(TAG, "Adapter Updated");
        removeAllViews();
        for (int x = 0 ; x < mAdapter.getItemCount() ; x++ ) {
            addView(x);
        }
    }

    private void onAdapterInvalid() {
        removeAllViews();
    }

    private void addView(int position) {
        VariantLoaderAdapter.ViewHolder holder = mExtraHolders.poll();
        if (holder == null) {
            holder = mAdapter.createViewHolder(this, 0);
            mHolders.put(holder.itemView, holder);
        }
        mAdapter.bindViewHolder(holder, position);
        addView(holder.itemView, position, defaultLP);
    }

    public void setAdapter(VariantLoaderAdapter adapter) {
        mAdapter = adapter;
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                onAdapterChanged();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                super.onItemRangeChanged(positionStart, itemCount);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                for (int x = 0; x < itemCount; x++) {
                    addView(positionStart + x);
                }
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount);
                if (itemCount != 1) {
                    throw new RuntimeException("Moving more than one item is not allowed");
                }
                if (fromPosition != toPosition) {
                    View v = getChildAt(fromPosition);
                    removeView(v);
                    if (toPosition > fromPosition) {
                        toPosition--;
                    }
                    addView(v, toPosition, defaultLP);
                }
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                for (int x = 0; x < itemCount; x++) {
                    mExtraHolders.push(mHolders.get(getChildAt(positionStart + x)));
                }
                removeViews(positionStart, itemCount);
            }
        });
        onAdapterChanged();
    }

    public void flashTop() {
        Log.d(TAG, "2 " + String.valueOf(getChildCount()));
        if (getChildCount() > 0) {
            ((ProductRatingBar) getChildAt(0)).flash();
        }
    }
}
