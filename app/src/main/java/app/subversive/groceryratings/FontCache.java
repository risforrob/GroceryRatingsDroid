package app.subversive.groceryratings;

import android.content.Context;
import android.graphics.Typeface;

import java.util.HashMap;

/**
 * Created by rob on 12/27/14.
 */
public class FontCache {
    private static HashMap<String, Typeface> mCache = new HashMap<>();

    public static Typeface get(String name, Context context) {
        Typeface font = mCache.get(name);
        if (font == null) {
            try {
                font = Typeface.createFromAsset(context.getAssets(), name);
            } catch (RuntimeException e) {
                // no font
                return null;
            }
            mCache.put(name, font);
        }
        return font;
    }
}
