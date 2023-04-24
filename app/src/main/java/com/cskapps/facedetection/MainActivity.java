package com.cskapps.facedetection;

import android.content.Context;
import android.graphics.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class MainActivity extends CameraActivity {

    private String TAG = "MainActivityLog";
    private TextView faceCount;
    private CameraBridgeViewBase openCvCameraView;

    private File cascadeFile;

    public CascadeClassifier faceCascade;

    private BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case LoaderCallbackInterface
                        .SUCCESS:
                    InputStream is  = getResources().openRawResource(R.raw.haarcascade_frontalface_default);
                    File cascadeDir = getDir("cascade" , Context.MODE_PRIVATE);
                    cascadeFile = new File(cascadeDir, "haarcascade_frontalface_default.xml");

                    try{
                        FileOutputStream fos = new FileOutputStream(cascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;

                        while ((bytesRead = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        fos.close();
                    }catch (IOException e){
                        e.printStackTrace();
                    }

                    faceCascade = new CascadeClassifier(cascadeFile.getAbsolutePath());
                    if(faceCascade.empty()){
                        faceCascade = null;
                    }
                    else{
                        cascadeDir.delete();
                    }
                    Log.v(TAG, "OpenCV loaded");
                    openCvCameraView.enableView();
                    break;
                default:
                    Log.v(TAG, "OpenCV Not loaded");
                    super.onManagerConnected(status);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        faceCount = findViewById(R.id.face_count);

        openCvCameraView = (CameraBridgeViewBase) findViewById(R.id.java_camera_view);
        openCvCameraView.setVisibility(SurfaceView.VISIBLE);
        openCvCameraView.setCvCameraViewListener(cvCameraViewListener);
    }

    private final CameraBridgeViewBase.CvCameraViewListener2 cvCameraViewListener = new CameraBridgeViewBase.CvCameraViewListener2() {
        @Override
        public void onCameraViewStarted(int width, int height) {

        }

        @Override
        public void onCameraViewStopped() {

        }

        @Override
        public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
            Mat input_rgba = inputFrame.rgba();
            Mat input_gray = inputFrame.gray();

            MatOfRect faces = new MatOfRect();
            faceCascade.detectMultiScale(input_gray, faces);
            Rect[] facesArray = faces.toArray();
            runOnUiThread(() -> {
                faceCount.setText(String.valueOf(facesArray.length));
            });
            for (Rect rect : facesArray)
                Imgproc.rectangle(input_rgba, rect.tl(), rect.br(), new Scalar(0, 0, 255), 2);

            return input_rgba;

        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        if (openCvCameraView != null){
            openCvCameraView.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!OpenCVLoader.initDebug()){
            Log.d(TAG, "OpenCV not found, Error");
        }else{
            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (openCvCameraView != null){
            openCvCameraView.disableView();
        }
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(openCvCameraView);
    }
}