package com.piscos.soni.shoppinglist;

import android.graphics.Bitmap;

public interface PhotoDownloadListener {
    void onSuccess(String productCode, Bitmap productPhoto);
}
