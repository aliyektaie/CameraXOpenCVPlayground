package com.test.corevisionandroidx.core.capture;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.core.impl.ImageOutputConfig;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;
import com.test.corevisionandroidx.R;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraCaptureFragment extends Fragment {
    private ExecutorService cameraExecutor = null;
    private ProcessCameraProvider cameraProvider = null;
    private ICameraCaptureFragmentListener listener = null;
    private int cameraMode = AspectRatio.RATIO_4_3;
    private Preview preview = null;
    private Camera camera = null;
    private PreviewView cameraPreviewSurface = null;

    public CameraCaptureFragment() {
        super(R.layout.camera_capture_fragment);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cameraExecutor = Executors.newSingleThreadExecutor();
        cameraPreviewSurface = view.findViewById(R.id.camera_preview_surface);
        setupCamera();
    }

    private void setupCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
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

        cameraProvider.unbindAll();
        try {
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview);
            preview.setSurfaceProvider(cameraPreviewSurface.getSurfaceProvider());
        } catch (Exception ex) {
            listener.onError(ex, "Unable to bind the camera use cases life cycle to fragment.");
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
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        cameraExecutor.shutdown();
    }

    public void setCameraAspectRatio(int mode) {
        this.cameraMode = mode;
    }

    public int getCameraAspectRatio() {
        return this.cameraMode;
    }
}
