package app.subversive.groceryratings;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

import java.util.List;

import app.subversive.groceryratings.Core.Variant;

/**
 * Created by rob on 1/4/15.
 */
public class ProductPageFragment extends Fragment {

    private static final String ARG_INDEX;
    private ViewPager pager;
    static {
        ARG_INDEX = "ARG_INDEX";
    }

    public static ProductPageFragment newInstance(int index) {
        ProductPageFragment newFrag = new ProductPageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_INDEX, index);
        newFrag.setArguments(args);
        return newFrag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        pager = new ViewPager(inflater.getContext());
        pager.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        final VariantPagerAdapter adapter = new VariantPagerAdapter(((MainWindow) getActivity()).getVariants());
        adapter.setOnItemClickedListener(new RatingAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int ratingIndex) {
                ((MainWindow) getActivity()).onRatingSelected(pager.getCurrentItem(), ratingIndex);
            }
        });
        pager.setAdapter(adapter);

        Bundle args = getArguments();
        pager.setCurrentItem(args.getInt(ARG_INDEX));
        return pager;
    }
}
