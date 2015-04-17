package app.subversive.groceryratings;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.List;

import app.subversive.groceryratings.Core.Variant;
import app.subversive.groceryratings.Core.VariantLoader;
import app.subversive.groceryratings.UI.ProductRatingBar;

/**
 * Created by rob on 4/11/15.
 */
public class VariantLoaderAdapter extends RecyclerView.Adapter<VariantLoaderAdapter.ViewHolder> {
    public interface VariantSelector {
        void onVariantSelected(Variant v);
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ProductRatingBar mbar;
        public ViewHolder(View itemView) {
            super(itemView);
            mbar = (ProductRatingBar) itemView;
        }
    }

    List<VariantLoader> mLoaders;
    VariantSelector mListener;
    final HashMap<ViewHolder, VariantLoader> loaderLookup = new HashMap<>();

    public VariantLoaderAdapter (List<VariantLoader> loaders, VariantSelector listener) {
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
                    mListener.onVariantSelected(loaderLookup.get(vh).getVariant());
                }
            }
        });

        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mbar.setLoader(mLoaders.get(position));
        loaderLookup.put(holder, mLoaders.get(position));
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
