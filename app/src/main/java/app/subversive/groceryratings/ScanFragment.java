package app.subversive.groceryratings;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.zxing.client.android.CaptureActivityHandler;
import com.google.zxing.client.android.camera.CameraManager;

import java.io.IOException;

import com.google.zxing.Result;

import app.subversive.groceryratings.Core.Product;
import app.subversive.groceryratings.UI.ObservableScrollView;
import app.subversive.groceryratings.UI.ProductRatingBar;
import app.subversive.groceryratings.UI.RatingsLayout;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import android.view.View.OnClickListener;


public class ScanFragment
        extends Fragment
        implements SurfaceHolder.Callback,
                   ObservableScrollView.Callbacks,
                   Camera.PictureCallback {


    CameraManager cameraManager;
    boolean hasSurface;
    private final String TAG = "ScanFrangment";
    CaptureActivityHandler handler;
    private FlipListener mFlipListener;
    private Vibrator mVibrator;
    private String lastScanned;

    private final ViewGroup.LayoutParams defaultLP =
            new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    ObservableScrollView mScrollView;

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
        mFlipListener = new FlipListener(getActivity(), new FlipListener.OnFlipListener() {
            @Override
            public void onFlip() {
                if (cameraManager != null) {
                    cameraManager.flipPreview();
                }
            }
        });
        mVibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        cameraManager = new CameraManager(getActivity());
        lastScanned = "";
    }

    private void setCaptureLayout() {
        mScrollView.setVisibility(View.GONE);
        getView().findViewById(R.id.statusBar).setVisibility(View.GONE);
        getView().findViewById(R.id.unknownBarcode).setVisibility(View.GONE);

        getView().findViewById(R.id.btnCancelTakePicture).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.btnTakePicture).setVisibility(View.VISIBLE);
    }

    private void setScanLayout() {
        mScrollView.setVisibility(View.VISIBLE);
        getView().findViewById(R.id.statusBar).setVisibility(View.VISIBLE);

        getView().findViewById(R.id.btnCancelTakePicture).setVisibility(View.GONE);
        getView().findViewById(R.id.btnTakePicture).setVisibility(View.GONE);
        getView().findViewById(R.id.btnRetakePicture).setVisibility(View.GONE);
    }

    private void takePicture() {
        cameraManager.takePicture(this);
    }

    private void restartPhotoPreview() {
        cameraManager.startPreview();
        getView().findViewById(R.id.btnRetakePicture).setVisibility(View.GONE);
    }

    private void setCapturePhotoMode() {
        handler.handleMessage(Message.obtain(handler, R.id.pause_scan));
        setCaptureLayout();
    }

    private void setScanMode() {
        setScanLayout();
        cameraManager.startPreview();
        restartScanner();
        setCameraIconTakePicture();
    }

    final OnClickListener onTakePicture = new OnClickListener() {
        @Override
        public void onClick(View v) {
            takePicture();
        }
    };
    final OnClickListener onConfirmPicture = new OnClickListener() {
        @Override
        public void onClick(View v) {
            setScanMode();
        }
    };



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_scan, container, false);
        mScrollView = ((ObservableScrollView) v.findViewById(R.id.scrollView));
        mScrollView.addCallbacks(this);

        v.findViewById(R.id.tvNoScanBarcode).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                v.findViewById(R.id.unknownBarcode).setVisibility(View.GONE);
            }
        });

        v.findViewById(R.id.tvYesScanBarcode).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setCapturePhotoMode();
            }
        });

        v.findViewById(R.id.btnCancelTakePicture).setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 setScanMode();
             }
         });

        v.findViewById(R.id.btnTakePicture).setOnClickListener(onTakePicture);

        v.findViewById(R.id.btnRetakePicture).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setCameraIconTakePicture();
                restartPhotoPreview();
            }
        });

        SurfaceView surfaceView = (SurfaceView) v.findViewById(R.id.svScan);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
//            The activity was paused but not stopped, so the surface still exists. Therefore
//            surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder);
        } else {
//            Install the callback and wait for surfaceCreated() to init the camera.
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
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us null surface!");
        }
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    public void setStatusText(String text, boolean showSpinner) {
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
            getView().findViewById(R.id.unknownBarcode).setVisibility(View.GONE);
            setStatusText("Fetching Product.", true);
            if (mVibrator != null && mVibrator.hasVibrator()) {
                mVibrator.vibrate(50);
            }
            mScrollView.smoothScrollTo(0,0);
            MainWindow.service.getProduct(rawResult.getText(), onProductLoaded);
            lastScanned = rawResult.getText();
        } else {
            restartScanner();
        }
    }

    private void restartScanner() {
        setStatusText("Show me a barcode to scan.", false);
        getHandler().handleMessage(Message.obtain(getHandler(), R.id.restart_preview));
    }

    public void showProductData(Product product) {
        ProductRatingBar pbar = new ProductRatingBar(getActivity());
        pbar.setProduct(product);
        ((RatingsLayout) getView().findViewById(R.id.RatingHolder)).addView(pbar, 0, defaultLP);
    }

    public void serviceError() {
        setStatusText("Error access webservice", false);
    }

    private void foundUnknownProduct() {
        getView().findViewById(R.id.unknownBarcode).setVisibility(View.VISIBLE);
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
            serviceError();
            restartScanner();
        }
    };



    @Override
    public void onScrollChanged(int deltaX, int deltaY) {
        getView().findViewById(R.id.unknownBarcode).setVisibility(View.GONE);
        if (mScrollView.getScrollY() == 0) {
//            cameraManager.startPreview();
        } else {
//            cameraManager.stopPreview();
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Log.i(TAG, "Picture!");
        //final Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
//        Log.i(TAG,String.format("%d x %d", image.getWidth(), image.getHeight()));
        getView().findViewById(R.id.btnRetakePicture).setVisibility(View.VISIBLE);
        setCameraIconConfirm();
    }

    private void setCameraIconConfirm() {
        ImageButton camButton = (ImageButton) getView().findViewById(R.id.btnTakePicture);
        camButton.setBackgroundResource(R.drawable.circle_green);
        camButton.setImageResource(R.drawable.ic_action_accept);
        camButton.setOnClickListener(onConfirmPicture);
    }

    private void setCameraIconTakePicture() {
        ImageButton camButton = (ImageButton) getView().findViewById(R.id.btnTakePicture);
        camButton.setBackgroundResource(R.drawable.circle);
        camButton.setImageResource(R.drawable.ic_action_camera);
        camButton.setOnClickListener(onTakePicture);
    }
}
