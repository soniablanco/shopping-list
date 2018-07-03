package com.piscos.soni.shoppinglist;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
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

import java.util.List;

import me.himanshusoni.quantityview.QuantityView;

public class NewShoppingListActivity extends AppCompatActivity {

    private RecyclerView mProductsRecyclerView;
    private NewShoppingListActivity.ShoppingListAdapter mAdapter;
    final ProductsManager pm = new ProductsManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_shopping_list);

        mProductsRecyclerView = (RecyclerView) findViewById(R.id.rv_new_shopping_list);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mProductsRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mProductsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        updateUI();
    }

    public void updateUI() {

        List<ShoppingListItem> products = pm.GetProducts2();
        mAdapter = new ShoppingListAdapter(products);
        mProductsRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    private class ShoppingListHolder extends RecyclerView.ViewHolder{
        public TextView mNameTextView;
        public ImageView mPhotoView;
        public me.himanshusoni.quantityview.QuantityView mQuantity;
        private ShoppingListItem mModel;
        //public ConstraintLayout mContainer;

        public ShoppingListHolder(final View itemView){
            super(itemView);
            mNameTextView = (TextView)itemView.findViewById(R.id.tvSLProductName);
            mPhotoView = (ImageView)itemView.findViewById(R.id.imSLPhotoView);
            mQuantity = (me.himanshusoni.quantityview.QuantityView)itemView.findViewById(R.id.quantityView_default);
            mQuantity.setOnQuantityChangeListener(new QuantityView.OnQuantityChangeListener() {
                @Override
                public void onQuantityChanged(int oldQuantity, int newQuantity, boolean programmatically) {
                    mModel.mQuantity = newQuantity;
                    if(newQuantity > 0){
                        mModel.mItemColor = "#7986CB";
                    }
                    else{
                        mModel.mItemColor = "#FFFFFF";
                    }
                    itemView.setBackgroundColor(Color.parseColor(mModel.mItemColor));
                }

                @Override
                public void onLimitReached() {

                }
            });
        }

    }


    private class ShoppingListAdapter extends RecyclerView.Adapter<NewShoppingListActivity.ShoppingListHolder>{

        private List<ShoppingListItem> mShoppingList;

        public ShoppingListAdapter(List<ShoppingListItem> products){
            mShoppingList = products;
        }

        @Override
        public NewShoppingListActivity.ShoppingListHolder onCreateViewHolder(ViewGroup parent, int viewType){
            LayoutInflater layoutInflater = LayoutInflater.from(getBaseContext());
            View view = layoutInflater.inflate(R.layout.shopping_list_item,parent,false);
            //View view = layoutInflater.inflate(android.R.layout.simple_list_item_1,parent,false);
            return new NewShoppingListActivity.ShoppingListHolder(view);
        }

        @Override
        public void onBindViewHolder(final NewShoppingListActivity.ShoppingListHolder holder, int position){
            final ShoppingListItem item = mShoppingList.get(position);

            holder.mModel = item;
            holder.mNameTextView.setText(item.getName());
            holder.mQuantity.setQuantity(item.mQuantity);
            holder.itemView.setBackgroundColor(Color.parseColor(item.mItemColor));

            if (item.mPhoto!=null){
                holder.mPhotoView.setImageBitmap(item.mPhoto);
            }
            else{
                holder.mPhotoView.setImageBitmap(null);
                item.fetchPhoto(NewShoppingListActivity.this, new PhotoDownloadListener() {
                    @Override
                    public void onSuccess(final String productCode, final Bitmap productPhoto) {
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
            return mShoppingList.size();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.new_shopping_list, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_item_save_new_shopping_list:
                Toast toast = Toast.makeText(getApplicationContext(), "Ready to save", Toast.LENGTH_LONG);
                toast.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
