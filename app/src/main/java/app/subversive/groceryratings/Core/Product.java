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

    public String getProductName() {
        return productName;
    }
}
