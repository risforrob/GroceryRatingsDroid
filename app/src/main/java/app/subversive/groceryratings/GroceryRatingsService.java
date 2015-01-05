package app.subversive.groceryratings;

import app.subversive.groceryratings.Core.Variant;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by rob on 8/24/14.
 */
public interface GroceryRatingsService {
    @GET("/variantdao/{productID}")
    void getProduct(@Path("productID") String productID, Callback<Variant> cb);

    @POST("/variantdao")
    void addNewProduct(@Body Variant variant, Callback<Variant> cb);
}
