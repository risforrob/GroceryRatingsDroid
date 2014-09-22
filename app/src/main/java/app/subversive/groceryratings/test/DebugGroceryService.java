package app.subversive.groceryratings.test;

import android.os.SystemClock;

import java.util.HashMap;
import java.util.Random;

import app.subversive.groceryratings.Core.Product;
import app.subversive.groceryratings.GroceryRatingsService;
import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.Path;

/**
 * Created by rob on 9/20/14.
 */
public class DebugGroceryService extends DebugService implements GroceryRatingsService {
    int productCounter;

    final static HashMap<String, Product> datastore = new HashMap<String, Product>();

    @Override
    public void getProduct(@Path("productID") String productID, Callback<Product> cb) {
        Product p;
        if (productID == null || productID.isEmpty()) {
            p = new Product(String.format("Debug product %d", productCounter++), random.nextInt(5), random.nextInt(50));
            p.published = true;
        } else {
            p = datastore.get(productID);
        }
        successfulRequest(p, cb);
    }

    @Override
    public void updateProduct(@Body Product product, Callback<Product> cb) {
        successfulRequest(product, cb);
    }
}
