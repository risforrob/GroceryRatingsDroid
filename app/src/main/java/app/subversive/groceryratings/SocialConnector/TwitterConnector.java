package app.subversive.groceryratings.SocialConnector;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import app.subversive.groceryratings.MainWindow;
import io.fabric.sdk.android.Fabric;

/**
 * Created by rob on 3/15/15.
 */
public class TwitterConnector implements SocialConnector {
    final String TAG = TwitterConnector.class.getSimpleName();
    MainWindow activity;
    TwitterAuthClient authClient;
    TwitterAuthConfig authConfig =
            new TwitterAuthConfig("8QGi7094SfA8p1g2Ry0U4HElT",
                    "***REMOVED***");


    public TwitterConnector(MainWindow activity) {
        this.activity = activity;
        Fabric.with(activity, new Twitter(authConfig));
        authClient = new TwitterAuthClient();
//        Fabric.getLogger().setLogLevel(0);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (TwitterCore.getInstance().getSessionManager().getActiveSession() != null) {
            onConnected();
        }
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

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void deauthorize() {
        Twitter.logOut();
        activity.onDeauthorize();
    }

    @Override
    public void login() {
        authClient.authorize(activity, new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> twitterSessionResult) {
                activity.onConnected();
            }

            @Override
            public void failure(TwitterException e) {
                Log.v(TAG, "Failed to connect to twitter:" + e.getMessage());
            }
        });
    }

    @Override
    public void logout() {
        Twitter.logOut();
        activity.onLogout();
    }

    @Override
    public void onConnected() {
        activity.onConnected();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        authClient.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void requestId(IdCallback callback) {
        callback.idResponse(String.valueOf(TwitterCore.getInstance().getSessionManager().getActiveSession().getUserId()));
    }

    @Override
    public String getSocialKey() {
        return "twitter";
    }
}
