package app.subversive.groceryratings.Core;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.util.Pair;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by rob on 8/24/14.
 */
public class Variant {
    public String brandName, productName, manName, productCode, description, firstImageKey;
    public int ratingSum, stars;
    public float ratingScore;
    public boolean published;
    public ArrayList<String> keywords, images;
    public ArrayList<Rating> ratings;

    public HashMap<String, Integer> wordscore;
    private transient List<Map.Entry<String, Integer>> sortedWordscore;

    public Variant() {
    }

    public Variant(String name, int stars) {
        productName = name;
        this.stars = stars;
    }

    public void initDefaults() {
        keywords = new ArrayList<>();
        images = new ArrayList<>();
        ratings = new ArrayList<>();

        manName = "";
        brandName = "";
        productName = "";
        description = "";

    }

    public String getName() {
        return productName;
    }

    public int getNumStars() {
        return stars;
    }

    public int getRatingCount() {
        return (ratings == null) ? 0 : ratings.size();
    }

//    public HashMap<String, Integer> getWordscore() {
//        return wordscore;
//    }

    public void resetSortedWordscore() { sortedWordscore = null; }

    public List<Map.Entry<String, Integer>> getSortedWordscore() {
        if ((sortedWordscore == null) && (wordscore != null)) {
            sortedWordscore = new ArrayList<>(wordscore.entrySet());
            Collections.sort(sortedWordscore, new Comparator<Map.Entry<String, Integer>>() {
                @Override
                public int compare(Map.Entry<String, Integer> lhs, Map.Entry<String, Integer> rhs) {
                    return rhs.getValue().compareTo(lhs.getValue());
                }
            });
        }
        return sortedWordscore;
    }

    public String getDescription() {
        return description;
    }

    public static String formatRatingString(int numRatings) {
        return (numRatings == 0) ? "No Reviews" :
            String.format("%d %s", numRatings, (numRatings == 1) ? "Review" : "Reviews");
    }

    public String getImageURL(int pxSize) {
        pxSize = (pxSize == 0) ? 1 : pxSize;
        String r =  String.format("http://www.groceryratings.com/groceryratings/image?key=%s&size=%d", firstImageKey, pxSize);
        Log.i("Variant", r);
        return r;
    }
}