package com.example.ruanxuan.OpenCVDemo.FaceRecognition;

import android.Manifest;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.ruanxuan.OpenCVDemo.R;
import com.example.ruanxuan.OpenCVDemo.VideoFdDemo.VideoFdActivity;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FaceRecgnitionActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    private CameraBridgeViewBase mCameraView = null;
    private File mXmlFile;
    private CascadeClassifier mFd;
    private Button mSwitchBtn = null;
    private int mCameraIdx = 0;

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
                    //createCascadeClassifier();
                    //mCameraView.setCameraIndex(mCameraIdx);
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
        setContentView(R.layout.activity_face_recgnition);

        if(ContextCompat.checkSelfPermission(FaceRecgnitionActivity.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(FaceRecgnitionActivity.this,new String[]{Manifest.permission.CAMERA},1);
        }

        mCameraView = (CameraBridgeViewBase)findViewById(R.id.cameraView);
        mCameraView.setCvCameraViewListener(this);

        mSwitchBtn = (Button)findViewById(R.id.cameraSw);
        mSwitchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCameraIdx == 0){
                    mCameraIdx = 1;
                    mCameraView.disableView();
                    mCameraView.setCameraIndex(mCameraIdx);
                    mCameraView.enableView();
                }else{
                    mCameraIdx = 0;
                    mCameraView.disableView();
                    mCameraView.setCameraIndex(mCameraIdx);
                    mCameraView.enableView();
                }

            }
        });
    }

    @Override
    public void onCameraViewStarted(int width, int height) {


    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Log.i("rx","frame in");
        return inputFrame.rgba();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(OpenCVLoader.initDebug())
            mbaseLoader.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        else
            Log.i("rx","Load init flase");
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
}
