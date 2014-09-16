package app.subversive.groceryratings;

import app.subversive.groceryratings.Core.Product;
import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.PUT;
import retrofit.http.Path;

/**
 * Created by rob on 8/24/14.
 */
public interface GroceryRatingsService {
    @GET("/variantdao/{productID}")
    void getProduct(@Path("productID") String productID, Callback<Product> cb);

    @PUT("/variantdao")
    void updateProduct(@Body Product product, Callback<Response> cb);
}
