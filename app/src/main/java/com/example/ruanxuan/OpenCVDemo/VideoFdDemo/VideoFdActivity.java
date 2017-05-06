package com.example.ruanxuan.OpenCVDemo.VideoFdDemo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.example.ruanxuan.OpenCVDemo.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class VideoFdActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2{

    private CameraBridgeViewBase mCameraView;
    private CascadeClassifier mFd;
    private File mXmlFile;
    private MatOfRect matOfRect;
    private int frameIdx = 0;
    Thread detectThread;
    private Date startTime;
    private Date stopTimer;

    private BlockingDeque<Mat> showMatQue;
    private BlockingDeque<Mat> processMatQue;

    static {
        System.loadLibrary("opencv_java3");
    }


    private BaseLoaderCallback mbaseLoader = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            super.onManagerConnected(status);

            if(status == LoaderCallbackInterface.SUCCESS){
                if(mCameraView != null){
                    Log.i("rx","start camera!");
                    createCascadeClassifier();
                    mCameraView.enableView();
                }else{
                    Log.i("rx","camera view not init ");
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_fd);

        if(ContextCompat.checkSelfPermission(VideoFdActivity.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(VideoFdActivity.this,new String[]{Manifest.permission.CAMERA},1);
        }

        mCameraView = (CameraBridgeViewBase)findViewById(R.id.cameraView);
        mCameraView.setCvCameraViewListener(this);


        showMatQue = new LinkedBlockingDeque<>(300);
        processMatQue = new LinkedBlockingDeque<>(300);

        matOfRect = new MatOfRect();
        detectThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Mat temMat;
                while(true){
                    try {
                        startTime = new Date(System.currentTimeMillis());
                        temMat = processMatQue.take();
                        mFd.detectMultiScale(temMat, matOfRect);
                        stopTimer = new Date(System.currentTimeMillis());
                        Log.i("rx","coast time:"+(stopTimer.getTime() - startTime.getTime()));

                        for (Rect rect : matOfRect.toArray()) {
                            Imgproc.rectangle(temMat, new Point(rect.x, rect.y),
                                    new Point(rect.x + rect.width, rect.y + rect.height),

                                    new Scalar(255, 0, 0), 5);
                        }
                        showMatQue.push(temMat);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
        detectThread.start();


        if(!OpenCVLoader.initDebug()){
            Log.i("rx","OpenCV not init! return!");
           // OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0,this,mbaseLoader);
            mbaseLoader.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }else{
            Log.i("rx","openCV init done");
        }
    }

    private void createCascadeClassifier(){
        try {

            InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt2);
            File cascadeDir = getDir("cascade", getApplicationContext().MODE_PRIVATE);
            mXmlFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
            FileOutputStream os = new FileOutputStream(mXmlFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            Log.i("rx","file path:"+mXmlFile.getAbsolutePath());

            mFd = new CascadeClassifier(mXmlFile.getAbsolutePath());
            if(mFd.empty()){
                Log.i("rx","Create CascadeClassifier false");
            }else{
                Log.i("rx","Create CascadeClassifier success");
            }


        }catch (IOException e){

        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }


    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

       // return inputFrame.rgba();

        processMatQue.push(inputFrame.rgba());
        if(frameIdx < 60) {
            frameIdx++;
            return null;
        }

        try {
            return showMatQue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(OpenCVLoader.initDebug())
            mbaseLoader.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        else
            Log.i("rx","Load init flase");
    }
}
