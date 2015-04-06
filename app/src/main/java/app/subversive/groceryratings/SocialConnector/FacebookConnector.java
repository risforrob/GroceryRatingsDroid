package app.subversive.groceryratings.SocialConnector;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;

import java.util.Arrays;

import app.subversive.groceryratings.Core.User;
import app.subversive.groceryratings.MainWindow;
import app.subversive.groceryratings.Core.GRClient;
import retrofit.Callback;

/**
 * Created by rob on 2/27/15.
 */
public class FacebookConnector implements SocialConnector {
    private final String TAG = FacebookConnector.class.getSimpleName();
    private final String mSocialKey = "facebook";
    private MainWindow activity;
    private UiLifecycleHelper uiLifecycleHelper;

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        Log.v(TAG, state.toString());
        Log.v(TAG, session.toString());
        if (exception != null) {
            Log.v(TAG, exception.toString() + " " + exception.getMessage());
        }
        if (state.isOpened()) {
            Log.i(TAG, "Logged in...");
            onConnected();
        } else if (state.isClosed()) {
            Log.i(TAG, "Logged out...");

        }
    }

    public FacebookConnector(MainWindow activity) {
        this.activity = activity;
        uiLifecycleHelper = new UiLifecycleHelper(activity, callback);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        uiLifecycleHelper.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        Session session = Session.getActiveSession();
        if (session != null &&
                (session.isOpened() || session.isClosed()) ) {
            onSessionStateChange(session, session.getState(), null);
        }
        uiLifecycleHelper.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        uiLifecycleHelper.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        uiLifecycleHelper.onPause();
    }

    @Override
    public void onStop() {
        uiLifecycleHelper.onStop();
    }

    @Override
    public void deauthorize() {
        new Request(
                Session.getActiveSession(),
                "/me/permissions",
                null,
                HttpMethod.DELETE,
                new Request.Callback() {
                    @Override
                    public void onCompleted(Response response) {
                        Session.getActiveSession().closeAndClearTokenInformation();
                        activity.onDeauthorize();
                    }
                }
        ).executeAsync();
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onDestroy() {
        uiLifecycleHelper.onDestroy();
    }

    @Override
    public void login() {
        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            Log.v(TAG, "Why are yo trying to connect?  You are already logged in.");
        } else {
            Session.openActiveSession(activity, true, Arrays.asList("email"), callback);
        }
    }

    @Override
    public void onConnected() {
        activity.onConnected();
    }

    @Override
    public void logout() {
        Session session = Session.getActiveSession();
        if (session == null) {
            throw new RuntimeException("Trying to log out of a null session");
        }
        session.closeAndClearTokenInformation();
        activity.onLogout();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        uiLifecycleHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public String getSocialKey() {
        return mSocialKey;
    }

    @Override
    public void getUser(MainWindow activity, Callback<User> userCallback) {
        GRClient.getService().getUser(getSocialKey(), com.facebook.Session.getActiveSession().getAccessToken(), null, userCallback);
    }
}
