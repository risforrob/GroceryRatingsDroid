package app.subversive.groceryratings;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
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
    static {
        ARG_INDEX = "ARG_INDEX";
    }

    final static Gson gson = new Gson();

    public static ProductPageFragment newInstance(int index) {
        ProductPageFragment newFrag = new ProductPageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_INDEX, index);
        newFrag.setArguments(args);
        return newFrag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewPager pager = new ViewPager(inflater.getContext());
        pager.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        pager.setAdapter(new VariantPagerAdapter(((MainWindow) getActivity()).getVariants()));

        Bundle args = getArguments();
        if (args != null) {
            pager.setCurrentItem(args.getInt(ARG_INDEX, 0));
        }
        return pager;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
    }
}
