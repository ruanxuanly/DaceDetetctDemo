package com.example.ruanxuan.OpenCVDemo.ImageFdDemo;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ruanxuan.OpenCVDemo.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.Utils;
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

public class ImageFDActivity extends AppCompatActivity implements View.OnClickListener{

    private Button selectBtn;
    private Button detectBtn;
    private TextView resultView;
    private Bitmap srcBitmap;
    private File mXmlFile;
    private CascadeClassifier mFd;
    private ImageView mImageView;

    static {
        System.loadLibrary("opencv_java3");
    }

    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {

            if(status == LoaderCallbackInterface.SUCCESS){
                Log.i("rx","Load OpenCV success");
                createCascadeClassifier();
            }else{
                Log.i("rx","Load OpenCV false ");
                super.onManagerConnected(status);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_fd);

        selectBtn = (Button)findViewById(R.id.selectPhoto);
        selectBtn.setOnClickListener(this);
        detectBtn = (Button)findViewById(R.id.detectBtn);
        detectBtn.setOnClickListener(this);
        resultView = (TextView)findViewById(R.id.resultText);
        mImageView = (ImageView)findViewById(R.id.showView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.detectBtn:
                Log.i("rx","detect face btn onclick");

                if(srcBitmap != null){
                    Mat srcMat = new Mat();
                    Utils.bitmapToMat(srcBitmap,srcMat);
                    MatOfRect matOfRect = new MatOfRect();
                    int count = 0;
                    mFd.detectMultiScale(srcMat,matOfRect);
//                    rect = matOfRect.toArray()[0];

                    for (Rect rect: matOfRect.toArray() ){
                        Imgproc.rectangle(srcMat,new Point(rect.x,rect.y),
                                new Point(rect.x + rect.width,rect.y+rect.height),
                                new Scalar(255,0,0),5);
                        count++;
                    }

                    Utils.matToBitmap(srcMat,srcBitmap);
                    mImageView.setImageBitmap(srcBitmap);
                    resultView.setText(count + "个");
                }
                break;
            case R.id.selectPhoto:
                Log.i("rx","select photo btn onclick");
                Intent photoPicker = new Intent(Intent.ACTION_PICK);
                photoPicker.setType("image/*");
                startActivityForResult(photoPicker,0);
                break;
            default:
                break;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);


        if(requestCode == 0 && resultCode == RESULT_OK && data.getData() != null){
            Log.i("rx","get Data success");

            Cursor cursor = getContentResolver().query(data.getData(),null,null,null,null);
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            String filePath = cursor.getString(idx);
            Log.i("rx","Get photo path:"+filePath);

/*
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            options.inSampleSize = Math.max(1, (int) Math.ceil(Math.max(
                    (double) options.outWidth / 1024f,
                    (double) options.outHeight / 1024f)));
            options.inJustDecodeBounds = false;
            */
            srcBitmap = BitmapFactory.decodeFile(filePath);

            mImageView.setImageBitmap(srcBitmap);

        }else{
            Log.i("rx","get Data false");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);

//        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0,this,baseLoaderCallback);OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0,this,baseLoaderCallback);
    }
}
