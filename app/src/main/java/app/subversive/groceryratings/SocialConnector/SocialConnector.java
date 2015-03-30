package app.subversive.groceryratings.SocialConnector;

import android.content.Intent;
import android.os.Bundle;

import java.util.HashMap;

/**
 * Created by rob on 1/30/15.
 */
public interface SocialConnector {
    public class IdCallback {
        public void idResponse(String id) {

        }
    }

    public void onCreate(Bundle savedInstanceState);
    public void onResume();
    public void onSaveInstanceState(Bundle outState);
    public void onPause();
    public void onStop();
    public void onDestroy();
    public void onStart();

    public void deauthorize();


    public void login();

    public void logout();
    public void onConnected();
    public void onActivityResult(int requestCode, int resultCode, Intent data);

    public String getSocialKey();

    public HashMap<String, String> getServiceHeader();
}
