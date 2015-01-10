package app.subversive.groceryratings;

import android.app.Activity;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

import app.subversive.groceryratings.Core.Variant;

/**
 * Created by rob on 1/7/15.
 */
public class HistoryManager {
    private final static String HISTORY_FILE = "HISTORY_FILE";
    private final static String TAG = "HistoryManager";
    private static final Gson gson = new Gson();
    public static Variant[] readHistory(Activity activity) {

        try {
            FileReader fr = new FileReader(new File(activity.getFilesDir(), HISTORY_FILE));
            Variant[] variants = gson.fromJson(fr, Variant[].class);
            fr.close();
            return variants;
        } catch (FileNotFoundException e) {
            Log.i(TAG, "No history to load");
            // do nothing
        } catch (IOException e) {
            Log.i(TAG, "error reading history file");
            // do nothing
        }
        return null;
    }

    public static void writeHistory(Activity activity, List<Variant> variants) {
        String jsonstring = gson.toJson(variants);
        try {
            FileOutputStream out = activity.openFileOutput(HISTORY_FILE, Activity.MODE_PRIVATE);
            out.write(jsonstring.getBytes());
            out.close();
        } catch (FileNotFoundException e) {
            Log.i(TAG, "No file to write history");
            // fail elegantly
        } catch (IOException e) {
            Log.i(TAG, "error writing history");
            // fail elegantly
        }
    }
}
