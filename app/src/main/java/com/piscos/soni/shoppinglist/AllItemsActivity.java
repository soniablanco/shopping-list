package com.piscos.soni.shoppinglist;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AllItemsActivity extends AppCompatActivity {

    private RecyclerView mProductsRecyclerView;
    private ProductListAdapter mAdapter;

    // Define the products Firebase DatabaseReference
    private DatabaseReference productsDB;

    // Define a String ArrayList for the teachers
    //private ArrayList<String> teachersList = new ArrayList<>();

    // Define a ListView to display the data
    //private ListView listViewTeachers;

    // Define an ArrayAdapter for the list
    //private ArrayAdapter<String> arrayAdapter;

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

        productsDB = FirebaseDatabase.getInstance().getReference();
        productsDB = productsDB.child("allproducts");

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

    private void updateUI() {
        //ProductListItemsTestData data = ProductListItemsTestData.get(this);
        final List<ProductListItem> productList = new ArrayList<>();//data.getProductListItems();

        mAdapter = new ProductListAdapter(productList);
        mProductsRecyclerView.setAdapter(mAdapter);

        productsDB.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                // Get the value from the DataSnapshot and add it to the products' list
                Product product = dataSnapshot.getValue(Product.class);
                ProductListItem productItem = new ProductListItem();
                productItem.setName(product.name);
                productItem.setCode(product.code);
                productList.add(productItem);

                // Notify the ArrayAdapter that there was a change
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

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
        });
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_CAPTURE) {
            Context context = getApplicationContext();
            CharSequence text;
            int duration = Toast.LENGTH_SHORT;

            if (resultCode == RESULT_OK) {
                text = "OK";
            }
            else{
                text = "Oh NO";
            }
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
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
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, getPhotoURI(mModel));
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
        public void onBindViewHolder(ProductHolder holder,int position){
            ProductListItem item = mProductList.get(position);
            holder.mModel = item;
            holder.mNameTextView.setText(item.getName());
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
