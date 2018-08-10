package com.piscos.soni.shoppinglist.products;

import android.graphics.Bitmap;

public interface PhotoDownloadListener {
    void onSuccess(String productCode, Bitmap productPhoto);
}
