package com.piscos.soni.shoppinglist.shoppinglist;

import android.content.Context;
import android.graphics.Bitmap;

import com.piscos.soni.shoppinglist.FetchProductPhotoListener;
import com.piscos.soni.shoppinglist.Utilities;


public class ShoppingListItem {

    public static final String SELECTED_COLOUR = "#7986CB";
    public static final String UNSELECTED_COLOUR = "#FFFFFF";

    private String mName;

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
        //this.setThumbnailPath();
    }

    private int mQuantity;

    public void setQuantity(int quantity) {
        mQuantity = quantity;
        if(quantity >0){
            mItemColor = SELECTED_COLOUR;
        }
        else{
            mItemColor = UNSELECTED_COLOUR;
        }
        mTimestamp = System.currentTimeMillis()/1000;
        mUpdated = true;
    }

    public int getQuantity() {
        return mQuantity;
    }

    private String mItemColor;

    public String getItemColor() {
        return mItemColor;
    }

    private Long mTimestamp;

    public Long getTimestamp() {
        return mTimestamp;
    }

    public ShoppingListItem(){
        setQuantity(0);

    }

    public boolean mUpdated;

    public boolean mWasCollected;

    public Bitmap mPhoto;

    private String mThumbnailPath;


    public ShoppingListItem(String name, String code,String thumbnailPath){
        setName(name);
        setCode(code);
        setQuantity(0);
        mUpdated = false;
        mWasCollected = false;
        mThumbnailPath = thumbnailPath;
    }

    public ShoppingListItem(String name, String code,String thumbnailPath, int quantity,Long timestamp,boolean updated,boolean wasCollected){
        setName(name);
        setCode(code);
        setQuantity(quantity);
        mTimestamp = timestamp;
        mUpdated = updated;
        mWasCollected = wasCollected;
        mThumbnailPath = thumbnailPath;
    }


    private String getThumbnailAbsPath(Context context){
        return context.getFilesDir()+"/"+this.mThumbnailPath;
    }

    public void loadPhoto(Context context, final ShoppingListItemPhotoReadyListener viewListener){
        FetchProductPhotoListener listener = new FetchProductPhotoListener() {
            @Override
            public void onReady(Bitmap photo) {
                mPhoto = photo;
                viewListener.onReady(ShoppingListItem.this);
            }
        };
        if (ShoppingListItem.this.mPhoto!=null) {
            listener.onReady(ShoppingListItem.this.mPhoto);
            return;
        }
        Utilities util = new Utilities();
        String imagePath = ShoppingListItem.this.getThumbnailAbsPath(context);
        util.loadProductPhoto(imagePath, listener);
    }

    /*private synchronized void fetchfoto(final Context context, final PhotoDownloadListener listener){
        if (ShoppingListItem.this.mPhoto!=null) {
            listener.onSuccess(ShoppingListItem.this.getCode(), ShoppingListItem.this.mPhoto);
            return;
        }
        String imagePath = ShoppingListItem.this.getThumbnailAbsPath(context);
        File imgFile = new  File(imagePath);
        if(imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imagePath);
            ShoppingListItem.this.mPhoto = myBitmap;
            listener.onSuccess(ShoppingListItem.this.getCode(), ShoppingListItem.this.mPhoto);
        }
        else{
            ShoppingListItem.this.mPhoto = null;
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

    /*public void fetchPhoto(final Context context,final PhotoDownloadListener listener){
        if (ShoppingListItem.this.mPhoto!=null) {
            listener.onSuccess(ShoppingListItem.this.getCode(), ShoppingListItem.this.mPhoto);
            return;
        }
        Utilities util = new Utilities();
        String imagePath = ShoppingListItem.this.getThumbnailAbsPath(context);
        util.loadProductPhoto(imagePath,ShoppingListItem.this.getCode(), listener);
    }*/
}
