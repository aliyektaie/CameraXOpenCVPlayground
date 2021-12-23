package com.test.corevisionandroidx.core.capture;

import android.app.Activity;
import android.graphics.Bitmap;
import org.opencv.core.Mat;
import org.opencv.core.Size;

public interface ICameraCaptureFragmentListener {
    String getListenerName();

    ICameraCaptureFragmentListener createNewInstance();

    void onError(Exception ex, String message);

    Mat onNewFrame(CameraFrame frame);

    void initialize(Activity activity, CameraCaptureFragment fragment);

    android.util.Size getCameraTargetResolution();

    double requiredZoomScale();

    Size requiredFrameSize();
}
