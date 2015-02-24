package app.subversive.groceryratings;

import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import app.subversive.groceryratings.Core.Variant;
import app.subversive.groceryratings.UI.Rater;
import app.subversive.groceryratings.UI.SequentialLayout;
import app.subversive.groceryratings.UI.TagDisplay;

/**
 * Created by rob on 1/10/15.
 */
abstract class RecyclingPagerAdapter extends PagerAdapter {
    private View stashedView;

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Log.v("pager", String.format("Instantiate %d", position));
        View view;
        if (stashedView == null) {
            view = inflateView(LayoutInflater.from(container.getContext()));
        } else {
            view = stashedView;
        }
        stashedView = null;
        loadData(position, view);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Log.v("pager", String.format("Destroy %d", position));
        container.removeView((View) object);
        if (stashedView == null) {
            Log.v("pager","stashing view for reuse");
            stashedView = (View) object;
            removeAllChildren(stashedView);
        } else {
            Log.v("pager", "I wanted to stash a view, but one already was, oh well");
        }
    }

    abstract public void removeAllChildren(View v);
//    abstract View loadView(int position, View stashedView, ViewGroup container);
    abstract View inflateView(LayoutInflater inflater);
    abstract void loadData(int position, View view);
}
