package app.subversive.groceryratings.Core;

import java.util.Observable;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by rob on 4/5/15.
 */
public class VariantLoader {
    private Variant mVariant;
    private String mBarcode;

    protected VariantLoader (Variant variant) {
        mVariant = variant;
        mBarcode = variant.productCode;
    }

    protected VariantLoader(String barcode) {
        if ((barcode == null) || (barcode.isEmpty())) {
            throw new RuntimeException("Empty or null barcode");
        }
        mBarcode = barcode;
    }

    public Variant getVariant() {
        return mVariant;
    }

    public void load() {
        GRClient.getService().getProduct(mBarcode, new Callback<Variant>() {
            @Override
            public void success(Variant variant, Response response) {
                mVariant = variant;
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }
}
