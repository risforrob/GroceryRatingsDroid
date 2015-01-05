package app.subversive.groceryratings.Callbacks;

import app.subversive.groceryratings.Core.Variant;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by rob on 8/31/14.
 */
public class ProductLoaded implements Callback<Variant> {
    @Override
    public void success(Variant variant, Response response) {

    }

    @Override
    public void failure(RetrofitError error) {

    }
}
