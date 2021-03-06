package com.test.corevisionandroidx.core.capture;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.camera2.CameraCharacteristics;
import android.os.Bundle;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.camera2.interop.Camera2CameraInfo;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;
import com.test.corevisionandroidx.R;
import com.test.corevisionandroidx.core.overlays.OpenCVCannyFilterExampleOverlay;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraCaptureFragment extends Fragment implements IFramePerSecondCounterListener {
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;

    private ExecutorService cameraExecutor = null;
    private ProcessCameraProvider cameraProvider = null;
    private ICameraCaptureFragmentListener listener = null;
    private int cameraMode = AspectRatio.RATIO_4_3;
    private Preview preview = null;
    private ImageAnalysis imageAnalysis = null;
    private Camera camera = null;
    private PreviewView cameraPreviewSurface = null;
    private TextView cameraDebugInformation = null;
    private ImageView cameraProcessedSurface = null;
    private Bitmap bitmapBuffer = null;
    private Bitmap previousFrame = null;
    private int cameraSensorOrientation = -1;
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final Map<String, ICameraCaptureFragmentListener> availableListeners = new HashMap<>();
    private FramePerSecondCounter fpsCounter = null;

    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);

        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);

        populateAvailableListeners();
    }

    private static void populateAvailableListeners() {
        addAvailableListener(OpenCVCannyFilterExampleOverlay.class);
    }

    private static void addAvailableListener(Class cls) {
        try {
            ICameraCaptureFragmentListener listener = (ICameraCaptureFragmentListener) cls.newInstance();
            availableListeners.put(listener.getListenerName(), listener);
        } catch (Exception e) {
            // This should not happen. Check why it is the case!
            throw new RuntimeException(e);
        }
    }

    public CameraCaptureFragment() {
        super(R.layout.camera_capture_fragment);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupListener();

        cameraExecutor = Executors.newSingleThreadExecutor();
        cameraPreviewSurface = view.findViewById(R.id.camera_preview_surface);
        cameraProcessedSurface = view.findViewById(R.id.camera_processed_surface);
        cameraDebugInformation = view.findViewById(R.id.camera_debug_information);
        setViewAspectRatio();
        setupCamera();


        setupFramePerSecondCounter();

        if (!listener.requiresCameraPreview()) {
            cameraPreviewSurface.setVisibility(View.INVISIBLE);
        }
    }

    private void setupFramePerSecondCounter() {
        fpsCounter = new FramePerSecondCounter();
        fpsCounter.setListener(this);
        fpsCounter.start();
    }

    private void setupListener() {
        String listenerName = null;
        if (getArguments() != null) {
            listenerName = getArguments().getString("listener_name", null);
        }

        if (listenerName == null) throw new RuntimeException();
        listener = availableListeners.get(listenerName).createNewInstance();
        listener.initialize(getActivity(), this);
    }

    private void setViewAspectRatio() {
        cameraMode = getAspectRatioFromRequestedOutput(listener.requiredFrameSize());

        ConstraintLayout.LayoutParams previewLayoutParams = (ConstraintLayout.LayoutParams) cameraPreviewSurface.getLayoutParams();
        ConstraintLayout.LayoutParams processedLayoutParams = (ConstraintLayout.LayoutParams) cameraProcessedSurface.getLayoutParams();
        if (cameraMode == AspectRatio.RATIO_4_3) {
            previewLayoutParams.dimensionRatio = "3:4";
            processedLayoutParams.dimensionRatio = "3:4";
        } else if (cameraMode == AspectRatio.RATIO_16_9) {
            previewLayoutParams.dimensionRatio = "9:16";
            processedLayoutParams.dimensionRatio = "9:16";
        } else {
            throw new RuntimeException();
        }
    }

    private int getAspectRatioFromRequestedOutput(org.opencv.core.Size size) {
        Size s = listener.getCameraTargetResolution();
        if (size == null) size = new org.opencv.core.Size(s.getWidth(), s.getHeight());
        double ratio = size.width / size.height;
        int result = 0;

        if (Math.abs(ratio - (3.0 / 4.0)) < 0.1) {
            result = AspectRatio.RATIO_4_3;
        } else if (Math.abs(ratio - (9.0 / 16.0)) < 0.1) {
            result = AspectRatio.RATIO_16_9;
        }

        return result;
    }

    private void setupCamera() {
        Context context = requireContext();
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
            } catch (Exception ex) {
                invokeListenerOnError(ex, "Unable to get camera provider.");
                return;
            }

            if (!hasBackCamera()) {
                invokeListenerOnError(null, "Device does not have a back camera.");
            }

            bindCameraUseCases();
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindCameraUseCases() {
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        preview = new Preview.Builder()
                .setTargetAspectRatio(cameraMode)
                .setTargetRotation(Surface.ROTATION_0)
                .build();

        imageAnalysis = setupImageAnalysis();

        cameraProvider.unbindAll();
        try {
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
            camera.getCameraControl().setZoomRatio((float) listener.requiredZoomScale());

            @SuppressLint({"RestrictedApi", "UnsafeOptInUsageError"})
            CameraCharacteristics cameraCharacteristics = Camera2CameraInfo.extractCameraCharacteristics(camera.getCameraInfo());
            this.cameraSensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

            preview.setSurfaceProvider(cameraPreviewSurface.getSurfaceProvider());
        } catch (Exception ex) {
            invokeListenerOnError(ex, "Unable to bind the camera use cases life cycle to fragment.");
        }
    }

    private ImageAnalysis setupImageAnalysis() {
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(listener.getCameraTargetResolution())
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, this::processCameraFrame);

        return imageAnalysis;
    }

    private void processCameraFrame(ImageProxy image) {
        if (fpsCounter != null) fpsCounter.onFrame();
        if (bitmapBuffer == null) {
            bitmapBuffer = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        }

        bitmapBuffer.copyPixelsFromBuffer(image.getPlanes()[0].getBuffer());

        Activity activity = getActivity();

        if (activity != null) {
            CameraFrame frame = new CameraFrame();
            frame.originalCameraFrame = adjustBitmapOrientation(activity, bitmapBuffer);
            setAdjustedFrame(frame);
            Mat frameToDisplay = invokeListenerNewFrame(frame);
            Bitmap bitmapToDisplay = Bitmap.createBitmap(frameToDisplay.cols(), frameToDisplay.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(frameToDisplay, bitmapToDisplay);
            frame.originalCameraFrame.recycle();

            activity.runOnUiThread(() -> {
                cameraProcessedSurface.setImageBitmap(bitmapToDisplay);
                releaseMemory(bitmapToDisplay, frame.frameWithRequestedSize);
            });
        } else {
            invokeListenerOnError(null, "Can not access the containing activity.");
        }
        image.close();
    }

    private void setAdjustedFrame(CameraFrame frame) {
        frame.frameWithRequestedSize = new Mat();

        org.opencv.core.Size requested = listener.requiredFrameSize();
        double dx = 1.0;
        double dy = 1.0;

        if (requested != null) {
            dx = requested.width / frame.frameWithRequestedSize.cols();
            dy = requested.height / frame.frameWithRequestedSize.rows();
        }

        if (dx != 1.0 || dy != 1.0) {
//            int mode = Imgproc.INTER_AREA;
//            if (dx > 1.0 || dy > 1.0) mode = Imgproc.INTER_CUBIC;
//            Mat resized = new Mat();
//            Imgproc.resize(frame.frameWithRequestedSize, resized, new org.opencv.core.Size(), dx, dy, mode);
//            frame.frameWithRequestedSize.release();
//            frame.frameWithRequestedSize = resized;

            Bitmap resized = Bitmap.createScaledBitmap(frame.originalCameraFrame, (int) requested.width, (int) requested.height, false);
            Utils.bitmapToMat(resized, frame.frameWithRequestedSize);
            resized.recycle();
        } else {
            Utils.bitmapToMat(frame.originalCameraFrame, frame.frameWithRequestedSize);
        }
    }

    private void releaseMemory(Bitmap frame, Mat frameMat) {
        frameMat.release();

        if (previousFrame != null) previousFrame.recycle();
        previousFrame = frame;
    }

    private void invokeListenerOnError(Exception ex, String message) {
        if (listener != null) {
            listener.onError(ex, message);
        }
    }

    private Mat invokeListenerNewFrame(CameraFrame frame) {
        Mat result = null;

        if (listener != null) {
            try {
                result = listener.onNewFrame(frame);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        return result;
    }

    @SuppressLint({"UnsafeOptInUsageError", "RestrictedApi"})
    private Bitmap adjustBitmapOrientation(Activity activity, Bitmap buffer) {
        int rotation = getDeviceRotation(activity);
        switch (cameraSensorOrientation) {
            case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                rotation = DEFAULT_ORIENTATIONS.get(rotation);
                break;
            case SENSOR_ORIENTATION_INVERSE_DEGREES:
                rotation = INVERSE_ORIENTATIONS.get(rotation);
                break;
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);
        Bitmap temp = Bitmap.createBitmap(buffer, 0, 0, buffer.getWidth(), buffer.getHeight(), matrix, true);
        Bitmap result = temp;

        if (listener instanceof ICustomCameraFrameCropperOverlay) {
            result = ((ICustomCameraFrameCropperOverlay)listener).cropCameraFrame(temp);
            temp.recycle();
            temp = null;
        }

        return result;
    }

    private int getDeviceRotation(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        return rotation;
    }


//    private Size getTargetResolution(int cameraMode) {
//        if (cameraMode == AspectRatio.RATIO_4_3) {
//            return new Size(720, 960);
//        } else {
//            return new Size(720, 1280);
//        }
//    }

    private boolean hasBackCamera() {
        try {
            return cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA);
        } catch (CameraInfoUnavailableException e) {
            return false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (fpsCounter != null) fpsCounter.resume();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (fpsCounter != null) fpsCounter.pause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (fpsCounter != null) fpsCounter.stop();
        cameraExecutor.shutdown();
    }

    public int getCameraAspectRatio() {
        return this.cameraMode;
    }

    @Override
    public void onFramePerSecondUpdate(int fps) {
        String content = String.format("FPS: %d", fps);
        getActivity().runOnUiThread(() -> {
            cameraDebugInformation.setText(content);
        });
    }
}
