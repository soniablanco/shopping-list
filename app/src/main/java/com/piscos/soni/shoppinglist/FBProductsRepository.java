package com.piscos.soni.shoppinglist;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class FBProductsRepository {
    private DatabaseReference productsDB;
    private static final String NODE = "allproducts";
    private static final String FIREBASE_BUCKET = "gs://shopping-list-123.appspot.com/";

    public FBProductsRepository(){
        productsDB = FirebaseDatabase.getInstance().getReference();
        productsDB = productsDB.child(NODE);
    }



    public  void  fetchProducts(final ProductsDownloadedListener listener){
        final List<ProductListItem> productList = new ArrayList<>();
        productsDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    FBProduct product = ds.getValue(FBProduct.class);
                    productList.add(new ProductListItem(product.name, product.code, product.thumbnailUrl));
                }
                listener.onReady(productList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public static Uri getPhotoURI(Context context, ProductListItem product){

        final String internalStoragePath = context.getFilesDir()+"/";
        String attachmentsFolderRelPath="pictures/";
        String attachmentsFolderAbsPath=internalStoragePath+attachmentsFolderRelPath;
        File attachmentsFolder = new File(attachmentsFolderAbsPath);
        if (!attachmentsFolder.exists()) {
            attachmentsFolder.mkdirs();
        }
        String uniqueMediaFolderRelPath="pictures/"+product.getCode()+".bmp";

        String targetFileAbsPath = context.getFilesDir()+"/"+uniqueMediaFolderRelPath;
        File file = new File(targetFileAbsPath);
        Uri uri = FileProvider.getUriForFile(context, "com.piscos.soni.shoppinglist", file);
        return uri;
    }

    public void uploadPhoto(final Context context,Uri fileUri, final String fileName,final PhotoUploadListener listener) {

        StorageReference storageRef = FirebaseStorage.getInstance(FIREBASE_BUCKET).getReference();
        StorageReference photoRemoteRef = storageRef.child(fileName + "/" + fileName +".bmp");
        UploadTask uploadTask = photoRemoteRef.putFile(fileUri);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                int j=3;
                j=j+1;
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                updateProductPhotoInfo(fileName);
                listener.onReady(context);
            }
        });
    }

    private void updateProductPhotoInfo(String fileName){
        String url = fileName+"/"+fileName +".bmp";
        String thumbnailUrl =  fileName+"/thumbnail_"+fileName + ".bmp";

        DatabaseReference urlDR = FirebaseDatabase.getInstance().getReference(NODE+"/"+ fileName + "/photoUrl");
        urlDR.setValue(url);

        DatabaseReference thumbUrlDR = FirebaseDatabase.getInstance().getReference(NODE+"/"+ fileName + "/thumbnailUrl");
        thumbUrlDR.setValue(thumbnailUrl);

        DatabaseReference tsDR = FirebaseDatabase.getInstance().getReference(NODE+"/"+ fileName + "/photoTimeStamp");
        Date ts = new Date();
        tsDR.setValue(ts.getTime());
    }

    public void downloadPhoto(final String photoUrl,final String fileName, final PhotoDownloadListener listener){
        if(photoUrl != null && photoUrl!= "") {
            FirebaseStorage storage = FirebaseStorage.getInstance(FIREBASE_BUCKET);
            StorageReference storageRef = storage.getReference(photoUrl);

            final long ONE_MEGABYTE = 10240 * 10240;
            storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    listener.onSuccess(fileName,BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
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


}
