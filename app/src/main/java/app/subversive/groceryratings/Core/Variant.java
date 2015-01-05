package app.subversive.groceryratings.Core;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rob on 8/24/14.
 */
public class Variant implements Parcelable {
    public String parent, brandName, productName, manName, productCode, description;
    public int  ratingCount, ratingSum, stars;
    public float ratingScore;
    public boolean published;
    public ArrayList<String> keywords, images;
    public List<Rating> ratings;


    public Variant() {}

    public Variant(boolean defaults) {
        if (defaults) {
            productName = "";
            description = "";
            manName = "";
            brandName = "";
            keywords = new ArrayList<>();
            images = new ArrayList<>();
            ratings = new ArrayList<>();
        }
    }

    public Variant(String name, int stars, int ratings) {
        productName = name;
        this.stars = stars;
        ratingCount = ratings;
    }

    public String getName() {
        return productName;
    }
    public int getNumStars() { return stars; }
    public int getRatingCount() { return ratingCount; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(parent);
        dest.writeString(brandName);
        dest.writeString(productName);
        dest.writeString(manName);
        dest.writeString(productCode);
        dest.writeString(description);
    }

    public static final Creator<Variant> CREATOR = new Creator<Variant>() {
        @Override
        public Variant createFromParcel(Parcel source) {
            return new Variant(source);
        }

        @Override
        public Variant[] newArray(int size) {
            return new Variant[size];
        }
    };

    private Variant(Parcel source) {
        super();
        parent = source.readString();
        brandName = source.readString();
        productName = source.readString();
        manName = source.readString();
        productCode = source.readString();
        description = source.readString();
    }

    public List<TasteTag> getTasteTags() {
        return new ArrayList<>();
    }
}