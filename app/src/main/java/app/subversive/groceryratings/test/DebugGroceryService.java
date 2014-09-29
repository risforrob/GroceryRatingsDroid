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
    static int productCounter;

    final static HashMap<String, Product> datastore = new HashMap<String, Product>();

    public static String addNewProduct() {
        String barcode = String.valueOf(random.nextInt(100000));
        Product p = new Product(String.format("Debug product %d", productCounter++), random.nextInt(5), random.nextInt(50));
        p.productCode = barcode;
        p.published = true;
        datastore.put(barcode, p);
        return barcode;
    }

    @Override
    public void getProduct(@Path("productID") String productID, Callback<Product> cb) {
        successfulRequest(datastore.get(productID), cb);
    }

    @Override
    public void addNewProduct(@Body Product product, Callback<Product> cb) {
        product.productName = String.format("New product name %d", productCounter++);
        product.stars = random.nextInt(5);
        product.ratingCount = random.nextInt(20);
        product.published = true;
        datastore.put(product.productCode, product);
        successfulRequest(product, cb);
    }
}
