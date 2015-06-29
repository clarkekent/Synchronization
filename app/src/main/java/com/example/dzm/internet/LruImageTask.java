package com.example.dzm.internet;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;
import android.widget.ImageView;

/**
 * Created by dzm on 6/13/2015.
 */
public class LruImageTask extends ImageTask {
    public LruImageTask(Context aContext, ImageView aView, LruCache<Integer, Bitmap> cache, int index) {
        super(aContext, aView);
        iconCache = cache;
        position = index;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        Bitmap bitmap = super.doInBackground(params);
        if(bitmap!=null){
            iconCache.put(position, bitmap);
        }

        return bitmap;
    }

    private LruCache<Integer, Bitmap> iconCache;
    private int position;
}
