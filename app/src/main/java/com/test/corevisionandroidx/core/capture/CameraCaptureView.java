package com.test.corevisionandroidx.core.capture;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Rational;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraX;
import androidx.camera.core.Preview;
import androidx.camera.core.impl.PreviewConfig;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.test.corevisionandroidx.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraCaptureView {
    public static final int CAMERA_MODE_ASPECT_RATIO_3x4 = 1;
    public static final int CAMERA_MODE_ASPECT_RATIO_4x3 = 2;
    public static final int CAMERA_MODE_ASPECT_RATIO_9x16 = 3;
    public static final int CAMERA_MODE_ASPECT_RATIO_16x9 = 4;
    private ConstraintLayout _view = null;
    private PreviewView cameraPreviewView = null;
    private Preview cameraPreview = null;

    private LifecycleOwner owner = null;
    private ExecutorService cameraExecutor = null;

    public static CameraCaptureView create(Activity activity) {
        CameraCaptureView result = new CameraCaptureView();
        LayoutInflater inflater = activity.getLayoutInflater();

        result._view = (ConstraintLayout) inflater.inflate(R.layout.camera_capture_view, null);
        result.connectViews();
        result.initialize();

        return result;
    }

    private void initialize() {
        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    public void setLifeCycleOwner(LifecycleOwner owner) {
        this.owner = owner;
    }

    public void setCameraAspectRatio(int mode) {
        if (mode == CAMERA_MODE_ASPECT_RATIO_3x4) {
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) cameraPreviewView.getLayoutParams();
            layoutParams.dimensionRatio = "3:4";
        } else if (mode == CAMERA_MODE_ASPECT_RATIO_9x16) {
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) cameraPreviewView.getLayoutParams();
            layoutParams.dimensionRatio = "9:16";
        } else if (mode == CAMERA_MODE_ASPECT_RATIO_4x3) {
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) cameraPreviewView.getLayoutParams();
            layoutParams.dimensionRatio = "4:3";
        } else if (mode == CAMERA_MODE_ASPECT_RATIO_16x9) {
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) cameraPreviewView.getLayoutParams();
            layoutParams.dimensionRatio = "16:9";
        }
    }

    private void connectViews() {
        cameraPreviewView = _view.findViewById(R.id.camera_preview);

    }

    public View view() {
        return _view;
    }

    public void disable() {
//        cameraPreview.disableView();
    }

    public void enable() {
        this.cameraPreview = enablePreview();


//        cameraPreview.enableView();
    }

    private Preview enablePreview() {

        Size screen = new Size(cameraPreviewView.getWidth(), cameraPreviewView.getHeight()); //size of the screen

        @SuppressLint("WrongConstant")
        Preview preview = new Preview.Builder()
                .setTargetResolution(screen)
                .build();

        preview.setSurfaceProvider(cameraPreviewView.getSurfaceProvider());

        return preview;
    }

}
