package com.piscos.soni.shoppinglist.buyerlist;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.piscos.soni.shoppinglist.FetchProductPhotoListener;
import com.piscos.soni.shoppinglist.ProductPhotoReadyListener;
import com.piscos.soni.shoppinglist.Utilities;
import com.piscos.soni.shoppinglist.products.PhotoDownloadListener;

import java.io.File;

public class BuyerShoppingListItem{

    public static final String COLLECTED_COLOUR = "#7986CB";
    public static final String UNCOLLECTED_COLOUR = "#FFFFFF";
    public int mQuantity;


    private String mName;

    public BuyerShoppingListItem(String name, String code, String thumbnailPath, int quantity, boolean wasCollected) {
        mQuantity = quantity;
        mName = name;
        mCode = code;
        mThumbnailPath = thumbnailPath;
        setWasCollected(wasCollected);
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    private String mCode;

    public String getCode() {
        return mCode;
    }

    public void setCode(String mCode) {
        this.mCode = mCode;
    }

    public Bitmap mPhoto;

    public String mThumbnailPath;

    public void setWasCollected(boolean wasCollected) {
        mWasCollected = wasCollected;
        if(wasCollected){
            mColour = COLLECTED_COLOUR;
        }
        else{
            mColour = UNCOLLECTED_COLOUR;
        }
    }

    public boolean wasCollected() {
        return mWasCollected;
    }

    private boolean mWasCollected;

    public String getColour() {
        return mColour;
    }

    private String mColour;

    private String getThumbnailAbsPath(Context context){
        return context.getFilesDir()+"/"+this.mThumbnailPath;
    }

    public void loadPhoto(Context context, final ProductPhotoReadyListener viewListener){
        FetchProductPhotoListener listener = new FetchProductPhotoListener() {
            @Override
            public void onReady(Bitmap photo) {
                mPhoto = photo;
                viewListener.onReady();
            }
        };
        if (BuyerShoppingListItem.this.mPhoto!=null) {
            listener.onReady(BuyerShoppingListItem.this.mPhoto);
            return;
        }
        Utilities util = new Utilities();
        String imagePath = BuyerShoppingListItem.this.getThumbnailAbsPath(context);
        util.loadProductPhoto(imagePath, listener);
    }
    /*private synchronized void fetchfoto(final Context context, final PhotoDownloadListener listener){
        if (BuyerShoppingListItem.this.mPhoto!=null) {
            listener.onSuccess(BuyerShoppingListItem.this.getCode(), BuyerShoppingListItem.this.mPhoto);
            return;
        }
        String imagePath = BuyerShoppingListItem.this.getThumbnailAbsPath(context);
        File imgFile = new  File(imagePath);
        if(imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imagePath);
            BuyerShoppingListItem.this.mPhoto = myBitmap;
            listener.onSuccess(BuyerShoppingListItem.this.getCode(), BuyerShoppingListItem.this.mPhoto);
        }
        else{
            BuyerShoppingListItem.this.mPhoto = null;
        }

    }

    public void fetchPhoto(final Context context,final PhotoDownloadListener listener){
        new Thread() {
            public void run() {

                fetchfoto(context,listener);
            }
        }
                .start();
    }*/

    public void fetchPhoto(final Context context,final PhotoDownloadListener listener){
        if (BuyerShoppingListItem.this.mPhoto!=null) {
            listener.onSuccess(BuyerShoppingListItem.this.mCode, BuyerShoppingListItem.this.mPhoto);
            return;
        }
        Utilities util = new Utilities();
        String imagePath = BuyerShoppingListItem.this.getThumbnailAbsPath(context);
        //util.loadProductPhoto(imagePath,BuyerShoppingListItem.this.mCode, listener);
    }
}
