package app.subversive.groceryratings.Core;

import android.database.DataSetObservable;
import android.database.DataSetObserver;

import java.util.Observable;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by rob on 4/5/15.
 */
public class VariantLoader extends DataSetObservable {
    public interface UnknownBarcodeCallback {
        void onUnknownBarcode(String barcode);
    }

    private Variant mVariant;
    private String mBarcode;
    private State mState;

    public enum State {
        FETCHING, LOADED, CREATED, ERROR;
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
                mVariant = variant;
                setState(State.LOADED);
                if (variant == null && callback != null) {
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
}
