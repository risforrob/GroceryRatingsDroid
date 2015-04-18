package app.subversive.groceryratings.SocialConnector;

import app.subversive.groceryratings.Core.GRClient;
import app.subversive.groceryratings.MainWindow;

/**
 * Created by rob on 2/28/15.
 */
public class SocialFactory {
    public static SocialConnector buildConnector(String type, MainWindow activity) {
        if (GRClient.getInstance().isDebug()) {
            return new DebugConnector(activity);
        }
        switch(type) {
            case "facebook":
                return new FacebookConnector(activity);
            case "twitter":
                return new TwitterConnector(activity);
            case "google":
                return new GoogleConnector(activity);
        }
        return null;
    }
}
