package app.subversive.groceryratings.Adapters;

import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created by rob on 4/3/15.
 */
public class TemplateAdapter<T> implements Adapter {
    public interface ViewBinder<T> {
        public View inflateView(ViewGroup parent);
        public void bindView(View view, T object);
    }

    DataSetObservable mObserver = new DataSetObservable();
    List<T> mThings;
    ViewBinder<T> mViewBinder;

    @Override
    public void registerDataSetObserver(DataSetObserver observer) { mObserver.registerObserver(observer); }
    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) { mObserver.unregisterObserver(observer); }
    public void notifyDataSetChanged() {mObserver.notifyChanged();}
    public void notifyDataSetInvalid() {mObserver.notifyInvalidated();}

    public TemplateAdapter(List<T> things, ViewBinder<T> viewBinder) {
        mThings = things;
        mViewBinder = viewBinder;
    }

    @Override
    public int getCount() {
        return mThings.size();
    }

    @Override
    public T getItem(int position) {
        return mThings.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mViewBinder.inflateView(parent);
        }
        mViewBinder.bindView(convertView, getItem(position));
        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return (mThings == null) || (mThings.isEmpty());
    }
}
