package com.example.ruanxuan.OpenCVDemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.ruanxuan.OpenCVDemo.EdgeDetection.EdgeDetectionActivity;
import com.example.ruanxuan.OpenCVDemo.FaceRecognition.FaceRecgnitionActivity;
import com.example.ruanxuan.OpenCVDemo.ImageFdDemo.ImageFDActivity;
import com.example.ruanxuan.OpenCVDemo.ImageMixingDemo.ImageMixingActivity;
import com.example.ruanxuan.OpenCVDemo.R;
import com.example.ruanxuan.OpenCVDemo.VideoFdDemo.VideoFdActivity;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{


    private Button mImageMixBtn ;
    private Button mVideoFdBtn;
    private Button mVideoFrBtn;
    private Button mImageFdBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }


        mImageMixBtn = (Button)findViewById(R.id.imageMixBtn);
        mImageMixBtn.setOnClickListener(this);

        mVideoFdBtn = (Button)findViewById(R.id.videoFdBtn);
        mVideoFdBtn.setOnClickListener(this);

        mImageFdBtn = (Button)findViewById(R.id.imageFdBtn);
        mImageFdBtn.setOnClickListener(this);

        mVideoFrBtn = (Button)findViewById(R.id.frBtn);
        mVideoFdBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()){
            case R.id.videoFdBtn:
                intent.setClass(this,VideoFdActivity.class);
                startActivity(intent);
                break;
            case R.id.imageFdBtn:
                intent.setClass(this,ImageFDActivity.class);
                startActivity(intent);
                break;
            case R.id.imageMixBtn:
                intent.setClass(this,ImageMixingActivity.class);
                startActivity(intent);
                break;
            case R.id.edgeDeteBtn:
                intent.setClass(this,EdgeDetectionActivity.class);
                startActivity(intent);
                break;
            case R.id.frBtn:
                intent.setClass(this,FaceRecgnitionActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }



    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        if(requestCode ==1 && resultCode == RESULT_OK){
            Log.i("rx","Get permisson success");
        }else{
            Log.i("rx","Get permisson false");
        }
    }


}
