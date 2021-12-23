package com.test.corevisionandroidx.core.capture;

import android.graphics.Bitmap;

public abstract class BaseCameraCaptureListener16by9To4by3Cropper extends BaseCameraCaptureListener implements ICustomCameraFrameCropperOverlay {
    @Override
    public Bitmap cropCameraFrame(Bitmap frame) {
        int newHeight = (int) (frame.getWidth() * 4.0 / 3);
        int newY = (frame.getHeight() - newHeight) / 2;
        return Bitmap.createBitmap(frame, 0, newY, frame.getWidth(), newHeight);
    }
}
