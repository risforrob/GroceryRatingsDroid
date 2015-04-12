package app.subversive.groceryratings;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.List;

import app.subversive.groceryratings.Core.VariantLoader;
import app.subversive.groceryratings.UI.ProductRatingBar;

/**
 * Created by rob on 4/11/15.
 */
public class VariantLoaderAdapter extends RecyclerView.Adapter<VariantLoaderAdapter.ViewHolder> {
    public static class ViewHolder extends RecyclerView.ViewHolder {

        ProductRatingBar mbar;
        public ViewHolder(View itemView) {
            super(itemView);
            mbar = (ProductRatingBar) itemView;
        }
    }

    List<VariantLoader> mLoaders;
    RatingAdapter.ItemClickListener mListener;
    final HashMap<ViewHolder, Integer> indexer = new HashMap<>();

    public VariantLoaderAdapter (List<VariantLoader> loaders, RatingAdapter.ItemClickListener listener) {
        mLoaders = loaders;
        mListener = listener;
        setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = new ProductRatingBar(parent.getContext());
        final ViewHolder vh = new ViewHolder(v);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(indexer.get(vh));
                }
            }
        });

        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mbar.setLoader(mLoaders.get(position));
        indexer.put(holder, position);
    }

    @Override
    public int getItemCount() {
        return mLoaders.size();
    }

    @Override
    public long getItemId(int position) {
        return Long.valueOf(mLoaders.get(position).getBarcode());
    }
}
