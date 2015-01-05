package app.subversive.groceryratings.Core;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by rob on 1/4/15.
 */
public class Rating {
    public int stars, like;
    public long datetime;
    public String comment;
    public boolean published;
    public Date date;
    public List<TasteTag> tags;
    public List<String> images;

    public Rating() {}

    public Rating(int stars, long datetime, String comment, List<TasteTag> tags) {
        this.stars = stars;
        this.datetime = datetime;
        this.comment = comment;
        this.tags = tags;
    }

    public Date getDate() {
        if (date == null) {
            date = new Date();
            date.setTime(datetime);
        }
        return date;
    }
}