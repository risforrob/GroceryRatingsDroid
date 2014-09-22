package app.subversive.groceryratings;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import app.subversive.groceryratings.test.DebugGroceryService;
import app.subversive.groceryratings.test.DebugImageService;
import retrofit.RestAdapter;

public class MainWindow extends ActionBarActivity {

    static final String endpoint = "https://1-dot-groceryratings.appspot.com/_ah/api/variantdaoendpoint/v1";
    static final String imageEndpoint = "https://groceryratings.appspot.com";
    public static GroceryRatingsService service, mainService, debugGroceryService;
    public static ImageService imageService, mainImageService, debugImageService;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkCameraHardware(this)) {
            throw new RuntimeException("No Camera");
        }

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(endpoint)
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

        setContentView(R.layout.activity_main_window);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, ScanFragment.newInstance())
                    .commit();

        }

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
        MenuItem m = menu.add(2,4,0,"Debug Service");
        m.setCheckable(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == 4) {
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
        }
        return super.onOptionsItemSelected(item);
    }
}
