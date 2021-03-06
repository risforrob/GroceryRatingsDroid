package app.subversive.groceryratings.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import app.subversive.groceryratings.Core.TasteTag;
import app.subversive.groceryratings.R;

/**
 * Created by rob on 1/7/15.
 */
public class TagDisplay extends LinearLayout {
    private static String TAG = TagDisplay.class.getSimpleName();
    public interface OnCancelListener {
        void onCancel(TagDisplay td);
    }

    OnCancelListener mListener;
    TextView tagName, tagPercent;
    ImageButton tagCancel;
    private boolean invalid;

    public TagDisplay(Context context) {
        super(context);
        init();
    }

    public TagDisplay(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TagDisplay(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void setOnCancelListener(OnCancelListener listener) {
        mListener = listener;
    }

    public void removeOnCancelListener() {
        mListener = null;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeOnCancelListener();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.taste_tag, this);
        tagName = (TextView) findViewById(R.id.tvTagName);
        tagPercent = (TextView) findViewById(R.id.tvPercent);
        tagCancel = (ImageButton) findViewById(R.id.tagCancel);
    }


    public void setTag(TasteTag tag) {
        setText(tag.name);
        setValue(tag.value);
    }

    public void setText(String text) {
        tagName.setText(text);
    }

    public void setValue(String text) {
        tagPercent.setText(text);
    }

    public void setTagInvalid() {
        tagName.setBackgroundResource(R.color.InvalidTagPrimary);
        tagPercent.setBackgroundResource(R.color.InvalidTagSecondary);
        tagCancel.setBackgroundResource(R.color.InvalidTagSecondary);
        invalid = true;
    }

    public void setCancelMode() {
        tagPercent.setVisibility(GONE);
        tagCancel.setVisibility(VISIBLE);

        tagCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onCancel(TagDisplay.this);
                }
            }
        });

        tagCancel.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (invalid) {
                            tagName.setBackgroundResource(R.color.InvalidTagPrimarySelected);
                            tagCancel.setBackgroundResource(R.color.InvalidTagSecondarySelected);
                        } else {
                            tagName.setBackgroundResource(R.color.TagPrimarySelected);
                            tagCancel.setBackgroundResource(R.color.TagSecondarySelected);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (invalid) {
                            tagName.setBackgroundResource(R.color.InvalidTagPrimary);
                            tagCancel.setBackgroundResource(R.color.InvalidTagSecondary);
                        } else {
                            tagName.setBackgroundResource(R.color.TagPrimary);
                            tagCancel.setBackgroundResource(R.color.TagSecondary);
                        }
                        break;
                }
                return false;
            }
        });
    }

    public String getTagName() {
        return tagName.getText().toString();
    }
}
