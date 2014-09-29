package app.subversive.groceryratings.Core;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Created by rob on 8/24/14.
 */
public class Product implements Parcelable {
    public String parent, brandName, productName, manName, productCode, description;
    public int  ratingCount, ratingSum, stars;
    public float ratingScore;
    public boolean published;
    public ArrayList<String> keywords, images;


    public Product() {}

    public Product(boolean defaults) {
        if (defaults) {
            productName = "";
            description = "";
            manName = "";
            brandName = "";
            keywords = new ArrayList<String>();
            images = new ArrayList<String>();
        }
    }

    public Product(String name, int stars, int ratings) {
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

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel source) {
            return new Product(source);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    private Product(Parcel source) {
        super();
        parent = source.readString();
        brandName = source.readString();
        productName = source.readString();
        manName = source.readString();
        productCode = source.readString();
        description = source.readString();
    }
}