package com.test.corevisionandroidx.core.capture;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

public interface ICameraCaptureFragmentListener {
    void onError(Exception ex, String message);

    void onNewFrame(Bitmap frame, Mat opencvFrame);
}
