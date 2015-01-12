package app.subversive.groceryratings.Core;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by rob on 8/24/14.
 */
public class Variant {
    public String parent, brandName, productName, manName, productCode, description;
    public int ratingCount, ratingSum, stars;
    public float ratingScore;
    public boolean published;
    public ArrayList<String> keywords, images;
    public ArrayList<Rating> ratings;

    public HashMap<String, Integer> wordscore;


    public Variant() {
    }

    public Variant(boolean defaults) {
        if (defaults) {
            productName = "";
            description = "";
            manName = "";
            brandName = "";
            keywords = new ArrayList<>();
            images = new ArrayList<>();
            ratings = new ArrayList<>();
        }
    }

    public Variant(String name, int stars, int ratings) {
        productName = name;
        this.stars = stars;
        ratingCount = ratings;
    }

    public String getName() {
        return productName;
    }

    public int getNumStars() {
        return stars;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public HashMap<String, Integer> getWordscore() {
        return wordscore;
    }

    public String getDescription() {
        return description;
    }

    public static String formatRatingString(int numRatings) {
        return (numRatings == 0) ? "No Reviews" :
                String.format("%d %s", numRatings, (numRatings == 1) ? "Review" : "Reviews");
    }
}