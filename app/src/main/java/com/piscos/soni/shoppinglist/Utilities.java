package com.piscos.soni.shoppinglist;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.piscos.soni.shoppinglist.products.PhotoDownloadListener;

import java.io.File;

public class Utilities {
    private synchronized void fetchProductPhoto(final String path, final String productCode,final PhotoDownloadListener listener){
         File imgFile = new  File(path);
        if(imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(path);
            listener.onSuccess(productCode, myBitmap);
        }
        else{
            listener.onSuccess(productCode, null);
        }

    }

    public void loadProductPhoto(final String path, final String productCode,final PhotoDownloadListener listener){
        new Thread() {
            public void run() {

                fetchProductPhoto(path,productCode,listener);
            }
        }
                .start();
    }
}
