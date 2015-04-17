package app.subversive.groceryratings;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link SocialSelectorFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SocialSelectorFragment extends DialogFragment {
    static class FancyDialog extends Dialog {

        public FancyDialog(Context context) {
            super(context);
        }

        protected FancyDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
            super(context, cancelable, cancelListener);
        }

        public FancyDialog(Context context, int theme) {
            super(context, theme);
        }

        @Override
        protected void onStart() {
            super.onStart();
            getWindow().getDecorView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("FOO", "click");
                }
            });
        }
    }

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
    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        return super.onCreateDialog(savedInstanceState);
        return new FancyDialog(getActivity(), getTheme());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setStyle(STYLE_NO_TITLE, R.style.CustomDialog);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_social_selector, container, false);
        final MainWindow activity = (MainWindow) getActivity();
        root.findViewById(R.id.btnFacebook).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.onSocialSelected("facebook");
                dismiss();
            }
        });

        root.findViewById(R.id.btnTwitter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.onSocialSelected("twitter");
                dismiss();
            }
        });

        root.findViewById(R.id.btnGoogle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.onSocialSelected("google");
                dismiss();
            }
        });
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0));
    }
}
