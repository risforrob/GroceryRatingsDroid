package app.subversive.groceryratings.test;

import app.subversive.groceryratings.ImageService;
import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Part;
import retrofit.mime.TypedByteArray;

/**
 * Created by rob on 9/21/14.
 */
public class DebugImageService extends DebugService implements ImageService {
    @Override
    public void uploadImage(@Part("data") TypedByteArray data, Callback<Response> cb) {

    }
}
