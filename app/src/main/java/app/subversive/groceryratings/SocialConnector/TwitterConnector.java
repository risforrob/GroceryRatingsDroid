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

import java.util.HashMap;

import app.subversive.groceryratings.Core.User;
import app.subversive.groceryratings.MainWindow;
import io.fabric.sdk.android.Fabric;

/**
 * Created by rob on 3/15/15.
 */
public class TwitterConnector implements SocialConnector {
    final String TAG = TwitterConnector.class.getSimpleName();
    private final String mSocialKey = "twitter";
    MainWindow activity;
    TwitterAuthClient authClient;
    long mID;
    public final static TwitterAuthConfig authConfig =
            new TwitterAuthConfig("Y9Lrj94I5eHZvGuRzfJdr4K8E",
                    "***REMOVED***");


    public TwitterConnector(MainWindow activity) {
        this.activity = activity;
        Fabric.with(activity, new Twitter(authConfig));
        authClient = new TwitterAuthClient();
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
                mID = twitterSessionResult.data.getUserId();
                onConnected();
            }

            @Override
            public void failure(TwitterException e) {
                Log.v(TAG, "Failed to connect to twitter:" + e.getMessage());
            }
        });
    }

    @Override
    public void logout() {
        mID = 0;
        Twitter.logOut();
        activity.onLogout();
    }

    @Override
    public void onConnected() {
        Log.d(TAG, Twitter.getSessionManager().getActiveSession().getAuthToken().toString());
        activity.onConnected();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        authClient.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public String getSocialKey() {
        return mSocialKey;
    }

    @Override
    public void getUser(MainWindow activity, retrofit.Callback<User> userCallback) {
        TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();
        MainWindow.service.getUser(
                getSocialKey(),
                session.getAuthToken().token,
                session.getAuthToken().secret,
                userCallback);
    }
}
