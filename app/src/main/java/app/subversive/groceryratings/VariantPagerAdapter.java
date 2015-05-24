package app.subversive.groceryratings;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;

import app.subversive.groceryratings.Core.Variant;
import app.subversive.groceryratings.UI.Rater;
import app.subversive.groceryratings.UI.SequentialLayout;
import app.subversive.groceryratings.UI.TagDisplay;

/**
 * Created by rob on 1/10/15.
 */
public class VariantPagerAdapter extends RecyclingPagerAdapter {
    private final String TAG = VariantPagerAdapter.class.getSimpleName();
    public interface AddReviewCallback {
        void onAddReview();
    }

    List<Variant> variants;
    RatingAdapter[] ratingAdapters;
    RecyclerView recycler;
    View.OnClickListener addReviewListener;
    int currentIndex;
    boolean loggedIn;

    public VariantPagerAdapter(List<Variant> variants, final AddReviewCallback callback, boolean loggedIn) {
        this.variants = variants;
        this.loggedIn = loggedIn;
        ratingAdapters = new RatingAdapter[variants.size()];
        for (int i = 0 ; i < variants.size() ; i++) {
            ratingAdapters[i] = new RatingAdapter(variants.get(i).ratings);
        }

        addReviewListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onAddReview();
            }
        };
    }

    @Override
    View inflateView(LayoutInflater inflater) {
        Log.v("pager", "inflating new root");
        View root = inflater.inflate(R.layout.product_page, null);
        RecyclerView recycler = (RecyclerView) root.findViewById(R.id.ratingHolder);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        Button addReview = (Button) root.findViewById(R.id.btnAddReview);
        addReview.setOnClickListener(addReviewListener);
        addReview.setText((loggedIn) ? "Add a review" : "Add a review (Requires Login)");
        return root;
    }

    @Override
    void loadData(int position, final View root) {
        currentIndex = position;
        final Variant variant = variants.get(position);
        recycler = (RecyclerView) root.findViewById(R.id.ratingHolder);

        ((TextView) root.findViewById(R.id.productName)).setText(variant.getName());
        ((TextView) root.findViewById(R.id.productNumRatings)).setText(Variant.formatRatingString(variant.getRatingCount()));
        ((Rater) root.findViewById(R.id.productStars)).setRating(variant.getNumStars());

        final ImageView image = (ImageView) root.findViewById(R.id.prodImageButton);

        image.post(new Runnable() {
                       @Override
                       public void run() {
                           Picasso.with(root.getContext()).load(variant.getImageURL(Math.max(image.getHeight(), image.getWidth()))).into(image);
                       }
                   });


        SequentialLayout tagLayout = ((SequentialLayout) root.findViewById(R.id.layoutTags));
        final List<Map.Entry<String, Integer>> wordscore = variant.getSortedWordscore();

        if (wordscore != null) {
            for (Map.Entry<String, Integer> elem :  wordscore) {
                TagDisplay td = new TagDisplay(root.getContext());
                td.setText(elem.getKey());
                td.setValue(String.valueOf(elem.getValue()));
                tagLayout.addView(td);
            }
        }
        recycler.setAdapter(ratingAdapters[position]);
    }

    @Override
    public int getCount() {
        return (variants != null) ? variants.size() : 0;
    }

    @Override
    public void removeAllChildren(View v) {
        ((SequentialLayout) v.findViewById(R.id.layoutTags)).removeAllViews();
    }


    public void setOnItemClickedListener(RatingAdapter.ItemClickListener listener) {
        for (RatingAdapter adapter : ratingAdapters) {
            adapter.setItemClickListener(listener);
        }
    }
}
