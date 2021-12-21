package com.test.corevisionandroidx.core.overlays;

import android.graphics.Bitmap;
import android.widget.Toast;

import com.test.corevisionandroidx.core.capture.BaseCameraCaptureListener;
import com.test.corevisionandroidx.core.capture.ICameraCaptureFragmentListener;

import org.opencv.core.Mat;

public class OpenCVCannyFilterExampleOverlay extends BaseCameraCaptureListener {
    @Override
    public String getListenerName() {
        return "capture_overlay_canny_filter";
    }

    @Override
    public ICameraCaptureFragmentListener createNewInstance() {
        return new OpenCVCannyFilterExampleOverlay();
    }

    @Override
    public void onError(Exception ex, String message) {
        Toast.makeText(_fragment.getActivity(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNewFrame(Bitmap frame, Mat opencvFrame) {
        System.out.println("here");
    }
}
