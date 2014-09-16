package app.subversive.groceryratings.Core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Created by rob on 8/24/14.
 */
public class Product {
    public String parent, brandName, productName, manName, productCode, description;
    public int  ratingCount, ratingSum, stars;
    public float ratingScore;
    public boolean published;
    public LinkedList<String> keywords, images;

    public Product() {
        productName = "";
        description = "";
    }

    public Product(String name, int stars, int ratings) {
        productName = name;
        this.stars = stars;
        ratingCount = ratings;
    }

    public String getName() {
        return productName;
    }
    public int getNumStars() { return stars; }
    public int getRatingCount() { return ratingCount; }
}
