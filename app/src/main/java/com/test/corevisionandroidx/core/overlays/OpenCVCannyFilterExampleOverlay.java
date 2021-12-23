package com.test.corevisionandroidx.core.overlays;

import android.graphics.Bitmap;
import android.widget.Toast;

import androidx.camera.core.AspectRatio;

import com.test.corevisionandroidx.core.capture.BaseCameraCaptureListener;
import com.test.corevisionandroidx.core.capture.BaseCameraCaptureListener16by9To4by3Cropper;
import com.test.corevisionandroidx.core.capture.CameraFrame;
import com.test.corevisionandroidx.core.capture.ICameraCaptureFragmentListener;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class OpenCVCannyFilterExampleOverlay extends BaseCameraCaptureListener16by9To4by3Cropper {
    private static final Size BLUR_KERNEL_SIZE = new Size(5, 5);

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
    public Mat onNewFrame(CameraFrame frame) {
        Mat opencvFrame = frame.frameWithRequestedSize;
        Mat gray = new Mat(opencvFrame.rows(), opencvFrame.cols(), CvType.CV_8UC1);
        Mat blurred = new Mat(opencvFrame.rows(), opencvFrame.cols(), opencvFrame.type());
        Mat edges = new Mat(opencvFrame.rows(), opencvFrame.cols(), opencvFrame.type());

        Imgproc.GaussianBlur(opencvFrame, blurred, BLUR_KERNEL_SIZE, 1);
        Imgproc.cvtColor(blurred, gray, Imgproc.COLOR_RGBA2GRAY);

        Imgproc.Canny(gray, edges, 100, 100);

        gray.release();
        blurred.release();

        return edges;
//        return opencvFrame;
    }

    @Override
    public android.util.Size getCameraTargetResolution() {
        return new android.util.Size(720, 1280);
    }

    @Override
    public double requiredZoomScale() {
        return 2.0;
    }

    @Override
    public Size requiredFrameSize() {
//        return new Size(480, 853);
        return new Size(480, 640);
//        return new Size(720, 1280);
//        return null;
    }

    @Override
    public boolean requiresCameraPreview() {
        return false;
    }
}
