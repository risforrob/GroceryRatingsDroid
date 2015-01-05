package app.subversive.groceryratings;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by rob on 1/4/15.
 */
public class ProductPageFragment extends Fragment {

    public static ProductPageFragment newInstance() {
        ProductPageFragment newFrag = new ProductPageFragment();
        Bundle args = new Bundle();
        newFrag.setArguments(args);
        return newFrag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.product_page, container, false);
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
