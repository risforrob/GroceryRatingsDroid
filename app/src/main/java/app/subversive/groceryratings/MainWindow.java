package app.subversive.groceryratings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import app.subversive.groceryratings.camera.CameraUtil;
import app.subversive.groceryratings.test.DebugGroceryService;
import app.subversive.groceryratings.test.DebugImageService;
import retrofit.RestAdapter;

public class MainWindow extends ActionBarActivity {
    private final static String TAG = MainWindow.class.getSimpleName();
    static final String endpoint = "https://1-dot-groceryratings.appspot.com/_ah/api/variantdaoendpoint/v1";
    static final String imageEndpoint = "https://groceryratings.appspot.com";
    public static GroceryRatingsService service, mainService, debugGroceryService;
    public static ImageService imageService, mainImageService, debugImageService;
    private ScanFragment scanFrag;

    public static class Preferences {
        final private static String AUTOSCAN = "AUTOSCAN";

        public static boolean autoscan;

        static void loadPrefs(SharedPreferences prefs) {
            autoscan = prefs.getBoolean(AUTOSCAN, false);
        }

        static void writePrefs(SharedPreferences prefs) {
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean(AUTOSCAN, autoscan);
            edit.commit();
        }
    }
    private final static String HISTORY_FILE = "HISTORY_FILE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_window);
        if (savedInstanceState == null) {
            // todo move this into loading async task
            scanFrag = ScanFragment.newInstance(readRawHistoryData());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, scanFrag)
                    .commit();


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

            CameraUtil.initialize(this);
            Utils.setDPMultiplier(getResources().getDisplayMetrics().density);
            Preferences.loadPrefs(getPreferences(MODE_PRIVATE));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Preferences.writePrefs(getPreferences(MODE_PRIVATE));
        writeHistory();
        ManagedTimer.cancelAll();
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

    @Override
    public void onBackPressed() {
        if (scanFrag.isVisible() && !scanFrag.onBackPressed()) {
            super.onBackPressed();
        }
    }

    private void writeHistory() {
        String jsonstring = (new Gson()).toJson(scanFrag.getProductHistory());
        try {
            FileOutputStream out = openFileOutput(HISTORY_FILE, MODE_PRIVATE);
            out.write(jsonstring.getBytes());
            out.close();
        } catch (FileNotFoundException e) {
            Log.i(TAG, "No file to write history");
            // fail elegantly
        } catch (IOException e) {
            Log.i(TAG, "error writing history");
            // fail elegantly
        }
    }

    private String readRawHistoryData() {
        File f = new File(getFilesDir(), HISTORY_FILE);
        try {
            RandomAccessFile rf = new RandomAccessFile(f, "r");
            byte[] bytes = new byte[(int) rf.length()];
            rf.readFully(bytes);
            rf.close();
            return new String(bytes);
        } catch (FileNotFoundException e) {
            Log.i(TAG, "No history to load");
            // do nothing
        } catch (IOException e) {
            Log.i(TAG, "error reading history file");
            // do nothing
        }
        return null;
    }
}
