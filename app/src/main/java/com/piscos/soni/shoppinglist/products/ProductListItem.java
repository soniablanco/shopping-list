package com.piscos.soni.shoppinglist.products;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.piscos.soni.shoppinglist.FetchProductPhotoListener;
import com.piscos.soni.shoppinglist.ProductPhotoReadyListener;
import com.piscos.soni.shoppinglist.Utilities;

import java.io.File;
import java.io.FileOutputStream;

public class ProductListItem {
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
        this.setThumbnailPath();
    }

    public ProductListItem(){

    }

    public ProductListItem(String name, String code, String photoUrl){
        this.mName = name;
        this.setCode(code);
        if(photoUrl != null && !photoUrl.isEmpty()) {
        this.mPhotoUrl = photoUrl;
        }
        else{
            this.mPhotoUrl =null;
        }
    }

    public Bitmap mPhoto;
    private String mThumbnailPath;
    public String getThumbnailPath() {
        return mThumbnailPath;
    }

    private void setThumbnailPath() {
        this.mThumbnailPath = THUMBNAILS_FOLDER + "thumbnail_"+this.getCode()+".bmp";
    }

    public String mPhotoUrl;

    public void downloadPhoto(Context context, final PhotosSynchronizationListener listener){
        final String thumbnail = getThumbnailAbsPath(context);
        FBProductsRepository productsData = new FBProductsRepository();
        productsData.downloadPhoto(this.mPhotoUrl,this.getCode(), new PhotoDownloadListener() {
            @Override
            public void onSuccess(String productCode, Bitmap productPhoto) {
                try {
                    File file =new File(thumbnail);
                    FileOutputStream out = new FileOutputStream(file);
                    productPhoto.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    out.flush();
                    out.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                listener.onReady();
            }
        });
    }

    private static String THUMBNAILS_FOLDER="thumbnails/";

    private String getThumbnailAbsPath(Context context){

        final String internalStoragePath = context.getFilesDir()+"/";
        //String attachmentsFolderRelPath="thumbnails/";
        String attachmentsFolderAbsPath=internalStoragePath + THUMBNAILS_FOLDER;
        File attachmentsFolder = new File(attachmentsFolderAbsPath);
        if (!attachmentsFolder.exists()) {
            attachmentsFolder.mkdirs();
        }

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
        if (ProductListItem.this.mPhoto!=null) {
            listener.onReady(ProductListItem.this.mPhoto);
            return;
        }
        Utilities util = new Utilities();
        String imagePath = ProductListItem.this.getThumbnailAbsPath(context);
        util.loadProductPhoto(imagePath, listener);
    }
    /*private synchronized void fetchfoto(final Context context, final PhotoDownloadListener listener){
        if (ProductListItem.this.mPhoto!=null) {
            listener.onSuccess(ProductListItem.this.getCode(), ProductListItem.this.mPhoto);
            return;
        }
        String imagePath = ProductListItem.this.getThumbnailAbsPath(context);
        File imgFile = new  File(imagePath);
        if(imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imagePath);
            ProductListItem.this.mPhoto = myBitmap;
            listener.onSuccess(ProductListItem.this.getCode(), ProductListItem.this.mPhoto);
        }
        else{
            ProductListItem.this.mPhoto = null;
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
        if (ProductListItem.this.mPhoto!=null) {
            listener.onSuccess(ProductListItem.this.getCode(), ProductListItem.this.mPhoto);
            return;
        }
        Utilities util = new Utilities();
        String imagePath = ProductListItem.this.getThumbnailAbsPath(context);
        //util.loadProductPhoto(imagePath,ProductListItem.this.getCode(), listener);
    }
}
