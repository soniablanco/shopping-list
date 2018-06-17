package com.piscos.soni.shoppinglist;

import android.graphics.Bitmap;

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
    }

    public ProductListItem(){

    }

    public ProductListItem(String name, String code, String photoUrl){
        this.mName = name;
        this.mCode = code;
        this.mPhotoUrl = photoUrl;
    }

    public Bitmap mPhoto;

    public String mPhotoUrl;

}
