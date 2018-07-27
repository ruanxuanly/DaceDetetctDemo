package com.example.ruanxuan.OpenCVDemo.GlobalFunction;

import android.os.Debug;
import android.util.Log;

/**
 * Created by ruanxuan on 2017/5/7.
 */

public class Mutex {
    private boolean syncLock;

    ////////////////////////////////////////////////
    //  Constructor
    ////////////////////////////////////////////////

    public Mutex()
    {
        syncLock = false;
    }

    ////////////////////////////////////////////////
    //  lock
    ////////////////////////////////////////////////

    public synchronized void lock()
    {
        while(syncLock == true) {
            try {
                wait();
            }
            catch (Exception e) {
                Log.e("rx","Lock error");
            };
        }
        syncLock = true;
    }

    public synchronized void unlock()
    {
        syncLock = false;
        notifyAll();
    }
}
