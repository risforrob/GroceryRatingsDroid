package app.subversive.groceryratings.UI;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;

import app.subversive.groceryratings.FontCache;
import app.subversive.groceryratings.R;

/**
 * Created by rob on 12/27/14.
 */
public class FontTextView extends TextView {
    public FontTextView(Context context) {
        super(context);
    }

    public FontTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.FontTextView);
        Typeface font = FontCache.get(arr.getString(R.styleable.FontTextView_font), context);
        if (font !=null) {
            setTypeface(font);
        }
    }
}
