package app.subversive.groceryratings;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.TextView;

import com.google.zxing.client.android.CaptureActivityHandler;
import com.google.zxing.client.android.camera.CameraManager;

import java.io.IOException;

import com.google.zxing.Result;

import app.subversive.groceryratings.Core.Product;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class ScanFragment extends Fragment implements SurfaceHolder.Callback{
    CameraManager cameraManager;
    boolean hasSurface;
    private final String TAG = "ScanFrangment";
    CaptureActivityHandler handler;
    private FlipListener mFlipListener;
    private Vibrator mVibrator;
    private String lastScanned;

    public static ScanFragment newInstance() {
        ScanFragment fragment = new ScanFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    public ScanFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFlipListener = new FlipListener(this.getActivity(), new FlipListener.OnFlipListener() {
            @Override
            public void onFlip() {
                if (cameraManager != null) {
                    cameraManager.flipPreview();
                }
            }
        });
        mVibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        lastScanned = "";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_scan, container, false);
        cameraManager = new CameraManager(getActivity());

        SurfaceView surfaceView = (SurfaceView) v.findViewById(R.id.svScan);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder);
        } else {
            // Install the callback and wait for surfaceCreated() to init the camera.
            surfaceHolder.addCallback(this);
        }
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        mFlipListener.enable();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        cameraManager.closeDriver();
        if (!hasSurface) {
            SurfaceView surfaceView = (SurfaceView) getView().findViewById(R.id.svScan);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
        mFlipListener.disable();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    public void setStatusText(String text, boolean showSpinner) {
//        ((StatusBar) getView().findViewById(R.id.statusBar)).setText(text, showSpinner);
        ((TextView) getView().findViewById(R.id.statusText)).setText(text);
        getView().findViewById(R.id.progressBar).setVisibility(showSpinner ? View.VISIBLE : View.GONE);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a RuntimeException.
            if (handler == null) {
                handler = new CaptureActivityHandler(this, null, null, null, cameraManager);
            }
//            decodeOrStoreSavedBitmap(null, null);
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Log.w(TAG, "Unexpected error initializing camera", e);
        }
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    public CaptureActivityHandler getHandler() {
        return handler;
    }

    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        Log.i(TAG, rawResult.getText());
        if (!lastScanned.equals(rawResult.getText())) {
            setStatusText("Fetching Product.", true);
            if (mVibrator != null && mVibrator.hasVibrator()) {
                mVibrator.vibrate(50);
            }
            MainWindow.service.getProduct(rawResult.getText(), onProductLoaded);
            lastScanned = rawResult.getText();
        }
    }

    private void restartScanner() {
        setStatusText("Show me a barcode to scan.", false);
        Message m = new Message();
        m.what = R.id.restart_preview;
        getHandler().handleMessage(m);
    }

    public void showProductData(Product product) {
        setStatusText(product.getProductName(), false);
    }

    public void serviceError() {
        setStatusText("Error access webservice", false);
    }

    private void foundUnknownProduct() {

    }

    Callback<Product> onProductLoaded = new Callback<Product>() {
        @Override
        public void success(Product product, Response response) {
            if (product == null) {
                foundUnknownProduct();
            } else {
                showProductData(product);
            }
            restartScanner();
        }

        @Override
        public void failure(RetrofitError error) {

        }
    };
}