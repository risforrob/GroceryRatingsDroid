package app.subversive.groceryratings;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;

import app.subversive.groceryratings.Core.Rating;
import app.subversive.groceryratings.Core.User;
import app.subversive.groceryratings.Core.Variant;
import app.subversive.groceryratings.SocialConnector.EmptyConnector;
import app.subversive.groceryratings.SocialConnector.SocialConnector;
import app.subversive.groceryratings.SocialConnector.SocialFactory;

import app.subversive.groceryratings.UI.GRClient;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainWindow
        extends Activity {

    private interface ConnectionCallback { void onConnected(); }

    interface UpNavigation { public void onNavigateUp(); }

    private final static String TAG = MainWindow.class.getSimpleName();

//    public

    private ScanFragment scanFrag;

    private ConnectionCallback mConnectionCallback;

    public SocialConnector mSocialConn;
    private UpNavigation mUpNav;
    private boolean isSocalConnected, shouldDisplayReviewFrag;

    private User mUser;
    private Variant mVariant;

    public static class Preferences {
        final private static String AUTOSCAN = "AUTOSCAN";
        final private static String TUTORIAL_COMPLETED = "TUTORIAL_COMPLETED";
        final private static String SOCIAL_CONN = "SOCIAL_CONN";

        public static boolean autoscan;
        public static boolean tutorialComplete;
        public static String socialConn;

        static void loadPrefs(SharedPreferences prefs) {
            autoscan = prefs.getBoolean(AUTOSCAN, false);
            tutorialComplete = prefs.getBoolean(TUTORIAL_COMPLETED, false);
            socialConn = prefs.getString(SOCIAL_CONN, null);
        }

        static void writePrefs(SharedPreferences prefs) {
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean(AUTOSCAN, autoscan);
            edit.putBoolean(TUTORIAL_COMPLETED, tutorialComplete);
            if (socialConn != null) {
                edit.putString(SOCIAL_CONN, socialConn);
            } else {
                edit.remove(SOCIAL_CONN);
            }
            edit.apply();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Fabric.with(this, new Crashlytics(), new Twitter(TwitterConnector.authConfig));


        if (savedInstanceState == null) {
            // todo move this into splash_screen async task
            setContentView(R.layout.activity_main_window);

            scanFrag = ScanFragment.newInstance();
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, scanFrag)
                    .commit();

        }
        if (!checkCameraHardware(this)) {
            throw new RuntimeException("No Camera");
        }

        GRClient.initialize(Installation.id(this));

        Utils.setDPMultiplier(getResources().getDisplayMetrics().density);
        Preferences.loadPrefs(getPreferences(MODE_PRIVATE));

        if (Preferences.socialConn == null) {
            mSocialConn = new EmptyConnector();
        } else {
            mSocialConn = SocialFactory.buildConnector(Preferences.socialConn, this);
        }

        mSocialConn.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mSocialConn.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSocialConn.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Preferences.writePrefs(getPreferences(MODE_PRIVATE));
        mSocialConn.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSocialConn.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocialConn.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mSocialConn.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mSocialConn.onActivityResult(requestCode, resultCode, data);
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

        menu.add(3,9,1, (isSocalConnected) ? "Sign Out" : "Sign In");
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
            case 9:
                if (isSocalConnected) {
                    socialLogout();
                } else {
                    showSocialSelector();
                }
                return true;
            case 4:
                Log.i("MainWindow", "debug mode!");
                item.setChecked(!item.isChecked());
                GRClient.getInstance().setDebug(item.isChecked());
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
                .replace(R.id.container, frag)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
    }

    void onRatingSelected(int variantIndex, int ratingIndex) {
        Log.v(TAG, String.format("%d %d", variantIndex, ratingIndex));
        ProductRatingsFragment frag = ProductRatingsFragment.newInstance(variantIndex, ratingIndex);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, frag)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
    }

    public void onDeauthorize() {
        Log.v(TAG, "DEAUTHORIZE");
    }

    public void onConnected() {
        isSocalConnected = true;
        Preferences.socialConn = mSocialConn.getSocialKey();
        Toast.makeText(this, "Connected to " + mSocialConn.getSocialKey(), Toast.LENGTH_SHORT).show();

        mSocialConn.getUser(this, new Callback<User>() {
            @Override
            public void success(User user, Response response) {
                if (response.getStatus() == 200) {
                    mUser = user;
                } else {
                    failure(null);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                mUser = null;
                Toast.makeText(MainWindow.this, "Failed to log into Grocery Ratings", Toast.LENGTH_SHORT).show();
            }
        });

        if (mConnectionCallback != null) {
            mConnectionCallback.onConnected();
            mConnectionCallback = null;
        }

        invalidateOptionsMenu();
    }
    public void onLogout() {
        Log.v(TAG, "LOGOUT");
        Toast.makeText(this, "Disconnected from " + mSocialConn.getSocialKey(), Toast.LENGTH_SHORT).show();
    }

    public void onAddReview(int index) {
        mVariant = getVariants().get(index);
        if (isSocalConnected) {
            showReviewFragment();
        } else {
            shouldDisplayReviewFrag = true;
            showSocialSelector();
        }
    }

    public void showReviewFragment() {
        shouldDisplayReviewFrag = false;
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, ProductReviewFragment.newInstance(), null)
                .addToBackStack(null)
                .commit();

    }

    public boolean isSocialConnected() {
        return isSocalConnected;
    }

    public void showSocialSelector() {
        SocialSelectorFragment.newInstance().show(getFragmentManager(), null);
    }

    public void onSocialSelected(String social) {
        Log.v(TAG, social);
        mSocialConn = SocialFactory.buildConnector(social, this);
        if (shouldDisplayReviewFrag) {
            mConnectionCallback = new ConnectionCallback() {
                @Override
                public void onConnected() {
                    showReviewFragment();
                }
            };
        }
        mSocialConn.login();
    }

    public void socialLogout() {
        isSocalConnected = false;
        Preferences.socialConn = null;
        mUser = null;
        mSocialConn.logout();
        mSocialConn = new EmptyConnector();
        invalidateOptionsMenu();
    }

    public void addRating(int numStars, String ratingText, boolean closeCurrentFragment) {
        Log.v(TAG, String.format("%d %s", numStars, ratingText));
        if (closeCurrentFragment) {
            getFragmentManager().popBackStack();
        }
        if ((mUser == null) || (mVariant == null)) {
            throw new RuntimeException("Trying to add a rating with no user or variant");
        }

        Rating rating = new Rating();
        rating.comment = ratingText;
        rating.stars = numStars;
        rating.parent = mVariant;
        rating.user = mUser;
        GRClient.getService().addNewRating(rating, new Callback<Rating>() {
            @Override
            public void success(Rating rating, Response response) {
                Toast.makeText(MainWindow.this, "Thank you for your rating.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(MainWindow.this, "Unable to connect to Grocery Ratings", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
