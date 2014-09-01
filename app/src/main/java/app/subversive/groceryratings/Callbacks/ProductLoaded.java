package app.subversive.groceryratings.Callbacks;

import app.subversive.groceryratings.Core.Product;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by rob on 8/31/14.
 */
public class ProductLoaded implements Callback<Product> {
    @Override
    public void success(Product product, Response response) {

    }

    @Override
    public void failure(RetrofitError error) {

    }
}
