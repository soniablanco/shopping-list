package com.piscos.soni.shoppinglist;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class AllItemsActivity extends AppCompatActivity {

    private RecyclerView mProductsRecyclerView;
    private ProductListAdapter mAdapter;

    // Define the products Firebase DatabaseReference
    //private DatabaseReference productsDB;

    private ProductsData productsData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_items);

        mProductsRecyclerView = (RecyclerView) findViewById(R.id.rv_product_list);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mProductsRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mProductsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        /*productsDB = FirebaseDatabase.getInstance().getReference();
        productsDB = productsDB.child("allproducts");*/

        productsData = new ProductsData();

        updateUI();

        //HockeyApp
        checkForUpdates();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkForCrashes();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterManagers();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterManagers();
    }

    public void updateUI() {

        productsData.FetchProducts(new OnAllProductsDownloadedListener() {
            @Override
            public void onReady(List<ProductListItem> products) {
                mAdapter = new ProductListAdapter(products);
                mProductsRecyclerView.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
            }
        });


       /* productsDB.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                // Get the value from the DataSnapshot and add it to the products' list
                Product product = dataSnapshot.getValue(Product.class);
                ProductListItem productItem = new ProductListItem(product.name,product.code,product.thumbnailUrl);
                productList.add(productItem);

                // Notify the ArrayAdapter that there was a change
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                int o =1;
                o++;
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/
    }

    public Uri getPhotoURI(ProductListItem product){

        final String internalStoragePath = this.getFilesDir()+"/";
        String attachmentsFolderRelPath="pictures/";
        String attachmentsFolderAbsPath=internalStoragePath+attachmentsFolderRelPath;
        File attachmentsFolder = new File(attachmentsFolderAbsPath);
        if (!attachmentsFolder.exists()) {
            attachmentsFolder.mkdirs();
        }
        String uniqueMediaFolderRelPath="pictures/"+product.getCode()+".bmp";

        String targetFileAbsPath = this.getFilesDir()+"/"+uniqueMediaFolderRelPath;
        File file = new File(targetFileAbsPath);
        Uri uri = FileProvider.getUriForFile(this, "com.piscos.soni.shoppinglist", file);
        return uri;
    }
    public static final int PHOTO_CAPTURE = 102;
    private Uri freshlyUploadedPhotoPath;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_CAPTURE) {
            CharSequence text;

            if (resultCode == RESULT_OK) {
                text = "OK";
                Uri photoUri = freshlyUploadedPhotoPath;
                // start cropping activity for pre-acquired image saved on the device
                CropImage.activity(photoUri).setOutputUri(photoUri).setAspectRatio(10,10).setFixAspectRatio(true)
                        .start(AllItemsActivity.this);
            }
            else{
                text = "Oh NO";
            }
            Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
            toast.show();
        }
        else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            Toast toast = Toast.makeText(getApplicationContext(), "Cropping", Toast.LENGTH_SHORT);
            toast.show();
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                uploadPhoto(freshlyUploadedPhotoPath);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public static final String FIREBASE_BUCKET = "gs://shopping-list-123.appspot.com/";
    public void uploadPhoto(Uri fileUri) {
        List<String> path = fileUri.getPathSegments();
        final String name = path.get(path.size()-1);

        String[] f = name.split("\\.");
        List<String> itemList = new ArrayList<String>(Arrays.asList(f));
        final String folder = itemList.get(0);

        StorageReference storageRef = FirebaseStorage.getInstance(FIREBASE_BUCKET).getReference();
        StorageReference photoRemoteRef = storageRef.child(folder+"/"+name);
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

                String url = folder+"/"+name;
                String thumbnailUrl =  folder+"/thumbnail_"+name;

                DatabaseReference urlDR = FirebaseDatabase.getInstance().getReference("allproducts"+"/"+ folder + "/photoUrl");
                urlDR.setValue(url);

                DatabaseReference thumbUrlDR = FirebaseDatabase.getInstance().getReference("allproducts"+"/"+ folder + "/thumbnailUrl");
                thumbUrlDR.setValue(thumbnailUrl);

                DatabaseReference tsDR = FirebaseDatabase.getInstance().getReference("allproducts"+"/"+ folder + "/photoTimeStamp");
                Date ts = new Date();
                tsDR.setValue(ts.getTime());

                Toast toast = Toast.makeText(getApplicationContext(), "Yahoo", Toast.LENGTH_SHORT);
                toast.show();
                AllItemsActivity.this.updateUI();
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
            }
        });
    }

    private class ProductHolder extends RecyclerView.ViewHolder{
        public TextView mNameTextView;
        public ImageView mPhotoView;
        private ProductListItem mModel;
        public ProductHolder(View itemView){
            super(itemView);
            mNameTextView = (TextView)itemView.findViewById(R.id.tvProductName);
            mPhotoView = (ImageView)itemView.findViewById(R.id.imPhotoView);


            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    freshlyUploadedPhotoPath = getPhotoURI(mModel);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT,freshlyUploadedPhotoPath );
                    AllItemsActivity.this.startActivityForResult(intent, PHOTO_CAPTURE);

                    return  true;
                }
            });
        }

    }


    private class ProductListAdapter extends RecyclerView.Adapter<ProductHolder>{

        private List<ProductListItem> mProductList;

        public ProductListAdapter(List<ProductListItem> products){
            mProductList = products;
        }

        @Override
        public ProductHolder onCreateViewHolder(ViewGroup parent,int viewType){
            LayoutInflater layoutInflater = LayoutInflater.from(getBaseContext());
            View view = layoutInflater.inflate(R.layout.product_list_item,parent,false);
            //View view = layoutInflater.inflate(android.R.layout.simple_list_item_1,parent,false);
            return new ProductHolder(view);
        }

        @Override
        public void onBindViewHolder(final ProductHolder holder,int position){
            final ProductListItem item = mProductList.get(position);

            holder.mModel = item;
            holder.mNameTextView.setText(item.getName());
            final com.piscos.soni.shoppinglist.DownloadListener downloadListener=new com.piscos.soni.shoppinglist.DownloadListener(){

                @Override
                public void onSuccess(ProductListItem productListItem) {
                    if (productListItem==item) {
                        if (item.mPhoto != null) {
                            holder.mPhotoView.setImageBitmap(item.mPhoto);
                        }
                    }
                }
            };
            holder.mModel.mListener=downloadListener;
            if (item.mPhoto!=null){
                holder.mPhotoView.setImageBitmap(item.mPhoto);
            }
            else{
                holder.mPhotoView.setImageBitmap(null);
                item.Download();
            }
        }
        @Override
        public int getItemCount(){
            return mProductList.size();
        }
    }

    private void checkForCrashes() {
        CrashManager.register(this);
    }

    private void checkForUpdates() {
        // Remove this for store builds!
        UpdateManager.register(this);
    }

    private void unregisterManagers() {
        UpdateManager.unregister();
    }
}