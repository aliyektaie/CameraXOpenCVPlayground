package com.test.corevisionandroidx;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.test.corevisionandroidx.core.Constants;
import com.test.corevisionandroidx.core.capture.CameraCaptureView;

import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity implements LoaderCallbackInterface {
    private static final int MY_CAMERA_REQUEST_CODE = 100;

    private LinearLayout cameraOptionsContainer = null;
    private RelativeLayout cameraViewContainer = null;
    private CameraCaptureView currentCameraView = null;

    @Override
    public void onPause()
    {
        super.onPause();
        if (currentCameraView != null)
            currentCameraView.disable();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (OpenCVLoader.initDebug()) {
            onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            Toast.makeText(this, "Can not initialize OpenCV", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (Constants.IS_DEBUG) {
            boolean init = OpenCVLoader.initDebug();
            System.out.println(init);
        }

        cameraOptionsContainer = findViewById(R.id.functions_container);
        cameraViewContainer = findViewById(R.id.camera_view_container);

        findViewById(R.id.cmd_open_calibration).setOnClickListener(this::onCalibrateCameraClick);

        checkCameraPermission();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, getApplicationContext(), this);

        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }

            runOnUiThread(()-> {
                onCalibrateCameraClick(null);
            });
        }).start();
    }

    private void onCalibrateCameraClick(View view) {
        CameraCaptureView cameraView = setupCameraView();

    }

    @NonNull
    private CameraCaptureView setupCameraView() {
        cameraOptionsContainer.setVisibility(View.GONE);
        cameraViewContainer.setVisibility(View.VISIBLE);

        CameraCaptureView view = CameraCaptureView.create(this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        view.view().setLayoutParams(layoutParams);
        cameraViewContainer.addView(view.view());

        view.setCameraAspectRatio(CameraCaptureView.CAMERA_MODE_ASPECT_RATIO_3x4);
        view.enable();

        currentCameraView = view;

        return view;
    }

    private void checkCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onManagerConnected(int status) {
        switch (status) {
            case LoaderCallbackInterface.SUCCESS: {
                for (int i = 0; i < cameraOptionsContainer.getChildCount(); i++) {
                    cameraOptionsContainer.getChildAt(i).setEnabled(true);
                }

                if (currentCameraView != null) {
                    currentCameraView.enable();
                }
            }
            break;
            default: {
                Toast.makeText(this, "Unable to initialize OpenCV", Toast.LENGTH_LONG).show();
            }
            break;
        }
    }

    @Override
    public void onPackageInstall(int operation, InstallCallbackInterface callback) {
        Toast.makeText(this, "Installing OpenCV Package", Toast.LENGTH_LONG).show();
    }
}
