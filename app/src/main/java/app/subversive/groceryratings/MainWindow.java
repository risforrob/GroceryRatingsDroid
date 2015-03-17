package app.subversive.groceryratings;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.List;

import app.subversive.groceryratings.Core.Rating;
import app.subversive.groceryratings.Core.Variant;
import io.fabric.sdk.android.Fabric;

import app.subversive.groceryratings.test.DebugGroceryService;
import app.subversive.groceryratings.test.DebugImageService;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;

public class MainWindow extends Activity {
    interface UpNavigation { public void onNavigateUp(); }

    private final static String TAG = MainWindow.class.getSimpleName();
    static final String endpoint = "https://groceryratings.appspot.com/_ah/api/variantdaoendpoint/v1";
    static final String imageEndpoint = "https://groceryratings.appspot.com";
    public static GroceryRatingsService service, mainService, debugGroceryService;
    public static ImageService imageService, mainImageService, debugImageService;
    private ScanFragment scanFrag;

    private UpNavigation mUpNav;

    public static class Preferences {
        final private static String AUTOSCAN = "AUTOSCAN";
        final private static String TUTORIAL_COMPLETED = "TUTORIAL_COMPLETED";

        public static boolean autoscan;
        public static boolean tutorialComplete;

        static void loadPrefs(SharedPreferences prefs) {
            autoscan = prefs.getBoolean(AUTOSCAN, false);
            tutorialComplete = prefs.getBoolean(TUTORIAL_COMPLETED, false);
        }

        static void writePrefs(SharedPreferences prefs) {
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean(AUTOSCAN, autoscan);
            edit.putBoolean(TUTORIAL_COMPLETED, tutorialComplete);
            edit.apply();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());


        if (savedInstanceState == null) {
            // todo move this into splash_screen async task
            scanFrag = ScanFragment.newInstance();
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, scanFrag)
                    .commit();


            if (!checkCameraHardware(this)) {
                throw new RuntimeException("No Camera");
            }

            final String uuid = Installation.id(this);

            RequestInterceptor ri = new RequestInterceptor() {
                @Override
                public void intercept(RequestFacade request) {
                    request.addQueryParam("deviceId", uuid);
                }
            };

            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint(endpoint)
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

            Utils.setDPMultiplier(getResources().getDisplayMetrics().density);
            Preferences.loadPrefs(getPreferences(MODE_PRIVATE));

            setContentView(R.layout.activity_main_window);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Preferences.writePrefs(getPreferences(MODE_PRIVATE));
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_window, menu);
        MenuItem m = menu.add(2,4,9,"Debug Service");
        m.setCheckable(true);

        menu.add(3,9,1,"Sign In");
        menu.add(3,8,100,"Send Feedback");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case android.R.id.home:
                if (mUpNav != null) {
                    mUpNav.onNavigateUp();
                    return true;
                } else {
                    return false;
                }
            case 4:
                Log.i("MainWindow", "debug mode!");
                item.setChecked(!item.isChecked());
                if (item.isChecked()) {
                    service = debugGroceryService;
                    imageService = debugImageService;
                } else {
                    service = mainService;
                    imageService = mainImageService;
                }
                return false;

            case 8:
                getFragmentManager()
                        .beginTransaction()
                        .hide(scanFrag)
                        .replace(R.id.container, FeedbackFragment.newInstance())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack(null)
                        .commit();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (scanFrag != null && scanFrag.isVisible() && scanFrag.onBackPressed()) {
            // do nothing, scanFrag handled this
        } else {
            super.onBackPressed();
        }
    }

    void setUpNav(UpNavigation nav) {
        if (nav != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            mUpNav = nav;
        } else {
            getActionBar().setDisplayHomeAsUpEnabled(false);
            getActionBar().setHomeButtonEnabled(false);
            mUpNav = null;
        }
    }

    List<Variant> getVariants() {
        if (scanFrag != null) {
            return scanFrag.getProductHistory();
        } else {
            return null;
        }
    }

    List<Rating> getRatings(int variantIndex) {
        if (scanFrag != null) {
            return scanFrag.getProductHistory().get(variantIndex).ratings;
        } else {
            return null;
        }
    }

    void displayVariantData(int index) {
        ProductPageFragment frag = ProductPageFragment.newInstance(index);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, frag, "foo")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
    }

    void onRatingSelected(int variantIndex, int ratingIndex) {
        Log.v(TAG, String.format("%d %d", variantIndex, ratingIndex));
        ProductRatingsFragment frag = ProductRatingsFragment.newInstance(variantIndex, ratingIndex);
        Fragment currentFrag = getFragmentManager().findFragmentByTag("foo");
        getFragmentManager()
                .beginTransaction()
                .hide(currentFrag)
                .add(R.id.container, frag)
//                .replace(R.id.container, frag)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
    }
}
