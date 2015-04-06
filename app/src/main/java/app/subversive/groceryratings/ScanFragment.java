package app.subversive.groceryratings;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Point;
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

import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;
import com.google.zxing.client.android.CaptureActivityHandler;

import app.subversive.groceryratings.Core.Variant;
import app.subversive.groceryratings.UI.AOFrameLayout;
import app.subversive.groceryratings.UI.FocusableSurfaceView;
import app.subversive.groceryratings.Core.GRClient;
import app.subversive.groceryratings.UI.TutorialOverlay;
import app.subversive.groceryratings.camera.CameraManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
            TutorialOverlay.Callbacks,
            ProductRatingBar.BarcodeCallbacks,
            BackFragment        {


    private final String TAG = ScanFragment.class.getSimpleName();

    final CaptureActivityHandler handler; { handler = new CaptureActivityHandler(this); }
    private Vibrator mVibrator;
    private String lastScanned;

    private Overlay currOverlay;
    private CameraControlsOverlay cameraControls;
    private ScanControlsOverlay scanControls;
    private TutorialOverlay tutorial;

    private FocusableSurfaceView surfaceView;

    private byte[] imageData;

    final ArrayList<MenuItem> debugMenuItems = new ArrayList<>();
    private ResultPointCallback pointsCallback = new ResultPointCallback() {
        @Override
        public void foundPossibleResultPoint(ResultPoint resultPoint) {
            if (surfaceView != null) {
                surfaceView.PointUpdate(resultPoint);
            }
        }


    };

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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mVibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        lastScanned = "";
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

            // add product
            case 2:
                handleDecode(DebugGroceryService.addNewProduct());
                handled = true;
                break;

            // show unknown
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
                MainWindow.Preferences.tutorialComplete = false;
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
        debugMenuItems.add(menu.add(1, 2, 20, "Add Variant"));
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
        ((MainWindow) getActivity()).setUpNav(null);
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
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final AOFrameLayout v = (AOFrameLayout) inflater.inflate(R.layout.fragment_scan, container, false);



        cameraControls = new CameraControlsOverlay(this);
        scanControls = new ScanControlsOverlay(this);
        cameraControls.attachOverlayToParent(v);
        scanControls.attachOverlayToParent(v);
        if (!MainWindow.Preferences.tutorialComplete) {
            tutorial = new TutorialOverlay(this);
            tutorial.attachOverlayToParent(v);
            tutorial.showOverlay(false);
            currOverlay = tutorial;
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

        v.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                scanControls.onParentLayoutComplete();
                cameraControls.onParentLayoutComplete();
                scanControls.setTouchOffset(-surfaceView.getLeft(), -surfaceView.getTop());

                if (MainWindow.Preferences.tutorialComplete) {
                    setScanMode(false);
                }
                v.removeOnLayoutChangeListener(this);
            }
        });
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AOFrameLayout) getView().findViewById(R.id.root)).restartTimer();
        CameraManager.initializeCamera(getActivity());
        Point r = CameraManager.getCameraResolution();
        surfaceView.setDesiredAspectRatio((float)r.x/(float)r.y);
        initCamera(surfaceView.getHolder());
        CameraManager.startPreview();
        handler.start();

        if (currOverlay == cameraControls) {
            setScanMode(false);
        } else if (currOverlay == scanControls) {
            restartScanner();
        } else if (currOverlay == tutorial) {
            handler.pause();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        ManagedTimer.cancelAll();
        handler.stop();
        CameraManager.stopPreview();
        CameraManager.closeDriver();
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
        ((AOFrameLayout) getView()).mTimer.restart();

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
        handler.start();
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
                new Utils.TypedFileByteArray("image/jpeg", String.format("%s.jpg", lastScanned), imageData);
        GRClient.getImageService().uploadImage(data, new Callback<Response>() {
            @Override
            public void success(Response data, Response response) {
                String id;
                byte[] bytes = new byte[(int) data.getBody().length()];
                try {
                    int read = data.getBody().in().read(bytes, 0, (int) data.getBody().length());
                    id = new String(bytes);
                } catch (IOException e) {
                    Log.i(TAG, "Error reading image ID");
                    pbar.setState(ProductRatingBar.States.ERROR);
                    return;
                }

                Variant p = new Variant();
                p.productCode = lastScanned;
                p.images.add(id);

                GRClient.getService().addNewProduct(p, new Callback<Variant>() {
                    @Override
                    public void success(Variant variant, Response response) {
                        pbar.setState(ProductRatingBar.States.THANKS);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.i(TAG, "Error uploading new product");
                        pbar.setState(ProductRatingBar.States.ERROR);
                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {
                Log.i(TAG, "Error uploading image");
                pbar.setState(ProductRatingBar.States.ERROR);
            }
        });
    }

    @Override
    public void onRetryPicture() {
        restartPhotoPreview();
    }

    @Override
    public void onCameraControlsFinishedShow() {
        ((MainWindow) getActivity()).setUpNav(new MainWindow.UpNavigation() {
            @Override
            public void onNavigateUp() {
                onBackPressed();
            }
        });
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
        if (currOverlay == cameraControls) {
            setScanMode(true);
            return true;
        } else {
            return false;
        }
    }

    public List<Variant> getProductHistory() {
        return scanControls.getAllProducts();
    }

    @Override
    public void onTutorialClicked() {
        tutorial.nextTutorial();
    }

    @Override
    public void onTutorialClosed() {
        MainWindow.Preferences.tutorialComplete = true;
        tutorial.detachOverlayFromParent();
        setScanMode(false);
    }

    public ResultPointCallback getPointsCallback() {
        return pointsCallback;
    }

    @Override
    public void onLoadVariantDetails(int index) {
        ((MainWindow) getActivity()).displayVariantData(index);
    }
}
