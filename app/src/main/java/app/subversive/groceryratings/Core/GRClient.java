package app.subversive.groceryratings.Core;

import app.subversive.groceryratings.GroceryRatingsService;
import app.subversive.groceryratings.ImageService;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;

/**
 * Created by rob on 4/3/15.
 */
public class GRClient {
    private static GRClient instance;
    private boolean debugMode;

    public static void initialize(String uuid) {
        instance = new GRClient(uuid);
    }

    public static GroceryRatingsService getService() {
        return (instance == null) ? null : instance.service;
    }

    public static ImageService getImageService() {
        return (instance == null) ? null : instance.imageService;
    }

    public static GRClient getInstance() {
        return instance;
    }

    
    private GroceryRatingsService service, mainService, debugGroceryService;
    private ImageService imageService, mainImageService, debugImageService;

    private static final String serviceEndpoint = "https://groceryratings.appspot.com/_ah/api";
    private static final String imageEndpoint = "https://groceryratings.appspot.com";
    private GRClient(final String uuid) {
        RequestInterceptor ri = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addQueryParam("deviceId", uuid);
            }
        };
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(serviceEndpoint)
                .setRequestInterceptor(ri)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();

        service = mainService = restAdapter.create(GroceryRatingsService.class);

        debugGroceryService = new DebugGroceryService();

        RestAdapter imageAdapter = new RestAdapter.Builder()
                .setEndpoint(imageEndpoint)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();

        imageService = mainImageService = imageAdapter.create(ImageService.class);
        debugImageService = new DebugImageService();
    }

    public void setDebug(boolean debugEnabled) {
        debugMode = debugEnabled;
        if (debugEnabled) {
            service = debugGroceryService;
            imageService = debugImageService;
        } else {
            service = mainService;
            imageService = mainImageService;
        }
    }

    public boolean isDebug() {
        return debugMode;
    }
}
