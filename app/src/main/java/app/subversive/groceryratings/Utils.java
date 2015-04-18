package app.subversive.groceryratings;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

/**
 * Created by rob on 9/7/14.
 */
public class Utils {
    final static int IMAGE_LONGEST_EDGE = 1024;

    public interface OnFormattedImage {
        void Callback(byte[] imageData);
    }

    public static class TypedFileByteArray extends TypedByteArray {
        String filename;
        public TypedFileByteArray(String mimeType, String filename, byte[] bytes) {
            super(mimeType, bytes);
            this.filename = filename;
        }

        @Override
        public String fileName() {
            return filename;
        }
    }


    private static float dpMultiplier = 3;

    public static void setDPMultiplier(float dpMultiplier) {
        Utils.dpMultiplier = dpMultiplier;
    }
    public static int dp2px (int dp) { return (int) ((dp * dpMultiplier) + 0.5f); }
    public static void setAllPaddingDP (View v, int padding) {
        setPaddingDP(v, padding, padding, padding, padding);
    }
    public static void setPaddingDP (View v, int left, int top, int right, int bottom) {
        v.setPadding(dp2px(left), dp2px(top), dp2px(right), dp2px(bottom));
    }

    public static void formatImage(final byte[] imageData, final OnFormattedImage callback) {
        new AsyncTask<byte[], Void, byte[]>() {
            @Override
            protected byte[] doInBackground(byte[]... params) {
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

                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                resized.compress(Bitmap.CompressFormat.JPEG, 98, byteStream);
                return byteStream.toByteArray();
            }

            @Override
            protected void onPostExecute(byte[] imageBytes) {
                super.onPostExecute(imageBytes);
                callback.Callback(imageBytes);
            }
        }.execute(imageData);
    }

    public static void hideKeyboard(Context context, View v) {
        ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE))
            .hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
}
