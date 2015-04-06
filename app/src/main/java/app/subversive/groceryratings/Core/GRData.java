package app.subversive.groceryratings.Core;

import android.app.Activity;
import android.util.Log;

import com.google.gson.Gson;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import app.subversive.groceryratings.Adapters.TemplateAdapter;

/**
 * Created by rob on 4/3/15.
 */
public class GRData {
    private final static String HISTORY_FILE = "HISTORY_FILE";
    private final static String TAG = "HistoryManager";
    private static final Gson gson = new Gson();

    private static GRData instance;

    public static GRData getInstance() {
        if (instance == null) {
            instance = new GRData();
        }
        return instance;
    }

    private ArrayList<VariantLoader> mVariantLoaders;

    private TemplateAdapter<VariantLoader> activeVLAdapter;

    private GRData() {
        mVariantLoaders = new ArrayList<>();
    }

    public void loadVariant(String barcode) {
        VariantLoader loader = new VariantLoader(barcode);
        mVariantLoaders.add(loader);
        loader.load();
        if (activeVLAdapter != null) { activeVLAdapter.notifyDataSetChanged(); }
    }

    public void setActiveVariantLoaderAdapter(TemplateAdapter<VariantLoader> adapter) {
        activeVLAdapter = adapter;
    }

    public void loadHistory(Activity activity) {
        mVariantLoaders = new ArrayList<>();
        try {
            FileReader fr = new FileReader(new File(activity.getFilesDir(), HISTORY_FILE));
            for (Variant variant : gson.fromJson(fr, Variant[].class)) {
                mVariantLoaders.add(new VariantLoader(variant));
            }
            fr.close();
        } catch (FileNotFoundException e) {
            Log.i(TAG, "No history to load");
            // do nothing
        } catch (IOException e) {
            Log.i(TAG, "error reading history file");
            // do nothing
        }
    }

    public void writeHistory(Activity activity) {
        LinkedList<Variant> variants = new LinkedList<>();
        for (VariantLoader loader : mVariantLoaders) {
            Variant variant = loader.getVariant();
            if (variant != null) {
                variants.add(variant);
            }
        }

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

    public void reloadAllVariants() {
        for (VariantLoader loader : mVariantLoaders) {
            loader.load();
        }
    }
}
