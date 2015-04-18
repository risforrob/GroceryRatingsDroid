package app.subversive.groceryratings.SocialConnector;

import android.content.Intent;
import android.os.Bundle;

import java.util.HashMap;

import app.subversive.groceryratings.Core.User;
import app.subversive.groceryratings.MainWindow;
import retrofit.Callback;

/**
 * Created by rob on 1/30/15.
 */
public interface SocialConnector {
    void onCreate(Bundle savedInstanceState);
    void onResume();
    void onSaveInstanceState(Bundle outState);
    void onPause();
    void onStop();
    void onDestroy();
    void onStart();

    void deauthorize();


    void login();

    void logout();
    void onConnected();
    void onActivityResult(int requestCode, int resultCode, Intent data);

    String getSocialKey();

//    public HashMap<String, String> getServiceHeader();
    void getUser(MainWindow activity, Callback<User> userCallback);
}
