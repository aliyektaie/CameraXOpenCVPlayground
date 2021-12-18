package com.test.corevisionandroidx.core.capture;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.test.corevisionandroidx.R;

public class CameraCaptureView {
    public static final int CAMERA_MODE_ASPECT_RATIO_3x4 = 1;
    public static final int CAMERA_MODE_ASPECT_RATIO_4x3 = 2;
    public static final int CAMERA_MODE_ASPECT_RATIO_9x16 = 3;
    public static final int CAMERA_MODE_ASPECT_RATIO_16x9 = 4;
    private ConstraintLayout _view = null;
    private View cameraPreview = null;

    public static CameraCaptureView create(Activity activity) {
        CameraCaptureView result = new CameraCaptureView();
        LayoutInflater inflater = activity.getLayoutInflater();

        result._view = (ConstraintLayout) inflater.inflate(R.layout.camera_capture_view, null);
        result.connectViews();
        return result;
    }

    public void setCameraAspectRatio(int mode) {
        if (mode == CAMERA_MODE_ASPECT_RATIO_3x4) {
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) cameraPreview.getLayoutParams();
            layoutParams.dimensionRatio = "3:4";
        } else if (mode == CAMERA_MODE_ASPECT_RATIO_9x16) {
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) cameraPreview.getLayoutParams();
            layoutParams.dimensionRatio = "9:16";
        } else if (mode == CAMERA_MODE_ASPECT_RATIO_4x3) {
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) cameraPreview.getLayoutParams();
            layoutParams.dimensionRatio = "4:3";
        } else if (mode == CAMERA_MODE_ASPECT_RATIO_16x9) {
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) cameraPreview.getLayoutParams();
            layoutParams.dimensionRatio = "16:9";
        }
    }

    private void connectViews() {
        cameraPreview = _view.findViewById(R.id.camera_preview);

    }

    public View view() {
        return _view;
    }

    public void disable() {
//        cameraPreview.disableView();
    }

    public void enable() {
//        cameraPreview.enableView();
    }
}
