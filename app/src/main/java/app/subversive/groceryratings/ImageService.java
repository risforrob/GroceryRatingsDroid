package app.subversive.groceryratings;

/**
 * Created by rob on 9/14/14.
 */

import app.subversive.groceryratings.Core.ImageKey;
import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.mime.TypedByteArray;

public interface ImageService {
    @Multipart
    @POST("/web_upload")
    public void uploadImage(@Part("data") TypedByteArray data, Callback<ImageKey> cb);
}