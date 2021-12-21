package com.test.corevisionandroidx.core.overlays;

import android.graphics.Bitmap;
import android.widget.Toast;

import com.test.corevisionandroidx.core.capture.BaseCameraCaptureListener;
import com.test.corevisionandroidx.core.capture.ICameraCaptureFragmentListener;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class OpenCVCannyFilterExampleOverlay extends BaseCameraCaptureListener {
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
    public Mat onNewFrame(Bitmap frame, Mat opencvFrame) {
        Mat gray = new Mat(opencvFrame.rows(), opencvFrame.cols(), CvType.CV_8UC1);
        Mat blurred = new Mat(opencvFrame.rows(), opencvFrame.cols(), opencvFrame.type());
        Mat edges = new Mat(opencvFrame.rows(), opencvFrame.cols(), opencvFrame.type());

        Imgproc.GaussianBlur(opencvFrame, blurred, BLUR_KERNEL_SIZE, 1);
        Imgproc.cvtColor(blurred, gray, Imgproc.COLOR_RGBA2GRAY);

        Imgproc.Canny(gray, edges, 100, 100);

        gray.release();
        blurred.release();

        return edges;
    }
}
