package app.subversive.groceryratings;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.FrameLayout;

import com.google.zxing.client.android.CaptureActivityHandler;
import com.google.zxing.client.android.camera.CameraManager;

import java.io.IOException;

import com.google.zxing.Result;

import app.subversive.groceryratings.Core.Product;
import app.subversive.groceryratings.UI.CameraControlsOverlay;
import app.subversive.groceryratings.UI.Overlay;
import app.subversive.groceryratings.UI.ScanControlsOverlay;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;


public class ScanFragment
        extends Fragment
        implements SurfaceHolder.Callback,
                   Camera.PictureCallback,
                   CameraControlsOverlay.Callbacks,
                   ScanControlsOverlay.Callbacks {


    CameraManager cameraManager;
    boolean hasSurface;
    private final String TAG = "ScanFrangment";
    CaptureActivityHandler handler;
    private FlipListener mFlipListener;
    private Vibrator mVibrator;
    private String lastScanned;

    private Overlay currOverlay;
    private CameraControlsOverlay cameraControls;
    private ScanControlsOverlay scanControls;

    private byte[] imageData;

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
        cameraManager = new CameraManager();
        lastScanned = "";
        cameraControls = new CameraControlsOverlay(this);
        scanControls = new ScanControlsOverlay(this);

        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        boolean handled = false;
        switch(item.getItemId()) {
            case 1:
                // swap overlay
                if (currOverlay == cameraControls) {
                    setScanMode();
                } else {
                    setCapturePhotoMode();
                }
                handled = true;
                break;

            case 2:
                scanControls.addNewRating(new Product("Test Product", (int) (Math.random() * 5), (int) (Math.random() * 20)));
                handled = true;
                break;

            case 3:
                scanControls.showUnknownBarcode(true);
                handled = true;
                break;
        }
        return handled;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add(1, 1, 1, "Swap Overlay");
        menu.add(2, 2, 2, "Add Product");
        menu.add(3, 3, 3, "Show Unknown");
    }


    private void takePicture() {
        cameraManager.takePicture(this);
    }

    private void restartPhotoPreview() {
        cameraManager.startPreview();
    }

    private void setCapturePhotoMode() {
        handler.handleMessage(Message.obtain(handler, R.id.pause_scan));
        scanControls.hideOverlay(true);
    }

    private void setScanMode() {
        cameraManager.startPreview();
        restartScanner();
        currOverlay = scanControls;
        cameraControls.hideOverlay(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_scan, container, false);

        SurfaceView surfaceView = (SurfaceView) v.findViewById(R.id.svScan);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            //The activity was paused but not stopped, so the surface still exists. Therefore
            //surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder);
        } else {
            //Install the callback and wait for surfaceCreated() to init the camera.
            surfaceHolder.addCallback(this);
        }

        cameraControls.attachOverlayToParent((FrameLayout) v);
        scanControls.attachOverlayToParent((FrameLayout) v);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mFlipListener.enable();
        scanControls.showOverlay(true);
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
            cameraManager.openDriver(surfaceHolder, getActivity());
            // Creating the handler starts the preview, which can also throw a RuntimeException.
            if (handler == null) {
                handler = new CaptureActivityHandler(this, cameraManager);
            }
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
        } catch (RuntimeException e) {
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
            scanControls.setStatusText("Fetching Product.", true);
            scanControls.hideUnknownBarcode(true);
            if (mVibrator != null && mVibrator.hasVibrator()) {
                mVibrator.vibrate(50);
            }
            scanControls.scrollHistoryToBeginning();
            MainWindow.service.getProduct(rawResult.getText(), onProductLoaded);
            lastScanned = rawResult.getText();
        } else {
            restartScanner();
        }
    }

    private void restartScanner() {
        scanControls.setStatusText("Show me a barcode to scan.", false);
        getHandler().handleMessage(Message.obtain(getHandler(), R.id.restart_preview));
    }

    public void showProductData(Product product) {
        scanControls.addNewRating(product);
    }

    public void serviceError() {
        scanControls.setStatusText("Error access webservice", false);
    }

    private void foundUnknownProduct() {
        scanControls.showUnknownBarcode(true);
    }

    Callback<Product> onProductLoaded = new Callback<Product>() {
        @Override
        public void success(Product product, Response response) {
            if (product == null) {
                foundUnknownProduct();
            } else if (product.published) {
                showProductData(product);
            } else {
                foundUnpublishedProduct(product);
            }
            restartScanner();
        }

        @Override
        public void failure(RetrofitError error) {
            serviceError();
            restartScanner();
        }
    };

    private void foundUnpublishedProduct(Product product) {
        //todo
        // what to do when there is a product but it is unpublished
    }


    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Log.i(TAG, "Picture!");
        imageData = data;
        cameraControls.setConfirmState(true);
    }


    @Override
    public void onTakePicture() {
        takePicture();
    }

    @Override
    public void onConfirmPicture() {
        Utils.formatImage(imageData, new Utils.OnFormattedImage() {
            @Override
            public void Callback(byte[] imageData) {
                Log.i("Callback", "I got my image back");
                uploadImage(imageData);
            }
        });
        imageData = null;
        setScanMode();
    }

    private void uploadImage(byte[] imageData) {
        TypedByteArray data =
                new Utils.TypedFileByteArray("image/jpeg", "foo.jpg", imageData);
        MainWindow.imageService.uploadImage(data, new Callback<Response>() {
            @Override
            public void success(Response data, Response response) {
                String id;
                byte[] bytes = new byte[(int)data.getBody().length()];
                try {
                    data.getBody().in().read(bytes, 0, (int) data.getBody().length());
                    id = new String(bytes);
                } catch (IOException e) {
                    Log.i(TAG, "Error reading image ID");
                    return;
                }

                Product p = new Product(true);
                p.productCode = new String(lastScanned);
                p.images.add(id);

                MainWindow.service.updateProduct(p, new Callback<Product>() {
                    @Override
                    public void success(Product product, Response response) {

                    }

                    @Override
                    public void failure(RetrofitError error) {

                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    @Override
    public void onRetryPicture() {
        restartPhotoPreview();
    }

    @Override
    public void onCancelPicture() {
        setScanMode();
    }

    @Override
    public void onCameraControlsFinishedShow() {

    }

    @Override
    public void onCameraControlsFinishedHide() {
        scanControls.showOverlay(true);
        currOverlay = scanControls;
    }

    @Override
    public void onCaptureNewProductPhoto() {
        setCapturePhotoMode();
    }

    @Override
    public void onScanControlsFinishedHide() {
        cameraControls.showOverlay(true);
        currOverlay = cameraControls;
    }

    @Override
    public void onScanControlsFinishedShow() {

    }
}
