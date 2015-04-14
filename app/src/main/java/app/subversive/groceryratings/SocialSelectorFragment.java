package app.subversive.groceryratings;

import android.app.Activity;
import android.app.DialogFragment;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link SocialSelectorFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SocialSelectorFragment extends Fragment {

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SocialSelectorFragment.
     */
    public static SocialSelectorFragment newInstance() {
        SocialSelectorFragment fragment = new SocialSelectorFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public SocialSelectorFragment() {
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
        View root = inflater.inflate(R.layout.fragment_social_selector, container, false);
        final MainWindow activity = (MainWindow) getActivity();
        root.findViewById(R.id.social_background).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
        root.findViewById(R.id.btnFacebook).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.onSocialSelected("facebook");
            }
        });

        root.findViewById(R.id.btnTwitter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.onSocialSelected("twitter");
            }
        });

        root.findViewById(R.id.btnGoogle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.onSocialSelected("google");
            }
        });
        return root;
    }
}
