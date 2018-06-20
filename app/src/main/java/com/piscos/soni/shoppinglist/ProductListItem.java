package com.piscos.soni.shoppinglist;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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
        this.mPhotoUrl = photoUrl;
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

    public void DownloadPhoto(Context context){
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

    public void fetchPhoto(Context context,PhotoDownloadListener listener){
        String imagePath = this.getThumbnailAbsPath(context);
        File imgFile = new  File(imagePath);
        if(imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imagePath);
            this.mPhoto = myBitmap;
            listener.onSuccess(this.getCode(), this.mPhoto);
        }
        else{
            this.mPhoto = null;
        }

    }
}
