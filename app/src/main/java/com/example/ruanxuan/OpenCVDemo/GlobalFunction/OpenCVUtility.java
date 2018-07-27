package com.example.ruanxuan.OpenCVDemo.GlobalFunction;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.ruanxuan.OpenCVDemo.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by ruanxuan on 2017/5/6.
 */

public class OpenCVUtility {
    public final int LOAD_BASE_SUCCESS = 0x01;

    static HashMap<Context,OpenCVUtility> inStance = null;

    private BaseLoaderCallback mbaseLoader = null;

    public OpenCVUtility getInstance(Context context){

        if(inStance == null){
            inStance = new HashMap<>();
        }

        Set<Context> set = inStance.keySet();

        for(Iterator<Context> iter = set.iterator();iter.hasNext();){
            Context key = iter.next();
            if(key.equals(context)){
                return inStance.get(key);
            }
        }

        OpenCVUtility utility = new OpenCVUtility();
        inStance.put(context,utility);

        return utility;
    }

    public OpenCVUtility getInstance(Context context, final Handler handler){

        if(inStance == null){
            inStance = new HashMap<>();
        }

        Set<Context> set = inStance.keySet();

        for(Iterator<Context> iter = set.iterator();iter.hasNext();){
            Context key = iter.next();
            if(key.equals(context)){
                return inStance.get(key);
            }
        }

        OpenCVUtility utility = new OpenCVUtility();
        inStance.put(context,utility);

        mbaseLoader = new BaseLoaderCallback(context) {
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);

                if(status == LoaderCallbackInterface.SUCCESS){
                    handler.obtainMessage(LOAD_BASE_SUCCESS).sendToTarget();
//                if(mCameraView != null){
//                    Log.i("rx","start camera!");
//                    createCascadeClassifier();
//                    mCameraView.enableView();
//                }else{
//                    Log.i("rx","camera view not init ");
//                }
                }
            }
        };

        return utility;
    }


}
