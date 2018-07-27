package com.example.ruanxuan.OpenCVDemo.EdgeDetection;

import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import com.example.ruanxuan.OpenCVDemo.GlobalFunction.Mutex;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

/**
 * Created by ruanxuan on 2017/5/7.
 */

public class CannyEdgeDetection{
    private Mutex mutex;
    private Mat srcMat = null;
    private Mat destMat = null;
    private Mat edgeMat = null;
    private Mat grarMat = null;
    private Mat blurMat = null;
    private Bitmap mDestBitmap;
    private int mThredhold = 0;
    private Handler mHandler;

    static {
        System.loadLibrary("opencv_java3");
    }

    public CannyEdgeDetection(Bitmap srcBitmap, Bitmap destBitmap, Handler handler){
        mDestBitmap = destBitmap;
        srcMat = new Mat();
        Utils.bitmapToMat(srcBitmap,srcMat);
        blurMat = new Mat(srcMat.size(),srcMat.type(),new Scalar(0));
        grarMat = new Mat();
        Imgproc.cvtColor(srcMat,grarMat,Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur(grarMat,blurMat,new Size(3,3));

        mutex = new Mutex();
        mHandler = handler;
    }

    public void setThreshold(int val){
        mThredhold = val;
        new Thread(new Runnable() {
            @Override
            public void run() {
                destMat = new Mat(srcMat.size(),srcMat.type(),new Scalar(0));
                edgeMat = new Mat();

                mutex.lock();
                Imgproc.Canny(blurMat,edgeMat,mThredhold,2*mThredhold,3,true);
                srcMat.copyTo(destMat,edgeMat);
                Utils.matToBitmap(destMat,mDestBitmap);
                destMat.release();
                edgeMat.release();
                mutex.unlock();

                Log.i("rx","canny done!Send message:EDGE_DETECTION_DONE:"+EdgeDetectionActivity.EDGE_DETECTION_DONE);
                mHandler.obtainMessage(EdgeDetectionActivity.EDGE_DETECTION_DONE).sendToTarget();
            }
        }).start();
    }
}
