package app.subversive.groceryratings.UI;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import app.subversive.groceryratings.VariantLoaderAdapter;

/**
 * Created by rob on 9/1/14.
 */
public class RatingsLayout extends ViewGroup {
    private final static String TAG = RatingsLayout.class.getSimpleName();
    final int rowSpacing = 2;
//    final int maxChildren = 7;
    final int visibleChildren = 2;

    private final ViewGroup.LayoutParams defaultLP =
            new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    VariantLoaderAdapter mAdapter;

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

    private void init(Context context) { }

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
            VariantLoaderAdapter.ViewHolder vh = mAdapter.createViewHolder(this, 0);
            mAdapter.bindViewHolder(vh, x);
            addView(vh.itemView, defaultLP);
        }
    }

    private void onAdapterInvalid() {
        removeAllViews();
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
                onAdapterChanged();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                onAdapterChanged();
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount);
                onAdapterChanged();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                onAdapterChanged();
            }
        });
        onAdapterChanged();
    }
}
