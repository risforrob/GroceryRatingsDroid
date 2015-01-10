package app.subversive.groceryratings.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import app.subversive.groceryratings.Core.TasteTag;
import app.subversive.groceryratings.R;

/**
 * Created by rob on 1/7/15.
 */
public class TagDisplay extends LinearLayout {

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

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.taste_tag, this);
    }


    public void setTag(TasteTag tag) {
        setText(tag.name);
        setValue(tag.value);
    }

    public void setText(String text) {
        ((TextView) findViewById(R.id.tvTagName)).setText(text);
    }

    public void setValue(String text) {
        ((TextView) findViewById(R.id.tvPercent)).setText(text);
    }
}
