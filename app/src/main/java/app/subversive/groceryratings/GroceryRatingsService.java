package app.subversive.groceryratings;

import app.subversive.groceryratings.Core.Rating;
import app.subversive.groceryratings.Core.TasteTagContainer;
import app.subversive.groceryratings.Core.User;
import app.subversive.groceryratings.Core.Variant;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by rob on 8/24/14.
 */
public interface GroceryRatingsService {

    @GET("/variantdaoendpoint/v1/variant/{productID}")
    void getProduct(@Path("productID") String productID, Callback<Variant> callback);

    @POST("/variantdaoendpoint/v1/variantdao")
    void addNewProduct(@Body Variant variant, Callback<Variant> callback);

    @GET("/userdaoendpoint/v1/user")
    void getUser(
            @Header("service") String service,
            @Header("token") String token,
            @Header("secret") String secret,
            Callback<User> callback);

    @POST("/ratingdaoendpoint/v1/rating")
    void addNewRating(@Body Rating rating, Callback<Rating> callback);

    @GET("/tastetagdaoendpoint/v1/tastetagdao")
    void getTasteTags(Callback<TasteTagContainer> callback);
}
