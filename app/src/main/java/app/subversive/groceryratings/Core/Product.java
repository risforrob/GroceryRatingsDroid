package app.subversive.groceryratings.Core;

/**
 * Created by rob on 8/24/14.
 */
public class Product {
    String parent, brandName, productName, manName, productCode, description;
    int  ratingCount, ratingSum, stars;
    float ratingScore;
    boolean published;
    String[] keywords;

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
