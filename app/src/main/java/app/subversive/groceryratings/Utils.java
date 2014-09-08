package app.subversive.groceryratings;

import android.view.View;

/**
 * Created by rob on 9/7/14.
 */
public class Utils {
    private static float dpMultiplier = 1;

    public static void setDPMultiplier(float dpMultiplier) { Utils.dpMultiplier = dpMultiplier; }
    public static int dp2px (int px) { return (int) ((px * dpMultiplier) + 0.5f); }
    public static void setAllPaddingDP (View v, int padding) {
        v.setPadding(dp2px(padding), dp2px(padding), dp2px(padding), dp2px(padding));
    }
    public static void setPaddingDP (View v, int left, int top, int right, int bottom) {
        v.setPadding(dp2px(left), dp2px(top), dp2px(right), dp2px(bottom));
    }

}
