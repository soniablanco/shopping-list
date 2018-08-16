package com.piscos.soni.shoppinglist;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.piscos.soni.shoppinglist.products.PhotoDownloadListener;

import java.io.File;

public class Utilities {
    private synchronized void fetchProductPhoto(final String path, final FetchProductPhotoListener listener){
         File imgFile = new  File(path);
        if(imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(path);
            listener.onReady(myBitmap);
        }
        else{
            listener.onReady(null);
        }

    }

    public void loadProductPhoto(final String path,final FetchProductPhotoListener listener){
        new Thread() {
            public void run() {

                fetchProductPhoto(path,listener);
            }
        }
                .start();
    }
}
