package app.subversive.groceryratings.Core;


import java.util.List;

/**
 * Created by rob on 1/9/15.
 */
public class User {
    public String name, displayName, pictureURL, loginId, email, sessionKey, key;
    public int reviews, variants;
    public List<Rating> ratings;
    public List<Image> images;
    public boolean admin, disabled;

    public User () {}
}
