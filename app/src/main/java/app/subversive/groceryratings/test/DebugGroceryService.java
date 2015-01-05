package app.subversive.groceryratings.test;

import java.util.HashMap;

import app.subversive.groceryratings.Core.Variant;
import app.subversive.groceryratings.GroceryRatingsService;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.Path;

/**
 * Created by rob on 9/20/14.
 */
public class DebugGroceryService extends DebugService implements GroceryRatingsService {
    static int productCounter;

    final static HashMap<String, Variant> datastore = new HashMap<String, Variant>();

    public static String addNewProduct() {
        String barcode = String.valueOf(random.nextInt(100000));
        Variant p = new Variant(String.format("Debug product %d", productCounter++), random.nextInt(5), random.nextInt(50));
        p.productCode = barcode;
        p.published = true;
        datastore.put(barcode, p);
        return barcode;
    }

    @Override
    public void getProduct(@Path("productID") String productID, Callback<Variant> cb) {
        successfulRequest(datastore.get(productID), cb);
    }

    @Override
    public void addNewProduct(@Body Variant variant, Callback<Variant> cb) {
        variant.productName = String.format("New variant name %d", productCounter++);
        variant.stars = random.nextInt(5);
        variant.ratingCount = random.nextInt(20);
        variant.published = true;
        datastore.put(variant.productCode, variant);
        successfulRequest(variant, cb);
    }
}
