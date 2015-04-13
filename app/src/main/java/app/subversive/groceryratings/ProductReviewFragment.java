package app.subversive.groceryratings;

import android.app.Activity;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import app.subversive.groceryratings.Core.Rating;
import app.subversive.groceryratings.UI.Rater;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link ProductReviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProductReviewFragment extends Fragment {
//    private OnFragmentInteractionListener mListener;
    private final static String TAG = ProductReviewFragment.class.getSimpleName();
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ProductReviewFragment.
     */
    public static ProductReviewFragment newInstance() {
        ProductReviewFragment fragment = new ProductReviewFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public ProductReviewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_product_review, container, false);
        final EditText etRating = (EditText) root.findViewById(R.id.etRating);
        final Button submitButton = (Button) root.findViewById(R.id.btnSubmitReview);

        etRating.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                EditText et = (EditText) v;
                if (hasFocus || et.getText().length() > 0) {
                    et.setGravity(Gravity.START | Gravity.TOP);
                    et.setHint(null);
                } else {
                    et.setGravity(Gravity.TOP | Gravity.CENTER);
                }
            }
        });

        etRating.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                submitButton.setEnabled(s.length() > 0);
            }
        });
        final Rater rater = (Rater) root.findViewById(R.id.ratingRater);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.hideKeyboard(getActivity(), etRating);
                ((MainWindow) getActivity()).addRating(rater.getRating(), etRating.getText().toString(), true);
            }
        });
        return root;
    }
}
