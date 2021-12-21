package com.test.corevisionandroidx.core.capture;

import android.app.Activity;
import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

public interface ICameraCaptureFragmentListener {
    String getListenerName();

    ICameraCaptureFragmentListener createNewInstance();

    void onError(Exception ex, String message);

    void onNewFrame(Bitmap frame, Mat opencvFrame);

    void setContainer(Activity activity, CameraCaptureFragment fragment);
}
