package com.example.corbihome;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;


import java.io.InputStream;

public class CustomImageView extends AsyncTask<String, Void, Bitmap> {
    private ImageView imageView;

    public CustomImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    protected Bitmap doInBackground(String... urls) {
        String url = urls[0];
        Bitmap icon = null;
        try {
            InputStream is = new java.net.URL(url).openStream();
            icon = BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            Log.e("CustomImageView", e.toString());
            return null;
        }
        return icon;
    }

    protected void onPostExecute(Bitmap result) {
        imageView.setImageBitmap(result);
    }
}
