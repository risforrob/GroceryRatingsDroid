package app.subversive.groceryratings.SocialConnector;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;

import app.subversive.groceryratings.MainWindow;

/**
 * Created by rob on 3/15/15.
 */
public class GoogleConnector implements SocialConnector {

    private final static String TAG = GoogleConnector.class.getSimpleName();

    private boolean mSignInClicked;
    private ConnectionResult mConnectionResult;

    GoogleApiClient.ConnectionCallbacks mConnectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {
            Log.v(TAG, "g+ connected!");
            mSignInClicked = false;
            GoogleConnector.this.onConnected();
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.v(TAG, "g+ suspended");
            mGoogleApiClient.connect();
        }
    };

    GoogleApiClient.OnConnectionFailedListener mOnConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(ConnectionResult result) {
            Log.v(TAG, "G+ connectionFailed");
            if (!mIntentInProgress) {
                // Store the ConnectionResult so that we can use it later when the user clicks
                // 'sign-in'.
                mConnectionResult = result;

                if (mSignInClicked) {
                    // The user has already clicked 'sign-in' so we attempt to resolve all
                    // errors until the user is signed in, or they cancel.
                    resolveSignInError();
                }
            }
        }
    };

    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                activity.startIntentSenderForResult(mConnectionResult.getResolution().getIntentSender(),
                        RC_SIGN_IN, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;

    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;

    /* A flag indicating that a PendingIntent is in progress and prevents
     * us from starting further intents.
     */
    private boolean mIntentInProgress;

    MainWindow activity;

    public GoogleConnector(MainWindow activity) {
        this.activity = activity;
        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .addConnectionCallbacks(mConnectionCallbacks)
                .addOnConnectionFailedListener(mOnConnectionFailedListener)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onStop() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
    }

    @Override
    public void deauthorize() {
        // Prior to disconnecting, run clearDefaultAccount().
        Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
        Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient)
                .setResultCallback(new ResultCallback<Status>() {

                    @Override
                    public void onResult(Status status) {
                        mGoogleApiClient.disconnect();
                        activity.onDeauthorize();
                    }
                });
    }

    @Override
    public void login() {
        mSignInClicked = true;
        if (!mGoogleApiClient.isConnecting()) {
            resolveSignInError();
        }
    }

    @Override
    public void logout() {
        Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
        mGoogleApiClient.disconnect();
        mGoogleApiClient.connect();
        activity.onLogout();
    }

    @Override
    public void onConnected() {
        activity.onConnected();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
                Log.v(TAG, "G+ activityResult()");
        if (requestCode == RC_SIGN_IN) {
            if (resultCode != activity.RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void requestId(IdCallback callback) {
        callback.idResponse(Plus.AccountApi.getAccountName(mGoogleApiClient));
    }

    @Override
    public String getSocialKey() {
        return "google";
    }
}
