package app.subversive.groceryratings.Core;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

import app.subversive.groceryratings.GroceryRatingsService;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.Header;
import retrofit.http.Path;

/**
 * Created by rob on 9/20/14.
 */
public class DebugGroceryService extends DebugService implements GroceryRatingsService {
    static int productCounter;
    final static HashMap<String, Variant> datastore = new HashMap<String, Variant>();

    public static String addNewProduct() {
        String barcode = String.valueOf(random.nextInt(100000));
        Variant variant = new Variant(String.format("Debug product %d", productCounter++), random.nextInt(5));
        variant.productCode = barcode;
        variant.published = true;
        variant.ratings = new ArrayList<>();
        int numRatings = random.nextInt(20);
        HashMap<String, Integer> wordcount = new HashMap<>();
        for (int i = 0; i < numRatings; i++) {
            final Rating r = randomRating();
            variant.ratings.add(r);
            for (TasteTag tag : r.tags) {
                wordcount.put(tag.value, (wordcount.get(tag.value) == null) ? 0 : wordcount.get(tag.value) + 1);
            }
        }
        variant.wordscore = wordcount;
        datastore.put(barcode, variant);
        return barcode;
    }

    @Override
    public void getProduct(@Path("productID") String productID, Callback<Variant> cb) {
        successfulRequest(datastore.get(productID), cb);
    }

    @Override
    public void addNewProduct(@Body Variant variant, Callback<Variant> cb) {
        variant.productName = String.format("New variant name %d", productCounter++);
        variant.stars = random.nextInt(6);
        int numRatings = random.nextInt(20);
        variant.published = true;
        variant.ratings = new ArrayList<>();

        HashMap<String, Integer> wordcount = new HashMap<>();
        for (int i = 0; i < numRatings; i++) {
            final Rating r = randomRating();
            variant.ratings.add(r);
            for (TasteTag tag : r.tags) {
                wordcount.put(tag.value, (wordcount.get(tag.value) == null) ? 0 : wordcount.get(tag.value) + 1);
            }
        }
        variant.wordscore = wordcount;
        datastore.put(variant.productCode, variant);
        successfulRequest(variant, cb);
    }

    @Override
    public void getUser(@Header("service") String service, @Header("token") String token, @Header("secret") String secret, Callback<User> callback) {
        successfulRequest(randomUser(), callback);
    }

    @Override
    public void addNewRating(@Body Rating rating, Callback<Rating> callback) {
        successfulRequest(rating, callback);
    }

    private static TasteTag randomTasteTag() {
        String[] names = {
                "allergen",
                "bitter",
                "bland",
                "bold",
                "chewy",
                "creamy",
                "crunchy",
                "dry",
                "fresh",
                "fatty",
                "greasy"};

        return new TasteTag("tasteTag", names[random.nextInt(names.length)]);
    }

    private static Rating randomRating() {
        Rating rating = new Rating();
        Calendar c = Calendar.getInstance();
        c.set(2000 + random.nextInt(15), random.nextInt(13), random.nextInt(28));
        rating.datetime = c.getTimeInMillis();
        rating.stars = random.nextInt(6);
        int nTags = random.nextInt(20);
        TasteTag[] tags = new TasteTag[nTags];
        for (int i = 0; i < nTags; i++) {
            tags[i] = randomTasteTag();
        }
        rating.tags = tags;
        rating.published = true;
        rating.user = randomUser();
        int[] commentCharCount = new int[10 + random.nextInt(200)];
        for (int i = 0; i < commentCharCount.length ; i++) {
            commentCharCount[i] = 3 + random.nextInt(7);
        }
        rating.comment = randomString(commentCharCount);
        return rating;
    }

    public static User randomUser() {
        User user = new User();
        user.name = randomString(new int[]{5,7});
        user.displayName = randomString(6);
        return user;
    }

    private static String randomString(int[] lengths) {
        StringBuilder b = new StringBuilder();
        for(int i : lengths) {
            b.append(randomString(i));
            b.append(" ");
        }
        b.deleteCharAt(b.length()-1);
        return b.toString();
    }

    private static String randomString(int length) {
        return UUID.randomUUID().toString().substring(0, length);
    }
}
