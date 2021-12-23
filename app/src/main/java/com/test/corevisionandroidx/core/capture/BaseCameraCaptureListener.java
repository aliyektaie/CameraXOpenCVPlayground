package com.test.corevisionandroidx.core.capture;

import android.app.Activity;

public abstract class BaseCameraCaptureListener implements ICameraCaptureFragmentListener {
    protected Activity _activity = null;
    protected CameraCaptureFragment _fragment = null;

    @Override
    public void initialize(Activity activity, CameraCaptureFragment fragment) {
        _activity = activity;
        _fragment = fragment;
    }
}
