package app.subversive.groceryratings.Core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import app.subversive.groceryratings.ImageService;
import retrofit.Callback;
import retrofit.client.Header;
import retrofit.client.Response;
import retrofit.http.Part;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedInput;

/**
 * Created by rob on 9/21/14.
 */
public class DebugImageService extends DebugService implements ImageService {
    @Override
    public void uploadImage(@Part("data") TypedByteArray data, Callback<Response> cb) {
        Response r = new Response("groceryratings.com", 200, "It worked?", new ArrayList<Header>(), new TypedInput() {
            @Override
            public String mimeType() {
                return "text/plain";
            }

            @Override
            public long length() {
                return 0;
            }

            @Override
            public InputStream in() throws IOException {
                return new ByteArrayInputStream("foobar".getBytes());
            }
        });
        successfulRequest(r, cb);
    }
}
