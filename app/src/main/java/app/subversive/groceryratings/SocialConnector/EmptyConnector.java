package app.subversive.groceryratings.SocialConnector;

import android.content.Intent;
import android.os.Bundle;

import java.util.HashMap;

/**
 * Created by rob on 3/14/15.
 */
public class EmptyConnector implements SocialConnector {
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

    }

    @Override
    public void logout() {

    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public String getSocialKey() {
        return null;
    }

    @Override
    public HashMap<String, String> getServiceHeader() {
        return null;
    }
}
