package app.subversive.groceryratings;

import android.app.Activity;
import android.database.DataSetObserver;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.MultiAutoCompleteTextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import app.subversive.groceryratings.Core.GRData;
import app.subversive.groceryratings.Core.Rating;
import app.subversive.groceryratings.UI.Rater;
import app.subversive.groceryratings.UI.SequentialLayout;
import app.subversive.groceryratings.UI.TagDisplay;

public class ProductReviewFragment extends Fragment {
    private final static String TAG = ProductReviewFragment.class.getSimpleName();
    Set<String> allTags;
    ArrayList<String> availableTags;
    ArrayAdapter<String> mAdapter;
    SequentialLayout mSeqLayout;
    AutoCompleteTextView tagsACTV;

    EditText etRating;
    Button submitButton;
    View btnTagHelp;
    FrameLayout flAddTag;
    Rater rater;

    TagDisplay.OnCancelListener onCancelListener = new TagDisplay.OnCancelListener() {
        @Override
        public void onCancel(TagDisplay td) {
            if (mSeqLayout != null) {
                mSeqLayout.removeView(td);
                if (allTags.contains(td.getTagName())) {
                    mAdapter.add(td.getTagName());
                }
                if (mSeqLayout.getChildCount() == 1) {
                    showTagHelp();
                }
            }
        }
    };

    public static ProductReviewFragment newInstance() {
        ProductReviewFragment fragment = new ProductReviewFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public ProductReviewFragment() {
        super();
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
        etRating = (EditText) root.findViewById(R.id.etRating);
        submitButton = (Button) root.findViewById(R.id.btnSubmitReview);
        mSeqLayout = (SequentialLayout) root.findViewById(R.id.ReviewSequentialLayout);
        flAddTag = (FrameLayout) root.findViewById(R.id.flAddTag);
        btnTagHelp = root.findViewById(R.id.btnTagHelp);

        rater = (Rater) root.findViewById(R.id.ratingRater);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.hideKeyboard(getActivity(), etRating);
                ((MainWindow) getActivity()).addRating(rater.getRating(), etRating.getText().toString(), true);
            }
        });


        tagsACTV = (AutoCompleteTextView) root.findViewById(R.id.newTasteTags);
        tagsACTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                addNewTag((String) parent.getItemAtPosition(position));
            }
        });

        tagsACTV.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if ((s.length() > 1) && (s.charAt(s.length() - 1) == ' ')) {
                    addNewTag(s.subSequence(0, s.length() - 1).toString());
                } else if ((s.length() == 1) && (s.charAt(0) == ' ')) {
                    tagsACTV.setText(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        allTags = GRData.getInstance().getTasteTags();
        availableTags = new ArrayList<>(allTags);
        mAdapter = new ArrayAdapter<>(root.getContext(), android.R.layout.simple_dropdown_item_1line, availableTags);
        mAdapter.setNotifyOnChange(true);
        tagsACTV.setAdapter(mAdapter);
        return root;
    }

    private void addNewTag(String tag) {
        Log.d(TAG, tag);
        if (mSeqLayout.getChildCount() == 1) {
            hideTagHelp();
        }
        TagDisplay td = new TagDisplay(getActivity());
        td.setText(tag);
        td.setCancelMode();
        td.setOnCancelListener(onCancelListener);
        if (!allTags.contains(tag)) {
            td.setTagInvalid();
        } else {
            mAdapter.remove(tag);
        }
        mSeqLayout.addView(td, mSeqLayout.getChildCount() - 1);
        tagsACTV.setText(null);
    }

    private void showTagHelp() {
        btnTagHelp.setVisibility(View.VISIBLE);
        ViewGroup.LayoutParams lp = flAddTag.getLayoutParams();
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        flAddTag.setLayoutParams(lp);
    }

    private void hideTagHelp() {
        btnTagHelp.setVisibility(View.GONE);
        ViewGroup.LayoutParams lp = flAddTag.getLayoutParams();
        lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        flAddTag.setLayoutParams(lp);
    }
}
