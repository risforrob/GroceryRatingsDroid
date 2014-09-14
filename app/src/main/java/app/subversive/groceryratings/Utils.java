package app.subversive.groceryratings;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

/**
 * Created by rob on 9/7/14.
 */
public class Utils {
    final static int IMAGE_LONGEST_EDGE = 1024;

    public interface OnFormattedImage {
        public void Callback(Bitmap image);
    }


    private static float dpMultiplier = 1;

    public static void setDPMultiplier(float dpMultiplier) { Utils.dpMultiplier = dpMultiplier; }
    public static int dp2px (int px) { return (int) ((px * dpMultiplier) + 0.5f); }
    public static void setAllPaddingDP (View v, int padding) {
        v.setPadding(dp2px(padding), dp2px(padding), dp2px(padding), dp2px(padding));
    }
    public static void setPaddingDP (View v, int left, int top, int right, int bottom) {
        v.setPadding(dp2px(left), dp2px(top), dp2px(right), dp2px(bottom));
    }

    public static void formatImage(final byte[] imageData, int rotate, final OnFormattedImage callback) {
        new AsyncTask<byte[], Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(byte[]... params) {
                final Bitmap image = BitmapFactory.decodeByteArray(params[0], 0, params[0].length);
                int newWidth, newHeight;
                if (image.getWidth() > image.getHeight()) {
                    newWidth = IMAGE_LONGEST_EDGE;
                    newHeight = Math.round((float) IMAGE_LONGEST_EDGE / image.getWidth() * image.getHeight());
                } else {
                    newHeight = IMAGE_LONGEST_EDGE;
                    newWidth = Math.round((float) IMAGE_LONGEST_EDGE / image.getHeight() * image.getWidth());
                }
                final Bitmap resized = Bitmap.createScaledBitmap(image, newWidth, newHeight, true);
                Log.i("ImageResize", String.format("%d x %d", resized.getWidth(), resized.getHeight()));
                return resized;
            }

            @Override
            protected void onPostExecute(Bitmap image) {
                super.onPostExecute(image);
                callback.Callback(image);
            }
        }.execute(imageData);
    }

}
