package app.subversive.groceryratings.Core;

import android.app.Activity;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import app.subversive.groceryratings.RatingAdapter;
import app.subversive.groceryratings.VariantLoaderAdapter;
import retrofit.mime.TypedByteArray;

/**
 * Created by rob on 4/3/15.
 */
public class GRData {
    private final static String HISTORY_FILE = "HISTORY_FILE";
    private final static String TAG = "HistoryManager";
    private static final Gson gson = new Gson();

    private static final int maxLoaders = 7;

    private static GRData instance;

    public static GRData getInstance() {
        if (instance == null) {
            instance = new GRData();
        }
        return instance;
    }

    private ArrayList<VariantLoader> mVariantLoaders;

    private Set<VariantLoaderAdapter> mVLAdapters;

    private GRData() {
        mVariantLoaders = new ArrayList<>();
        mVLAdapters = new HashSet<>();
    }

    public void loadVariant(String barcode, VariantLoader.UnknownBarcodeCallback callback) {
        for (int i = 0 ; i < mVariantLoaders.size(); i++) {
            VariantLoader loader = mVariantLoaders.get(i);
            if (loader.getBarcode().equals(barcode)) {
                mVariantLoaders.remove(i);
                mVariantLoaders.add(0, loader);
                for (VariantLoaderAdapter adapter : mVLAdapters) {
                    adapter.notifyItemMoved(i, 0);
                }
                return;
            }
        }

        // barcode not in cache

        if (mVariantLoaders.size() >= maxLoaders) {
            int position = mVariantLoaders.size()-1;
            mVariantLoaders.remove(position);
            for (VariantLoaderAdapter adapter : mVLAdapters) {
                adapter.notifyItemRemoved(position);
            }
        }


        VariantLoader loader = new VariantLoader(barcode);
        mVariantLoaders.add(0, loader);
        loader.load(callback);
        for (VariantLoaderAdapter adapter : mVLAdapters) {
            adapter.notifyItemInserted(0);
        }
    }

    public void loadHistory(Activity activity) {
        mVariantLoaders.clear();
        try {
            FileReader fr = new FileReader(new File(activity.getFilesDir(), HISTORY_FILE));
            for (Variant variant : gson.fromJson(fr, Variant[].class)) {
                variant.resetSortedWordscore();
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
        ArrayList<Variant> variants = new ArrayList<>();
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
            loader.load(null);
        }
    }

    public VariantLoaderAdapter getVariantLoaderAdapter(RatingAdapter.ItemClickListener listener) {
        VariantLoaderAdapter adapter = new VariantLoaderAdapter(mVariantLoaders, listener);
        mVLAdapters.add(adapter);
        return adapter;
    }

    public void flushHistory() {
        mVariantLoaders.clear();
        for (VariantLoaderAdapter adapter : mVLAdapters) {
            adapter.notifyItemRangeRemoved(0, mVariantLoaders.size());
        }
    }

    public List<Variant> getVariants() {
        ArrayList<Variant> variants = new ArrayList<>();
        for (VariantLoader loader : mVariantLoaders) {
            if (loader.getVariant() != null) {
                variants.add(loader.getVariant());
            }
        }
        return variants;
    }

    public void addNewVariant(String barcode, TypedByteArray imageData) {
        VariantLoader newLoader = null;
        for (VariantLoader loader : mVariantLoaders) {
            if (loader.getBarcode().equals(barcode)) {
                newLoader = loader;
                break;
            }
        }
        if (newLoader == null) {
            newLoader = new VariantLoader(barcode);
        }
        newLoader.insertNewVariant(imageData);

    }
}
