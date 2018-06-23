package com.piscos.soni.shoppinglist;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import java.util.List;

import static com.piscos.soni.shoppinglist.ProductsManager.GetProducts;

public class AllItemsActivity extends AppCompatActivity {

    private RecyclerView mProductsRecyclerView;
    private ProductListAdapter mAdapter;

    private FBProductsRepository productsData;

    public static final int PHOTO_CAPTURE = 102;
    private CameraAccess mCameraAccess;

    final ProductsManager pm = new ProductsManager();

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
        productsData = new FBProductsRepository();
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

       /* productsData.fetchProducts(new ProductsDownloadedListener() {
            @Override
            public void onReady(List<ProductListItem> products) {
                mAdapter = new ProductListAdapter(products);
                mProductsRecyclerView.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
            }
        });*/
        List<ProductListItem> products = pm.GetProducts();
        mAdapter = new ProductListAdapter(products);
        mProductsRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PHOTO_CAPTURE) {
            CharSequence text;

            if (resultCode == RESULT_OK) {
                text = "OK";
                // start cropping activity for pre-acquired image saved on the device
                CropImage.activity(mCameraAccess.mTargetUri).setOutputUri(mCameraAccess.mTargetUri).setAspectRatio(10,10).setFixAspectRatio(true)
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
                productsData.uploadPhoto(AllItemsActivity.this, mCameraAccess.mTargetUri,mCameraAccess.mName, new PhotoUploadListener() {
                    @Override
                    public void onReady(Context context) {
                        AllItemsActivity.this.updateUI();
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
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
                    //freshlyUploadedPhotoPath = FBProductsRepository.getPhotoURI(AllItemsActivity.this,mModel);
                    mCameraAccess = new CameraAccess();
                    mCameraAccess.mTargetUri = FBProductsRepository.getPhotoURI(AllItemsActivity.this,mModel);
                    mCameraAccess.mName = mModel.getCode();
                    intent.putExtra(MediaStore.EXTRA_OUTPUT,mCameraAccess.mTargetUri );
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

            if (item.mPhoto!=null){
                holder.mPhotoView.setImageBitmap(item.mPhoto);
            }
            else{
                holder.mPhotoView.setImageBitmap(null);
                //productsData.downloadPhoto(item.mPhotoUrl,item.getCode(), new PhotoDownloadListener() {
                item.fetchPhoto(AllItemsActivity.this, new PhotoDownloadListener() {
                    @Override
                    public void onSuccess(final String productCode, final  Bitmap productPhoto) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                item.mPhoto = productPhoto;
                                if (productCode == holder.mModel.getCode())
                                {
                                    holder.mPhotoView.setImageBitmap(productPhoto);
                                }
                            }
                        });
                    }
                });
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

    private class CameraAccess{
        public Uri mTargetUri;
        public String mName;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.all_items_list, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_item_sync:
                SyncData(new DataSynchronizationListener() {
                    @Override
                    public void onReady() {
                        Toast toast = Toast.makeText(getApplicationContext(), "Synchronization OK", Toast.LENGTH_LONG);
                        toast.show();
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

   /* private void SyncData(){
        productsData.fetchProducts(new ProductsDownloadedListener() {
            @Override
            public void onReady(List<ProductListItem> products) {
                pm.UpdateDatabase(products);
                for(ProductListItem item:products){
                    item.DownloadPhoto(AllItemsActivity.this);
                }
                Toast toast = Toast.makeText(getApplicationContext(), "Synchronization OK", Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }*/

    private void SyncData(final DataSynchronizationListener listener){//listener 1
        productsData.fetchProducts(new ProductsDownloadedListener() {
            @Override
            public  void onReady(List<ProductListItem> products) {
                pm.UpdateDatabase(products);
                final int[] photosCount = new int[1];
                photosCount[0] = 0;
                for (ProductListItem p:products){
                    if(p.mPhotoUrl != null && p.mPhotoUrl != ""){
                        photosCount[0] = photosCount[0] + 1;
                    }
                }
                final int[] processed = new int[1];
                processed[0] = 0;
                for(ProductListItem item:products){
                    item.DownloadPhoto(AllItemsActivity.this,new PhotosSynchronizationListener(){
                        @Override
                        public synchronized void onReady() {
                            processed[0] = processed[0]+1;
                            if(processed[0] == photosCount[0]){
                                listener.onReady();
                            }
                        }
                    });//=>{cuantashecompletado=cuantashecompletado+1  cuantasfotos=completdo =<> cuandotemrine() listener2
                }
            }
        });
    }
}