package app.subversive.groceryratings.Core;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by rob on 1/4/15.
 */
public class Rating {
    public int stars, like;
    public long datetime;
    public String comment, userKey;
    public boolean published;
    private Calendar date;
    public TasteTag[] tags;
    public String[] images;
    private String dateString;
    public User user;
    public Variant parent;

    public Rating() {}

    public Rating(int stars, long datetime, String comment, TasteTag[] tags) {
        this.stars = stars;
        this.datetime = datetime;
        this.comment = comment;
        this.tags = tags;
    }

    public Calendar getDate() {
        if (date == null) {
            date = Calendar.getInstance();
            date.setTimeInMillis(datetime);
        }
        return date;
    }

    public String getDateString() {
        Calendar date = getDate();
        if (dateString == null) {
            dateString = String.valueOf(date.get(Calendar.MONTH)) + "/" +
                         String.valueOf(date.get(Calendar.DAY_OF_MONTH)) + "/" +
                         String.valueOf(date.get(Calendar.YEAR));
        }
        return dateString;
    }
}