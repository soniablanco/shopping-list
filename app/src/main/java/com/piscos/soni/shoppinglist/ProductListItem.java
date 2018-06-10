package com.piscos.soni.shoppinglist;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProductListItem {
    private String mName;

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    private String mCode;
    public  DownloadListener mListener;

    public String getCode() {
        return mCode;
    }

    public void setCode(String mCode) {
        this.mCode = mCode;
    }

    public void  Download(){
        if(mPhotoUrl != null && mPhotoUrl!= "") {
            FirebaseStorage storage = FirebaseStorage.getInstance("gs://shopping-list-123.appspot.com/");
            StorageReference storageRef = storage.getReference(mPhotoUrl);



            String[] items = mPhotoUrl.split("/");
            List<String> itemList = new ArrayList<String>(Arrays.asList(items));

            String name = itemList.get(itemList.size()-1);
            String[] f = name.split("\\.");
            List<String> itemsList = new ArrayList<String>(Arrays.asList(f));
            String folder = itemsList.get(0);

            //StorageReference islandRef = storageRef.child(folder+"/"+name);

            final long ONE_MEGABYTE = 10240 * 10240;
            storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    mPhoto = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    mListener.onSuccess(ProductListItem.this);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    int j =1;
                    j++;
                }
            });
        }
    }
    public Bitmap mPhoto;

    public String mPhotoUrl;

}
