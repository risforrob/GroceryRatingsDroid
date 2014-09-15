package app.subversive.groceryratings;

import app.subversive.groceryratings.Core.Product;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;

/**
 * Created by rob on 8/24/14.
 */
public interface GroceryRatingsService {
    @GET("/variantdao/{productID}")
    void getProduct(@Path("productID") String productID, Callback<Product> cb);
}
