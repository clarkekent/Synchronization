package com.example.dzm.internet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.IOException;
import java.net.URL;

/**
 * Created by dzm on 6/7/2015.
 */
public class ImageTask extends AsyncTask<String, Integer, Bitmap> {
    public ImageTask(Context aContext,ImageView aView){
        appContext = aContext;
        imageVeiw = aView;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        try {
            Bitmap aBitmap = BitmapFactory.decodeStream(new URL(params[0]).openStream());
            return aBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        imageVeiw.setImageDrawable(new MyBitmapDrawable(appContext, bitmap, null));
    }

    private Context appContext;
    private ImageView imageVeiw;
}
