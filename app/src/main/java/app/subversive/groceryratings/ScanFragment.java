package app.subversive.groceryratings;

import android.app.Fragment;
import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.gson.Gson;
import com.google.zxing.client.android.CaptureActivityHandler;

import app.subversive.groceryratings.UI.FocusableSurfaceView;
import app.subversive.groceryratings.camera.CameraManager;

import java.io.IOException;
import java.util.ArrayList;
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
    private final String TAG = ScanFragment.class.getSimpleName();

    CaptureActivityHandler handler;
    {
        handler = new CaptureActivityHandler(this);
    }
    private Vibrator mVibrator;
    private String lastScanned;

    private Overlay currOverlay;
    private CameraControlsOverlay cameraControls;
    private ScanControlsOverlay scanControls;

    private FocusableSurfaceView surfaceView;

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
        lastScanned = "";
        cameraControls = new CameraControlsOverlay(this);
        scanControls = new ScanControlsOverlay(this);


        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean parentHandled = super.onOptionsItemSelected(item);

        boolean handled = false;
        switch(item.getItemId()) {
            case 1:
                // swap overlay
                if (currOverlay == cameraControls) {
                    setScanMode(true);
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

            case 6:
                ProductRatingBar pbar = scanControls.getProductBar(0);
                if (pbar != null) {
                    pbar.flash();
                }
                handled = true;
                break;
            case 7:
                scanControls.flushHistory();
                getActivity().finish();
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

        menu.add(4,6,40, "Flash");
        menu.add(5,7,50, "Flush(Exit)");
    }


    private void takePicture() {
        cameraControls.setPendingState(false);
        CameraManager.takePicture(cameraControls, this);
    }

    private void restartPhotoPreview() {
        CameraManager.startPreview();
    }

    private void setCapturePhotoMode() {
        handler.pause();
        scanControls.hideOverlay(true);

    }

    private void setScanMode(boolean animated) {
        CameraManager.startPreview();
        restartScanner();
        currOverlay = scanControls;
        cameraControls.hideOverlay(animated);
        if (!animated) {
            scanControls.showOverlay(false);
        }
    }


    GestureDetector.SimpleOnGestureListener touchListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            onTouchUp(e.getX(), e.getY());

            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_scan, container, false);

        v.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                scanControls.onParentLayoutComplete();
                cameraControls.onParentLayoutComplete();
                setScanMode(true);
                v.removeOnLayoutChangeListener(this);
            }
        });

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

        final GestureDetector gd = new GestureDetector(getActivity(), touchListener);

        surfaceView = (FocusableSurfaceView) v.findViewById(R.id.svScan);
        surfaceView.setWillNotDraw(false);
        surfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gd.onTouchEvent(event);
            }
        });
        surfaceView.getHolder().addCallback(this);

        v.setKeepScreenOn(true);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.start();
        if (currOverlay == cameraControls) {
            setScanMode(false);
        }
        if (surfaceView != null) {
            initCamera(surfaceView.getHolder());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.stop();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        initCamera(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        try {
            CameraManager.setPreviewDisplay(surfaceHolder);
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
        }
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
        } else {
            scanControls.flashTop();
        }
        restartScanner();
    }

    private void loadProduct(String barcode) {
        scanControls.addNewRating(barcode, this);
    }

    private void restartScanner() {
        handler.unpause();
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
        setScanMode(true);
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
        setScanMode(true);
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
    public void onTouchUp(float x, float y) {

        surfaceView.setFocus(x, y);

        CameraManager.autoFocus(
                x,
                y,
                surfaceView.getRadius(),
                surfaceView.getWidth(),
                surfaceView.getHeight(),
                new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        surfaceView.unsetFocus();
                    }
                });
    }

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
            setScanMode(true);
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
