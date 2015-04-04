package app.subversive.groceryratings.Core;

/**
 * Created by rob on 4/3/15.
 */
public class GRData {
    private static GRData instance;

    public static GRData getInstance() {
        if (instance == null) {
            instance = new GRData();
        }
        return instance;
    }

    private GRData() {}
}
