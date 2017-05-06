package com.example.ruanxuan.OpenCVDemo.ImageMixingDemo;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * Created by ruanxuan on 2017/5/1.
 */

public class MoveImageView extends AppCompatImageView{
    private int lastX = 0;
    private int lastY = 0;

    private int limitLeft = 0;
    private int limitRight = 1080;
    private int limitTop = 0;
    private int limitBottom = 1920;

    public MoveImageView(Context context){
        super(context);
    }

    public MoveImageView(Context context, AttributeSet attr)
    {
        super(context,attr,0);
    }

    public MoveImageView(Context context,AttributeSet attr,int defaultSet){
        super(context,attr,defaultSet);
    }

    public void setLimitPosition(int left,int top,int right,int bottom){
        limitLeft = left;
        limitRight = right;
        limitTop = top;
        limitBottom = bottom;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                lastX = (int)event.getRawX();
                lastY = (int)event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                int x = (int)event.getRawX() - lastX;
                int y = (int)event.getRawY() - lastY;
                int left = getLeft() + x;
                int top = getTop() + y;
                int right = getRight() + x;
                int bottom = getBottom() + y;

                if(left < limitLeft){
                    left = limitLeft;
                    right = getWidth();
                }

                if(right > limitRight){
                    right = limitRight;
                    left = limitRight - getWidth();
                }

                if(top < limitTop){
                    top = limitTop;
                    bottom = getHeight();
                }

                if(bottom > limitBottom){
                    bottom = limitBottom;
                    top = limitBottom - getHeight();
                }

                layout(left,top,right,bottom);
                lastX = (int)event.getRawX();
                lastY = (int)event.getRawY();
                break;
            default:
                break;
        }

        return true;
    }
}
