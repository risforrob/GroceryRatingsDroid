package app.subversive.groceryratings.Core;

import android.os.AsyncTask;
import android.os.SystemClock;

import java.util.LinkedList;
import java.util.Random;

import retrofit.Callback;
import retrofit.client.Header;
import retrofit.client.Response;

/**
 * Created by rob on 9/21/14.
 */
public class DebugService {
    final static Random random = new Random();
    int sleepmin = 1000;
    int sleepmax = 5000;

    void sleep() {
        SystemClock.sleep((long) sleepmin + random.nextInt(sleepmax - sleepmin));
    }

    <T> void successfulRequest(final T returnValue, final Callback<T> callback) {
        new AsyncTask<Void, Void, Void> () {
            @Override
            protected Void doInBackground(Void... params) {
                sleep();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                callback.success(returnValue, new Response("", 200, "This Worked", new LinkedList<Header>(), null));
            }
        }.execute();
    }
}
