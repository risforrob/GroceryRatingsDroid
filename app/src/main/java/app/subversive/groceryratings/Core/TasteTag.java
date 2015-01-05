package app.subversive.groceryratings.Core;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by rob on 1/4/15.
 */
public class TasteTag implements Parcelable {
    public String name, value;

    public TasteTag() {}



    public TasteTag(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{name, value});
    }

    public static final Parcelable.Creator<TasteTag> CREATOR
            = new Parcelable.Creator<TasteTag>() {
        public TasteTag createFromParcel(Parcel in) {
            String[] vals = in.createStringArray();
            return new TasteTag(vals[0], vals[1]);
        }

        public TasteTag[] newArray(int size) {
            return new TasteTag[size];
        }
    };
}
