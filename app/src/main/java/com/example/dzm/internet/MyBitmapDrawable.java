package com.example.dzm.internet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import java.lang.ref.WeakReference;

/**
 * Created by dzm on 6/8/2015.
 */
public class MyBitmapDrawable extends BitmapDrawable {
    public MyBitmapDrawable(Context context, Bitmap bitmap, ImageTask task){
        super(context.getResources(), bitmap);
        if(task!=null){
            iTask = new WeakReference<ImageTask>(task);
        }
    }

    public void setTask(ImageTask imageTask){
        if(imageTask==null){
            iTask = null;
        }else{
            iTask = new WeakReference<ImageTask>(imageTask);
        }
    }

    public ImageTask getTask(){
        if(iTask==null){
            return null;
        }else{
            return iTask.get();
        }
    }

    private WeakReference<ImageTask> iTask = null;
}
