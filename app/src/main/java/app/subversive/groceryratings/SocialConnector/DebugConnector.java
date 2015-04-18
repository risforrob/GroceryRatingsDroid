package app.subversive.groceryratings.SocialConnector;

import android.content.Intent;
import android.os.Bundle;

import app.subversive.groceryratings.Core.GRClient;
import app.subversive.groceryratings.Core.User;
import app.subversive.groceryratings.MainWindow;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by rob on 4/17/15.
 */
public class DebugConnector implements SocialConnector {
    MainWindow mActivity;


    public DebugConnector(MainWindow activity) {
        mActivity = activity;
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

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void deauthorize() {

    }

    @Override
    public void login() {
        onConnected();
    }

    @Override
    public void logout() {

    }

    @Override
    public void onConnected() {
        mActivity.onConnected();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public String getSocialKey() {
        return null;
    }

    @Override
    public void getUser(MainWindow activity, Callback<User> userCallback) {
        GRClient.getService().getUser("debug", "token", "secret", userCallback);
    }
}
