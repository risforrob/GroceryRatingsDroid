package app.subversive.groceryratings;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;

import app.subversive.groceryratings.Core.Rating;
import app.subversive.groceryratings.Core.TasteTag;
import app.subversive.groceryratings.Core.Variant;
import app.subversive.groceryratings.UI.Rater;
import app.subversive.groceryratings.UI.RatingDisplay;
import app.subversive.groceryratings.UI.SequentialLayout;
import app.subversive.groceryratings.UI.TagDisplay;

/**
 * Created by rob on 1/4/15.
 */
public class ProductPageFragment extends Fragment {

    private static final String ARG_NAME, ARG_NUMSTARS, ARG_DESCRIPTION, ARG_TAGS, ARG_NUM_REVIEWS, ARG_VARIANT;
    static {
        ARG_NAME = "NAME";
        ARG_NUMSTARS = "NUMSTARS";
        ARG_DESCRIPTION ="DESCRIPTION";
        ARG_TAGS = "TAGS";
        ARG_NUM_REVIEWS = "NUM_REVIEWS";
        ARG_VARIANT = "VARIANT";
    }

    final static Gson gson = new Gson();

    public static ProductPageFragment newInstance(Variant v) {
        ProductPageFragment newFrag = new ProductPageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_VARIANT, gson.toJson(v));
//        args.putString(ARG_NAME,  v.getName());
//        args.putInt(ARG_NUMSTARS, v.getNumStars());
//        args.putInt(ARG_NUM_REVIEWS, v.getRatingCount());
//        args.putString(ARG_DESCRIPTION, v.getDescription());
//        args.putParcelableArray(ARG_TAGS, v.getTasteTags());
        newFrag.setArguments(args);
        return newFrag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        LinearLayout v = (LinearLayout) inflater.inflate(R.layout.product_page, container, false);
        Bundle args = getArguments();
        Variant variant = gson.fromJson(args.getString(ARG_VARIANT, "{}"), Variant.class);

        ((TextView) v.findViewById(R.id.productName)).setText(variant.getName());
        ((TextView) v.findViewById(R.id.productNumRatings)).setText(Variant.formatRatingString(variant.getRatingCount()));
        ((Rater) v.findViewById(R.id.productStars)).setRating(variant.getNumStars());

        SequentialLayout tagLayout = ((SequentialLayout) v.findViewById(R.id.layoutTags));
        final TasteTag[] tags = variant.getTasteTags();
        if (tags != null) {
            for (TasteTag tag : tags) {
                TagDisplay td = new TagDisplay(tagLayout.getContext());
                td.setTag(tag);
                tagLayout.addView(td);
            }
        }

        LinearLayout ratingHolder = (LinearLayout) v.findViewById(R.id.ratingHolder);
        for (Rating r : variant.ratings) {
            RatingDisplay rd = new RatingDisplay(v.getContext());
            rd.setRating(r);
            Utils.setPaddingDP(rd, 8, 8, 8, 8);
            ratingHolder.addView(rd);
        }
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}
