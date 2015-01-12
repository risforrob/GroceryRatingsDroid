package app.subversive.groceryratings.Core;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by rob on 1/4/15.
 */
public class TasteTag {
    public String name, value;

    public TasteTag() {}

    public TasteTag(String name, String value) {
        this.name = name;
        this.value = value;
    }
}
