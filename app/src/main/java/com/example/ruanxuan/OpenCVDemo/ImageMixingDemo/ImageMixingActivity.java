package com.example.ruanxuan.OpenCVDemo.ImageMixingDemo;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;


import com.example.ruanxuan.OpenCVDemo.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class ImageMixingActivity extends AppCompatActivity implements View.OnClickListener,SeekBar.OnSeekBarChangeListener{

    static {
        System.loadLibrary("opencv_java3");
    }

    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {

            if(status == LoaderCallbackInterface.SUCCESS){
                Log.i("rx","Load OpenCV success");
            }else{
                Log.i("rx","Load OpenCV false ");
                super.onManagerConnected(status);
            }
        }
    };


    private ImageView mBottomImageView;
    private MoveImageView mTopImageView;
    private View mImageView;
    private Bitmap mBtmSrcBitmap = null;
    private Bitmap mTopSrcBitmap = null;
    private Button mSelBtmImageBtn;
    private Button mSaveImageBtn;
    private Button mSelTopImageBtn;
    private SeekBar mAlphaSeek;
    private static final int  BOTTOM_SELCLEC_IMAGE_REQUEST = 100;
    private static final int  TOP_SELCLEC_IMAGE_REQUEST = 101;
    private int screenHeight = 0;
    private float alphaVal = 0;
    private boolean isBottomImageRotate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_mixing);

        mAlphaSeek = (SeekBar)findViewById(R.id.alphaBar);
        mAlphaSeek.setOnSeekBarChangeListener(this);

        screenHeight = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getHeight();

        mBottomImageView = (ImageView) findViewById(R.id.bottomImageView);
        mTopImageView = (MoveImageView) findViewById(R.id.topImageView);
        mImageView = findViewById(R.id.imageView);

        mBottomImageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mTopImageView.setLimitPosition(mBottomImageView.getLeft(),mBottomImageView.getTop(),mBottomImageView.getRight(),mBottomImageView.getBottom());
                Log.i("rx","Bottom Image left:"+mBottomImageView.getLeft()+"Top:"+mBottomImageView.getTop()+
                        "Right:"+mBottomImageView.getRight()+"Bottom:"+mBottomImageView.getBottom());
            }
        });



