package app.subversive.groceryratings;

import android.content.Context;
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

import com.google.gson.Gson;
import com.google.zxing.client.android.CaptureActivityHandler;
import com.google.zxing.client.android.camera.CameraManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import app.subversive.groceryratings.Core.Product;
import app.subversive.groceryratings.UI.CameraControlsOverlay;
import app.subversive.groceryratings.UI.Overlay;
import app.subversive.groceryratings.UI.ProductRatingBar;
import app.subversive.groceryratings.UI.ScanControlsOverlay;
import app.subversive.groceryratings.test.DebugGroceryService;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;


public class ScanFragment
        extends Fragment
        implements
            SurfaceHolder.Callback,
            Camera.PictureCallback,
            CameraControlsOverlay.Callbacks,
            ScanControlsOverlay.Callbacks,
            ProductRatingBar.BarcodeCallbacks,
            BackFragment        {


    private final static String ARG_RAW_HISTORY = "ARG_RAW_HISTORY";
    CameraManager cameraManager;
    boolean hasSurface;
    private final String TAG = "ScanFrangment";

    CaptureActivityHandler handler;

    private Vibrator mVibrator;
    private String lastScanned;

    private Overlay currOverlay;
    private CameraControlsOverlay cameraControls;
    private ScanControlsOverlay scanControls;

    private byte[] imageData;

    final ArrayList<MenuItem> debugMenuItems = new ArrayList<MenuItem>();

    public static ScanFragment newInstance(String rawHistoryData) {
        ScanFragment fragment = new ScanFragment();
        Bundle args = new Bundle();
        if (rawHistoryData != null && !rawHistoryData.isEmpty()) {
            args.putString(ARG_RAW_HISTORY, rawHistoryData);
        }
        fragment.setArguments(args);
        return fragment;
    }
    public ScanFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mVibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        cameraManager = new CameraManager();
        lastScanned = "";
        cameraControls = new CameraControlsOverlay(this);
        scanControls = new ScanControlsOverlay(this);


        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        Log.i(TAG, "Parceling data");
//        outState.putParcelableArray("products", scanControls.getAllProducts());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean parentHandled = super.onOptionsItemSelected(item);

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
                handleDecode(DebugGroceryService.addNewProduct());
                handled = true;
                break;

            case 3:
                scanControls.showUnknownBarcode(true);
                handled = true;
                break;

            case 4:
                for (MenuItem m : debugMenuItems) {
                    m.setEnabled(item.isChecked());
                }
                handled = true;
                break;

            case 5:
                item.setChecked(!item.isChecked());
                MainWindow.Preferences.autoscan = item.isChecked();
                handled = true;
                break;
        }
        return handled || parentHandled;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        debugMenuItems.add(menu.add(1, 1, 10, "Swap Overlay"));
        debugMenuItems.add(menu.add(1, 2, 20, "Add Product"));
        debugMenuItems.add(menu.add(1, 3, 30, "Show Unknown"));

        for (MenuItem m : debugMenuItems) {
            m.setEnabled(false);
        }

        MenuItem m = menu.add(3,5,8, "Auto Photo");
        m.setCheckable(true);
        m.setChecked(MainWindow.Preferences.autoscan);
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

        Bundle args = getArguments();
        if (args.containsKey(ARG_RAW_HISTORY)) {
            Product[] products = (new Gson()).fromJson(args.getString(ARG_RAW_HISTORY), Product[].class);
            for (int i = products.length-1 ; i >= 0 ; i --) {
                ProductRatingBar pbar = ProductRatingBar.fromProduct(products[i], v.getContext());
                scanControls.addNewProductBar(pbar.getBarcode(), pbar);
            }
        }

        v.setKeepScreenOn(true);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        scanControls.showOverlay(true);
        currOverlay = scanControls;
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
            cameraManager.openDriver(surfaceHolder, getActivity().getWindowManager().getDefaultDisplay());
//            Creating the handler starts the preview, which can also throw a RuntimeException.
            if (handler == null) {
                handler = new CaptureActivityHandler(this, cameraManager);
            }
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
        } catch (RuntimeException e) {
            Log.w(TAG, "Unexpected error initializing camera", e);
        }
    }

    public CaptureActivityHandler getHandler() {
        return handler;
    }

    public void handleDecode(String barcode) {
        Log.i(TAG, barcode);
        scanControls.resetPromptTimer();
        if (!lastScanned.equals(barcode)) {
            scanControls.hideUnknownBarcode(true);
            if (mVibrator != null && mVibrator.hasVibrator()) {
                mVibrator.vibrate(50);
            }
            scanControls.scrollHistoryToBeginning();
            loadProduct(barcode);
            lastScanned = barcode;
        }
        restartScanner();
    }

    private void loadProduct(String barcode) {
        scanControls.addNewRating(barcode, this);
    }

    private void restartScanner() {
        getHandler().handleMessage(Message.obtain(getHandler(), R.id.restart_preview));
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
        final ProductRatingBar pbar = scanControls.getProductBar(0);
        pbar.setState(ProductRatingBar.States.UPLOADING);
        TypedByteArray data =
                new Utils.TypedFileByteArray("image/jpeg", "foo.jpg", imageData);
        MainWindow.imageService.uploadImage(data, new Callback<Response>() {
            @Override
            public void success(Response data, Response response) {
                String id;
                byte[] bytes = new byte[(int) data.getBody().length()];
                try {
                    data.getBody().in().read(bytes, 0, (int) data.getBody().length());
                    id = new String(bytes);
                } catch (IOException e) {
                    Log.i(TAG, "Error reading image ID");
                    return;
                }

                Product p = new Product(true);
                p.productCode = lastScanned;
                p.images.add(id);

                MainWindow.service.addNewProduct(p, new Callback<Product>() {
                    @Override
                    public void success(Product product, Response response) {
                        pbar.setState(ProductRatingBar.States.THANKS);
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
    public void onScanControlsFinishedShow() { }

    @Override
    public void onUnknownBarcode(String barcode) {
        if (MainWindow.Preferences.autoscan) {
            setCapturePhotoMode();
        } else {
            scanControls.showUnknownBarcode(true);
        }
    }

    @Override
    public boolean onBackPressed() {
        if (currOverlay != scanControls) {
            setScanMode();
            return true;
        } else {
            return false;
        }
    }

    public List<Product> getProductHistory() {
        return scanControls.getAllProducts();
    }

    public void setHistory(Product[] products) {

    }
}
