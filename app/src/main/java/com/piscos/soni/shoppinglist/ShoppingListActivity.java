package com.piscos.soni.shoppinglist;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

import me.himanshusoni.quantityview.QuantityView;

public class ShoppingListActivity extends AppCompatActivity {

    private RecyclerView mProductsRecyclerView;
    private ShoppingListActivity.ShoppingListAdapter mAdapter;

    private static final String EXTRA_SHOPPING_LIST_ID = "com.piscos.soni.shoppinglist.shoppingListId";

    private ShoppingList mShoppingList;

    public static Intent newIntent(Context packageContext, int shoppingListId){
        Intent i = new Intent(packageContext,ShoppingListActivity.class);
        if(shoppingListId > 0){
            i.putExtra(EXTRA_SHOPPING_LIST_ID,shoppingListId);
        }
        return i;
    }

    public static Intent newIntent(Context packageContext){
        return newIntent(packageContext,0);
    }

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

        int shoppingListId = getIntent().getIntExtra(EXTRA_SHOPPING_LIST_ID,0);
        if (shoppingListId == 0) {
            mShoppingList = ShoppingList.GetNewShoppingList();//ShoppingList.GetShoppingListById(7);
        }
        else{
            mShoppingList = ShoppingList.GetShoppingListById(shoppingListId);
        }
        updateUI();
    }


    public void updateUI() {
        mAdapter = new ShoppingListAdapter(mShoppingList.Items);
        mProductsRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    private class ShoppingListHolder extends RecyclerView.ViewHolder{
        public TextView mNameTextView;
        public ImageView mPhotoView;
        public me.himanshusoni.quantityview.QuantityView mQuantity;
        private ShoppingListItem mModel;

        public ShoppingListHolder(final View itemView){
            super(itemView);
            mNameTextView = (TextView)itemView.findViewById(R.id.tvSLProductName);
            mPhotoView = (ImageView)itemView.findViewById(R.id.imSLPhotoView);
            mQuantity = (me.himanshusoni.quantityview.QuantityView)itemView.findViewById(R.id.quantityView_default);
            mQuantity.setOnQuantityChangeListener(new QuantityView.OnQuantityChangeListener() {
                @Override
                public void onQuantityChanged(int oldQuantity, int newQuantity, boolean programmatically) {
                    mModel.setQuantity(newQuantity);
                    itemView.setBackgroundColor(Color.parseColor(mModel.getItemColor()));
                    mShoppingList.UpdateItem(mModel);
                }

                @Override
                public void onLimitReached() {

                }
            });
        }

    }


    private class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListActivity.ShoppingListHolder>{

        private List<ShoppingListItem> mShoppingList;

        public ShoppingListAdapter(List<ShoppingListItem> products){
            mShoppingList = products;
        }

        @Override
        public ShoppingListActivity.ShoppingListHolder onCreateViewHolder(ViewGroup parent, int viewType){
            LayoutInflater layoutInflater = LayoutInflater.from(getBaseContext());
            View view = layoutInflater.inflate(R.layout.shopping_list_item,parent,false);
            //View view = layoutInflater.inflate(android.R.layout.simple_list_item_1,parent,false);
            return new ShoppingListActivity.ShoppingListHolder(view);
        }

        @Override
        public void onBindViewHolder(final ShoppingListActivity.ShoppingListHolder holder, int position){
            final ShoppingListItem item = mShoppingList.get(position);

            holder.mModel = item;
            holder.mNameTextView.setText(item.getName());
            holder.mQuantity.setQuantity(item.getQuantity());
            holder.itemView.setBackgroundColor(Color.parseColor(item.getItemColor()));

            if (item.mPhoto!=null){
                holder.mPhotoView.setImageBitmap(item.mPhoto);
            }
            else{
                holder.mPhotoView.setImageBitmap(null);
                item.fetchPhoto(ShoppingListActivity.this, new PhotoDownloadListener() {
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

   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.new_shopping_list, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_item_save_new_shopping_list:
                //saveShoppingList(mProducts);
                Toast toast = Toast.makeText(getApplicationContext(), "Ready to save", Toast.LENGTH_LONG);
                toast.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }*/

    /*public void saveShoppingList(List<ShoppingListItem> items){
       int res =  ShoppingListManager.CreateShoppingList();
       if (res!= -1){
           ShoppingListManager.AddShoppingListItems(res,items);
       }
    }*/
}
