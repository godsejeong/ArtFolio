package com.didimstory.artfolioapplication.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class BitmapGender extends Thread {
    Bitmap myBitmap;
    String address;

    public BitmapGender(String url) {
        this.address = url;
    }

    @Override
    public void run() {
        super.run();
        try {
            URL url = new URL(address);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            myBitmap = BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Bitmap ReturnBitmap(){
        return myBitmap;
    }

}
