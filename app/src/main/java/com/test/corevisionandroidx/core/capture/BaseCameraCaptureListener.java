package com.test.corevisionandroidx.core.capture;

import android.app.Activity;
import android.graphics.Bitmap;

import org.opencv.core.Mat;

public abstract class BaseCameraCaptureListener implements ICameraCaptureFragmentListener {
    protected Activity _activity = null;
    protected CameraCaptureFragment _fragment = null;

    @Override
    public void setContainer(Activity activity, CameraCaptureFragment fragment) {
        _activity = activity;
        _fragment = fragment;
    }
}
