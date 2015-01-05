package app.subversive.groceryratings;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import app.subversive.groceryratings.Core.Variant;
import app.subversive.groceryratings.UI.Rater;

/**
 * Created by rob on 1/4/15.
 */
public class ProductPageFragment extends Fragment {

    private static final String ARG_NAME, ARG_NUMSTARS, ARG_DESCRIPTION, ARG_TAGS, ARG_NUM_REVIEWS;
    static {
        ARG_NAME = "NAME";
        ARG_NUMSTARS = "NUMSTARS";
        ARG_DESCRIPTION ="DESCRIPTION";
        ARG_TAGS = "TAGS";
        ARG_NUM_REVIEWS = "NUM_REVIEWS";
    }

    public static ProductPageFragment newInstance(Variant v) {
        ProductPageFragment newFrag = new ProductPageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NAME,  v.getName());
        args.putInt(ARG_NUMSTARS, v.getNumStars());
        args.putInt(ARG_NUM_REVIEWS, v.getRatingCount());
        args.putString(ARG_DESCRIPTION, v.getDescription());
        args.putParcelableArrayList(ARG_TAGS, v.getTasteTags());
        newFrag.setArguments(args);
        return newFrag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.product_page, container, false);
        Bundle args = getArguments();
        ((TextView) v.findViewById(R.id.productName)).setText(args.getString(ARG_NAME));
        ((TextView) v.findViewById(R.id.productNumRatings)).setText(Variant.formatRatingString(args.getInt(ARG_NUM_REVIEWS)));
        ((Rater) v.findViewById(R.id.productStars)).setRating(args.getInt(ARG_NUMSTARS));
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


}
