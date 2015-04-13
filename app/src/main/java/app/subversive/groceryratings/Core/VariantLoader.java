package app.subversive.groceryratings.Core;

import android.database.DataSetObservable;
import android.util.Log;

import java.io.IOException;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

/**
 * Created by rob on 4/5/15.
 */
public class VariantLoader extends DataSetObservable {
    private static final String TAG = VariantLoader.class.getSimpleName();
    public interface UnknownBarcodeCallback {
        void onUnknownBarcode(String barcode);
    }

    private Variant mVariant;
    private String mBarcode;
    private State mState;

    public enum State {
        FETCHING, LOADED, CREATED, ERROR, PROGRESS_IMAGE, PROGRESS_ADD_NEW_VARIANT, SUCCESS_ADD;
    }

    protected VariantLoader (Variant variant) {
        mVariant = variant;
        mBarcode = variant.productCode;
        setState(State.LOADED);
    }

    protected VariantLoader(String barcode) {
        if ((barcode == null) || (barcode.isEmpty())) {
            throw new RuntimeException("Empty or null barcode");
        }
        mBarcode = barcode;
        setState(State.CREATED);
    }

    public Variant getVariant() {
        return mVariant;
    }

    public State getState() { return mState; }

    public void load(final UnknownBarcodeCallback callback) {
        setState(State.FETCHING);
        GRClient.getService().getProduct(mBarcode, new Callback<Variant>() {
            @Override
            public void success(Variant variant, Response response) {
                setState(State.LOADED);
                if (variant != null && variant.published) {
                    mVariant = variant;
                } else {
                    callback.onUnknownBarcode(mBarcode);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                setState(State.ERROR);
            }
        });
    }

    private void setState(State state) {
        mState = state;
        notifyChanged();
    }

    public String getBarcode() { return mBarcode; }

    protected void insertNewVariant(TypedByteArray imageData) {
        if (imageData != null) {
            setState(State.PROGRESS_IMAGE);
            GRClient.getImageService().uploadImage(imageData, new Callback<Response>() {
                @Override
                public void success(Response data, Response response) {
                    String id;
                    byte[] bytes = new byte[(int) data.getBody().length()];
                    try {
                        int read = data.getBody().in().read(bytes, 0, (int) data.getBody().length());
                        id = new String(bytes);
                        insertNewVariant(id);
                    } catch (IOException e) {
                        Log.i(TAG, "Error reading image ID");
                        setState(State.ERROR);
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    setState(State.ERROR);
                }
            });

        } else {
            insertNewVariant((String) null);
        }
    }

    private void insertNewVariant(String imageKey) {
        Variant variant = new Variant();
        variant.productCode = mBarcode;
        if (imageKey != null && !imageKey.isEmpty()) {
            variant.images.add(imageKey);
        }
        setState(State.PROGRESS_ADD_NEW_VARIANT);
        GRClient.getService().addNewProduct(variant, new Callback<Variant>() {
            @Override
            public void success(Variant variant, Response response) {
                setState(State.SUCCESS_ADD);
            }

            @Override
            public void failure(RetrofitError error) {
                setState(State.ERROR);
            }
        });
    }
}
