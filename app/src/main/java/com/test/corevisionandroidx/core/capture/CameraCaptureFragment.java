package com.test.corevisionandroidx.core.capture;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.impl.ImageOutputConfig;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;
import com.test.corevisionandroidx.R;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraCaptureFragment extends Fragment {
    private ExecutorService cameraExecutor = null;
    private ProcessCameraProvider cameraProvider = null;
    private ICameraCaptureFragmentListener listener = null;
    private int cameraMode = AspectRatio.RATIO_4_3;
    private Preview preview = null;
    private ImageAnalysis imageAnalysis = null;
    private Camera camera = null;
    private PreviewView cameraPreviewSurface = null;
    private ImageView cameraProcessedSurface = null;
    private Bitmap bitmapBuffer = null;

    public CameraCaptureFragment() {
        super(R.layout.camera_capture_fragment);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cameraExecutor = Executors.newSingleThreadExecutor();
        cameraPreviewSurface = view.findViewById(R.id.camera_preview_surface);
        cameraProcessedSurface = view.findViewById(R.id.camera_processed_surface);
        setViewAspectRatio();
        setupCamera();
    }

    private void setViewAspectRatio() {
        cameraMode = this.getArguments() != null ? this.getArguments().getInt("aspect_ratio", AspectRatio.RATIO_4_3) : AspectRatio.RATIO_4_3;

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

    private void setupCamera() {
        Context context = requireContext();
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
            } catch (Exception ex) {
                listener.onError(ex, "Unable to get camera provider.");
                return;
            }

            if (!hasBackCamera()) {
                listener.onError(null, "Device does not have a back camera.");
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
            preview.setSurfaceProvider(cameraPreviewSurface.getSurfaceProvider());
        } catch (Exception ex) {
            listener.onError(ex, "Unable to bind the camera use cases life cycle to fragment.");
        }
    }

    private ImageAnalysis setupImageAnalysis() {
//        ImageAnalysisCon imageAnalysisConfig = new ImageAnalysisConfig.Builder()
//                .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
//                .setCallbackHandler(new Handler(analyzerThread.getLooper()))
//                .setImageQueueDepth(1).build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(getTargetResolution(cameraMode))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, (image) -> {
            processCameraFrame(image);
        });

        return imageAnalysis;
    }

    private void processCameraFrame(ImageProxy image) {
        if (bitmapBuffer == null) {
            bitmapBuffer = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        }

        bitmapBuffer.copyPixelsFromBuffer(image.getPlanes()[0].getBuffer());
        getActivity().runOnUiThread(() -> {
            cameraProcessedSurface.setImageBitmap(bitmapBuffer);
        });
        image.close();
        //        Utils.
//        final Bitmap bitmap = image.getImage();
//
//
//
//        if(bitmap==null)
//            return;
//
//        Mat mat = new Mat();
//        Utils.bitmapToMat(bitmap, mat);
//
//
//        Imgproc.cvtColor(mat, mat, currentImageType);
//        Utils.matToBitmap(mat, bitmap);
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                ivBitmap.setImageBitmap(bitmap);
//            }
//        });
    }

    private Size getTargetResolution(int cameraMode) {
        if (cameraMode == AspectRatio.RATIO_4_3) {
            return new Size(720, 960);
        } else {
            return new Size(720, 1280);
        }
    }

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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        cameraExecutor.shutdown();
    }

    public int getCameraAspectRatio() {
        return this.cameraMode;
    }
}
