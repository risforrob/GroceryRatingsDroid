package app.subversive.groceryratings;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.subversive.groceryratings.Core.GRData;

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
        final MainWindow activity = (MainWindow) getActivity();
        VariantPagerAdapter.AddReviewCallback reviewCallback = new VariantPagerAdapter.AddReviewCallback() {
            @Override
            public void onAddReview() {
                activity.onAddReview(pager.getCurrentItem());
            }
        };

        pager = new ViewPager(inflater.getContext());
        pager.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        final VariantPagerAdapter adapter = new VariantPagerAdapter(GRData.getInstance().getVariants(), reviewCallback, activity.isSocialConnected());
        adapter.setOnItemClickedListener(new RatingAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int ratingIndex) {
                ((MainWindow) getActivity()).onRatingSelected(pager.getCurrentItem(), ratingIndex);
            }
        });
        pager.setAdapter(adapter);

        int index;
        if (savedInstanceState != null) {
            index = savedInstanceState.getInt(ARG_INDEX);
        } else {
            Bundle args = getArguments();
            index = args.getInt(ARG_INDEX);
        }
        pager.setCurrentItem(index);
        return pager;
    }

    @Override
    public void onPause() {
        super.onPause();
        getArguments().putInt(ARG_INDEX, pager.getCurrentItem());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_INDEX, pager.getCurrentItem());
    }
}
