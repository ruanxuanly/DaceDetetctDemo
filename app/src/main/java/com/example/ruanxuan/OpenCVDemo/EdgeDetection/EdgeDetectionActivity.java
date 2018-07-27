package com.example.ruanxuan.OpenCVDemo.EdgeDetection;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.media.Image;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewDebug;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ruanxuan.OpenCVDemo.GlobalFunction.Mutex;
import com.example.ruanxuan.OpenCVDemo.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class EdgeDetectionActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener,View.OnClickListener{


    private BaseLoaderCallback baseLoadCB = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if(status == LoaderCallbackInterface.SUCCESS){
                Log.i("rx","Load opencv success:EdgeDetectionActivity");
            }else {
                Log.i("rx","Load opencv Fail:EdgeDetectionActivity");
                super.onManagerConnected(status);
            }
        }
    };

    private Button cannyBtn;
    private Button sobelBtn;
    private Button laplaceBtn;
    private Button scharrBtn;
    private Button srcBtn;
    private SeekBar thresholdSeekbar;
    private TextView thresholdValView;
    private SurfaceView imageShow = null;
    private Button selectBtn;
    private static final int PHOTO_PICK_REQUEST = 879;
    public static final int EDGE_DETECTION_DONE = 344;
    private static final int SRC_IMAGE_SEL_DONE = 214;
    Bitmap mSrcBitmap = null;
    Bitmap mdestBitmap = null;
    private int thresholdVal = 0;
    private CannyEdgeDetection mCanny;
    private SurfaceHolder mImageHoler = null;
    private EdgeHandle mHandle;

    private int mProcessNum = 0;
    private Mutex mutex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_edge_detection);
        setContentView(R.layout.activity_edge_detection);

        cannyBtn = (Button)findViewById(R.id.cannyBtn);
        cannyBtn.setOnClickListener(this);
        cannyBtn.isSelected();
        sobelBtn = (Button)findViewById(R.id.sobelBtn);
        sobelBtn.setOnClickListener(this);
        laplaceBtn = (Button)findViewById(R.id.laplaceBtn);
        laplaceBtn.setOnClickListener(this);
        scharrBtn = (Button)findViewById(R.id.scharrBtn);
        scharrBtn.setOnClickListener(this);
        srcBtn = (Button)findViewById(R.id.srcBtn);
        srcBtn.setOnClickListener(this);
        thresholdSeekbar = (SeekBar)findViewById(R.id.thresholdSeekbar);
        thresholdSeekbar.setOnSeekBarChangeListener(this);
        thresholdValView = (TextView)findViewById(R.id.valText);
        imageShow = (SurfaceView) findViewById(R.id.imageShow);
        imageShow.setOnClickListener(this);

        mImageHoler = imageShow.getHolder();
        mImageHoler.addCallback(new ImageShowCB());

        selectBtn = srcBtn;

        mHandle = new EdgeHandle();
        thresholdSeekbar.setEnabled(false);

//        mdestBitmap = new Bitmap();

    }

    private void setButtonBackground(Button v) {
        selectBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        v.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        selectBtn = v;
        if(v.getId() == R.id.srcBtn){
            mHandle.obtainMessage(SRC_IMAGE_SEL_DONE).sendToTarget();
        }else {
            mHandle.obtainMessage(EDGE_DETECTION_DONE).sendToTarget();
        }
    }


    @Override
    public void onClick(View v) {

        if(mSrcBitmap == null && v.getId()!= R.id.imageShow)
            return;
        switch (v.getId()){
            case R.id.cannyBtn:
                setButtonBackground((Button) v);
                break;
            case R.id.sobelBtn:
                setButtonBackground((Button) v);
                break;
            case R.id.laplaceBtn:
                setButtonBackground((Button) v);
                break;
            case R.id.scharrBtn:
                setButtonBackground((Button) v);
                break;
            case R.id.srcBtn:
                setButtonBackground((Button) v);
                mHandle.obtainMessage(SRC_IMAGE_SEL_DONE).sendToTarget();
                break;
            case R.id.imageShow:
                Intent photoPick = new Intent(Intent.ACTION_PICK);
                photoPick.setType("image/*");
                startActivityForResult(photoPick,PHOTO_PICK_REQUEST);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PHOTO_PICK_REQUEST && resultCode == RESULT_OK && data != null){
            Cursor cursor = getContentResolver().query(data.getData(), null, null, null,null);
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            String path = cursor.getString(index);
            Log.i("rx","Select file :"+path);

            if(mSrcBitmap != null){
                mSrcBitmap.recycle();
            }

            if(mdestBitmap != null){
                mdestBitmap.recycle();
            }

            mSrcBitmap = BitmapFactory.decodeFile(path);
            mdestBitmap = BitmapFactory.decodeFile(path);
            mCanny = new CannyEdgeDetection(mSrcBitmap,mdestBitmap,mHandle);
            mCanny.setThreshold(thresholdVal);
            thresholdSeekbar.setEnabled(true);

            mHandle.obtainMessage(SRC_IMAGE_SEL_DONE).sendToTarget();


        }
    }

    class ImageShowCB implements SurfaceHolder.Callback{

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.i("rx","SurfaceView create");
            Canvas canvas = holder.lockCanvas();
            Bitmap bitmap = BitmapFactory.decodeResource(EdgeDetectionActivity.this.getResources(),R.mipmap.star);
            canvas.drawBitmap(bitmap,0,0,null);
            holder.unlockCanvasAndPost(canvas);
            holder.lockCanvas(new Rect(0,0,0,0));
            holder.unlockCanvasAndPost(canvas);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        thresholdVal = progress;
        if(mdestBitmap == null)
            return;
        thresholdValView.setText(new String(String.valueOf(progress)));
        mCanny.setThreshold(progress);

    }

   public class EdgeHandle extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case SRC_IMAGE_SEL_DONE:
                    Log.i("rx","get msg:SRC_IMAGE_SEL_DONE\nstart paint src bitmap");
                    Canvas canvas = mImageHoler.lockCanvas();
                    canvas.drawBitmap(mSrcBitmap,0,0,null);
                    mImageHoler.unlockCanvasAndPost(canvas);
                    break;
                case EDGE_DETECTION_DONE:
                    Log.i("rx","get msg:EDGE_DETECTION_DONE");
                    if(selectBtn.getId()!=R.id.srcBtn){
                        Log.i("rx","Canvas dest bitmap");
                        Canvas destCavas = mImageHoler.lockCanvas();
                        destCavas.drawBitmap(mdestBitmap,0,0,null);
                        mImageHoler.unlockCanvasAndPost(destCavas);
                        mImageHoler.lockCanvas(new Rect(0,0,0,0));
                        mImageHoler.unlockCanvasAndPost(destCavas);
                    }
                    break;
            }

        }
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
