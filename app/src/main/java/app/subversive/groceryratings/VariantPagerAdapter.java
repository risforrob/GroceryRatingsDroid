package app.subversive.groceryratings;

import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import app.subversive.groceryratings.Core.Rating;
import app.subversive.groceryratings.Core.TasteTag;
import app.subversive.groceryratings.Core.Variant;
import app.subversive.groceryratings.UI.Rater;
import app.subversive.groceryratings.UI.RatingDisplay;
import app.subversive.groceryratings.UI.SequentialLayout;
import app.subversive.groceryratings.UI.TagDisplay;

/**
 * Created by rob on 1/10/15.
 */
public class VariantPagerAdapter extends PagerAdapter {
    View stashedView;
    List<Variant> variants;

    public VariantPagerAdapter(List<Variant> variants) {
        this.variants = variants;
    }

    private View loadViewFromVariant(Variant variant, View existingView, LayoutInflater inflater) {
        View root;
        if (existingView == null) {
            Log.v("pager", "inflating new root");
            root = inflater.inflate(R.layout.product_page, null);
        } else {
            Log.v("pager", "recycling view");
            root = existingView;
        }

        ((TextView) root.findViewById(R.id.productName)).setText(variant.getName());
        ((TextView) root.findViewById(R.id.productNumRatings)).setText(Variant.formatRatingString(variant.getRatingCount()));
        ((Rater) root.findViewById(R.id.productStars)).setRating(variant.getNumStars());

        SequentialLayout tagLayout = ((SequentialLayout) root.findViewById(R.id.layoutTags));
        final TasteTag[] tags = variant.getTasteTags();
        if (tags != null) {
            for (TasteTag tag : tags) {
                TagDisplay td = new TagDisplay(tagLayout.getContext());
                td.setTag(tag);
                tagLayout.addView(td);
            }
        }

        LinearLayout ratingHolder = (LinearLayout) root.findViewById(R.id.ratingHolder);
        for (Rating r : variant.ratings) {
            RatingDisplay rd = new RatingDisplay(root.getContext());
            rd.setRating(r);
            Utils.setPaddingDP(rd, 8, 8, 8, 8);
            ratingHolder.addView(rd);
        }
        return root;
    }

    @Override
    public int getCount() {
        return (variants != null) ? variants.size() : 0;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Log.v("pager", String.format("Instantiate %d", position));

        View v = loadViewFromVariant(variants.get(position), stashedView, LayoutInflater.from(container.getContext()));
        stashedView = null;
        container.addView(v);
        return v;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Log.v("pager", String.format("Destroy %d", position));
        container.removeView((View) object);
        if (stashedView == null) {
            Log.v("pager","stashing view for reuse");
            stashedView = (View) object;
            ((SequentialLayout) stashedView.findViewById(R.id.layoutTags)).removeAllViews();
            ((LinearLayout) stashedView.findViewById(R.id.ratingHolder)).removeAllViews();
        } else {
            Log.v("pager", "I wanted to stash a view, but one already was, oh well");
        }
    }
}