//        mTopImageView.setOnTouchListener(new TouchListen());

        mSelBtmImageBtn = (Button)findViewById(R.id.selBottomImgBtn);
        mSelBtmImageBtn.setOnClickListener(this);
        mSelTopImageBtn = (Button)findViewById(R.id.selTopImgBtn);
        mSelTopImageBtn.setOnClickListener(this);
        mSaveImageBtn = (Button)findViewById(R.id.saveBtn);
        mSaveImageBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.selBottomImgBtn: {
                Log.i("rx", "select Bottom image\n");
                Intent photoPicker = new Intent(Intent.ACTION_PICK);
                photoPicker.setType("image/*");
                startActivityForResult(photoPicker, BOTTOM_SELCLEC_IMAGE_REQUEST);
            }
                break;
            case R.id.selTopImgBtn: {
                Log.i("rx", "select Top image\n");
                Intent photoPicker = new Intent(Intent.ACTION_PICK);
                photoPicker.setType("image/*");
                startActivityForResult(photoPicker, TOP_SELCLEC_IMAGE_REQUEST);
            }
            break;
            case R.id.saveBtn:
                if(mBtmSrcBitmap == null) {
                    Toast.makeText(getApplicationContext(),new String("Please select bottom image"), Toast.LENGTH_SHORT).show();
                    break;
                }
                if(mTopSrcBitmap == null) {
                    Toast.makeText(getApplicationContext(),new String("Please select top image"), Toast.LENGTH_SHORT).show();
                    break;
                }


                int x = mTopImageView.getLeft() - mBottomImageView.getLeft();
                int y = 0;
                if(mBtmSrcBitmap.getHeight() <= mBottomImageView.getHeight()){
                    y = mTopImageView.getTop() - mBottomImageView.getTop();
                }else {
                    y = (mTopImageView.getTop() - mBottomImageView.getTop())*((screenHeight-getStatusBarHeight())/mBottomImageView.getBottom());
                }
                int w = mTopImageView.getRight() - x;
                int h = mTopImageView.getBottom() - y;

                Log.i("rx","(x,y,w,h) -> ("+x+","+y+","+w+","+h+")");
                saveImage(mTopSrcBitmap,mBtmSrcBitmap,new Rect(x,y,w,h));
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);

        if(mBottomImageView != null && mBtmSrcBitmap != null){
            mBottomImageView.setImageBitmap(mBtmSrcBitmap);
        }
        if(mTopImageView != null && mTopSrcBitmap != null){
            mTopImageView.setImageBitmap(mTopSrcBitmap);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if((requestCode==BOTTOM_SELCLEC_IMAGE_REQUEST || requestCode==TOP_SELCLEC_IMAGE_REQUEST)
                &&resultCode==RESULT_OK&&data.getData()!=null){
            Log.i("rx","getDatasuccess");

            Cursor cursor=getContentResolver().query(data.getData(),null,null,null,null);
            cursor.moveToFirst();
            int idx=cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            String filePath=cursor.getString(idx);
            Log.i("rx","Getphotopath:"+filePath);

            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inJustDecodeBounds=true;

            options.inSampleSize=Math.max(1,(int)Math.ceil(Math.max(
                    (double)options.outWidth/1024f,
                    (double)options.outHeight/1024f)));
            options.inJustDecodeBounds=false;

            if(requestCode==BOTTOM_SELCLEC_IMAGE_REQUEST) {
                mBtmSrcBitmap = BitmapFactory.decodeFile(filePath, options);
                if(mBtmSrcBitmap.getHeight() < mBtmSrcBitmap.getWidth()) {
                    mBtmSrcBitmap = rotateBitmap(mBtmSrcBitmap);
                    Log.i("rx","Bottom bitrate (w,h) ->(" + mBtmSrcBitmap.getWidth() +","+mBtmSrcBitmap.getHeight()+")");
                    isBottomImageRotate = true;
                }else{
                    isBottomImageRotate = false;
                }
            }
            else {
                mTopSrcBitmap = BitmapFactory.decodeFile(filePath, options);
                if(isBottomImageRotate) {
                    mTopSrcBitmap = rotateBitmap(mTopSrcBitmap);
                }
            }

        }else{
            Log.i("rx","getDatafalse");
        }

    }

    private Bitmap rotateBitmap(Bitmap srcBitmap){
        Matrix matrix = new Matrix();
        matrix.setRotate(90);
        Bitmap tempBitmap = Bitmap.createBitmap(srcBitmap,0,0,srcBitmap.getWidth(),srcBitmap.getHeight(),matrix,true);
        srcBitmap.recycle();

        return tempBitmap;
    }

    private void saveImage(Bitmap topBitmap, Bitmap bottomBitmap, Rect rect){
        Mat topMat = new Mat();
        Mat bottomMat = new Mat();
        Mat recMat = new Mat();

        Utils.bitmapToMat(topBitmap,topMat);
        Utils.bitmapToMat(bottomBitmap,bottomMat);

        Log.i("rx","bottomMat col="+bottomMat.cols()+", row="+bottomMat.rows());

        recMat=bottomMat.submat(rect);

        float alph = (float) (alphaVal/255);
        Log.i("rx","calculate alpha val"+alph);
        Core.addWeighted(recMat, alph, topMat, (1-alph), 0., recMat);

        //直接copy
//        recMat = bottomMat.submat(rect);
//        topMat.copyTo(recMat);



        Bitmap saveBitmap = Bitmap.createBitmap(bottomBitmap.getWidth(),bottomBitmap.getHeight(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(bottomMat,saveBitmap);
//        mBottomImageView.setImageBitmap(saveBitmap);
        java.text.SimpleDateFormat s = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String filename = s.format(new Date()).toString();
        filename = filename.replace(":","");
        filename = filename.replace("-","");
        filename = filename.replace(" ","_");
        filename +=".png";
        Log.i("rx","create file name:"+filename);
        saveBitmap(saveBitmap,filename);
    }

    public boolean saveBitmap(Bitmap bm,String fileName) {

        boolean ret = false;
        Log.i("rx", "保存图片");
        File f = new File(Environment.getExternalStorageDirectory(),"DCIM/rx_test/"+ fileName);
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            ret = true;
            Log.i("rx", "已经保存"+Environment.getExternalStorageDirectory().toString()+"/DCIM/rx_test/"+ fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;

    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        alphaVal = progress;
        mTopImageView.setAlpha(0xff-progress);
        Log.i("rx","set alpha "+progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        Log.i("rx","状态栏高度"+result);
        return result;
    }
}
