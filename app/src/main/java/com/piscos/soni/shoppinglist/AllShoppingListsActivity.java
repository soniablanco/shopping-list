package com.piscos.soni.shoppinglist;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
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

import java.util.ArrayList;
import java.util.List;

public class AllShoppingListsActivity extends AppCompatActivity {
    private RecyclerView mProductsRecyclerView;
    private AllShoppingListsActivity.ShoppingListsAdapter mAdapter;

    private FloatingActionButton mBtnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_shopping_lists);

        mBtnAdd = (FloatingActionButton) findViewById(R.id.fabNewShoppingList);

        mBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = ShoppingListActivity.newIntent(AllShoppingListsActivity.this);
                startActivity(i);
            }
        });

        mProductsRecyclerView = (RecyclerView) findViewById(R.id.rv_all_shopping_lists);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mProductsRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mProductsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        updateUI();

    }

    public void updateUI() {

        List<ShoppingListElement> shoppingLists = ShoppingListManager.GetAllShoppingLists();
        mAdapter = new AllShoppingListsActivity.ShoppingListsAdapter(shoppingLists);
        mProductsRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    private class ShoppingListHolder extends RecyclerView.ViewHolder{
        public TextView mNameTextView;
        public TextView mTotalItemsView;
        private ShoppingListElement mModel;
        public ShoppingListHolder(View itemView){
            super(itemView);
            mNameTextView = (TextView)itemView.findViewById(R.id.tv_sl_name);
            mTotalItemsView = (TextView)itemView.findViewById(R.id.tv_sl_total_items);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = ShoppingListActivity.newIntent(AllShoppingListsActivity.this,mModel.mId);
                    startActivity(i);
                }
            });
        }
    }


    private class ShoppingListsAdapter extends RecyclerView.Adapter<AllShoppingListsActivity.ShoppingListHolder>{

        private List<ShoppingListElement> mShoppingLists;

        public ShoppingListsAdapter(List<ShoppingListElement> products){
            mShoppingLists = products;
        }

        @Override
        public AllShoppingListsActivity.ShoppingListHolder onCreateViewHolder(ViewGroup parent, int viewType){
            LayoutInflater layoutInflater = LayoutInflater.from(getBaseContext());
            View view = layoutInflater.inflate(R.layout.shopping_lists_element,parent,false);
            //View view = layoutInflater.inflate(android.R.layout.simple_list_item_1,parent,false);
            return new AllShoppingListsActivity.ShoppingListHolder(view);
        }

        @Override
        public void onBindViewHolder(final AllShoppingListsActivity.ShoppingListHolder holder, int position){
            final ShoppingListElement item = mShoppingLists.get(position);

            holder.mModel = item;
            holder.mNameTextView.setText(item.mName);
            holder.mTotalItemsView.setText(String.valueOf(item.mTotalItems));

            /*if (item.mPhoto!=null){
                holder.mPhotoView.setImageBitmap(item.mPhoto);
            }
            else{
                holder.mPhotoView.setImageBitmap(null);
                //productsData.downloadPhoto(item.mPhotoUrl,item.getCode(), new PhotoDownloadListener() {
                item.fetchPhoto(AllShoppingListsActivity.this, new PhotoDownloadListener() {
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
            }*/
        }
        @Override
        public int getItemCount(){
            return mShoppingLists.size();
        }
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

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

}
