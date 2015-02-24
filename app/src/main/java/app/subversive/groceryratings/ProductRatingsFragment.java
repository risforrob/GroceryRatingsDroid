package app.subversive.groceryratings;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

/**
 * Created by rob on 2/22/15.
 */
public class ProductRatingsFragment extends Fragment {
    private static final String VARIANT_INDEX, RATING_INDEX;
    static {
        VARIANT_INDEX = "VARIANT_INDEX";
        RATING_INDEX = "RATING_INDEX";
    }

    final static Gson gson = new Gson();

    public static ProductRatingsFragment newInstance(int variantIndex, int ratingIndex) {
        ProductRatingsFragment newFrag = new ProductRatingsFragment();
        Bundle args = new Bundle();
        args.putInt(VARIANT_INDEX, variantIndex);
        args.putInt(RATING_INDEX, ratingIndex);
        newFrag.setArguments(args);
        return newFrag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();

        final ViewPager pager = new ViewPager(inflater.getContext());
        pager.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ProductReviewsPagerAdapter adapter = new ProductReviewsPagerAdapter(((MainWindow) getActivity()).getRatings(args.getInt(VARIANT_INDEX, 0)));
        pager.setAdapter(adapter);
        pager.setCurrentItem(args.getInt(RATING_INDEX, 0));
        return pager;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
    }
}
