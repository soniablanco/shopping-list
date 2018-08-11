package com.piscos.soni.shoppinglist.buyerlist;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.piscos.soni.shoppinglist.R;
import com.piscos.soni.shoppinglist.shoppinglist.ShoppingListActivity;
import com.piscos.soni.shoppinglist.products.PhotoDownloadListener;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import java.util.List;
import java.util.UUID;

public class BuyerShoppingListActivity extends AppCompatActivity {

    private RecyclerView mProductsRecyclerView;
    private BuyerShoppingListActivity.ShoppingListAdapter mAdapter;

    private static final String EXTRA_SHOPPING_LIST_ID = "com.piscos.soni.shoppinglist.shoppingListId";

    private BuyerShoppingList mShoppingList;
    private String mShoppingListId;

    public static Intent newIntent(Context packageContext, UUID shoppingListId){
        Intent i = new Intent(packageContext,BuyerShoppingListActivity.class);
        if(shoppingListId != null) {
            i.putExtra(EXTRA_SHOPPING_LIST_ID, shoppingListId.toString());
        }
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer_shopping_list);

        mProductsRecyclerView = (RecyclerView) findViewById(R.id.rv_shopping_list);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mProductsRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mProductsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mShoppingListId = getIntent().getStringExtra(EXTRA_SHOPPING_LIST_ID);

        updateUI();
        //HockeyApp
        checkForUpdates();
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

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
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
        if (mShoppingListId != null) {
            mShoppingList = BuyerShoppingList.getShoppingListById(UUID.fromString(mShoppingListId));
        }
        mAdapter = new BuyerShoppingListActivity.ShoppingListAdapter(mShoppingList.Items);
        mProductsRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    private class ShoppingListHolder extends RecyclerView.ViewHolder{
        public TextView mNameTextView;
        public ImageView mPhotoView;
        public TextView mQuantity;
        public CheckBox mWasCollected;

        private BuyerShoppingListItem mModel;

        public ShoppingListHolder(final View itemView){
            super(itemView);
            mNameTextView = (TextView)itemView.findViewById(R.id.tvBSLProductName);
            mPhotoView = (ImageView)itemView.findViewById(R.id.imBSLPhotoView);
            mQuantity = (TextView)itemView.findViewById(R.id.tvBSLQuantity);
            mWasCollected = (CheckBox)itemView.findViewById(R.id.cbWasCollected);

            mWasCollected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked != mModel.wasCollected()) {
                        mModel.setWasCollected(isChecked);
                        itemView.setBackgroundColor(Color.parseColor(mModel.getColour()));
                        mShoppingList.updateItem(mModel);
                    }
                }
            });
        }
    }


    private class ShoppingListAdapter extends RecyclerView.Adapter<BuyerShoppingListActivity.ShoppingListHolder>{

        private List<BuyerShoppingListItem> mShoppingList;

        public ShoppingListAdapter(List<BuyerShoppingListItem> products){
            mShoppingList = products;
        }

        @Override
        public BuyerShoppingListActivity.ShoppingListHolder onCreateViewHolder(ViewGroup parent, int viewType){
            LayoutInflater layoutInflater = LayoutInflater.from(getBaseContext());
            View view = layoutInflater.inflate(R.layout.buyer_shopping_list_item,parent,false);
            //View view = layoutInflater.inflate(android.R.layout.simple_list_item_1,parent,false);
            return new BuyerShoppingListActivity.ShoppingListHolder(view);
        }

        @Override
        public void onBindViewHolder(final BuyerShoppingListActivity.ShoppingListHolder holder, int position){
            final BuyerShoppingListItem item = mShoppingList.get(position);

            holder.mModel = item;
            holder.mNameTextView.setText(item.getName());
            holder.mQuantity.setText(Integer.toString(item.mQuantity));
            holder.mWasCollected.setChecked(item.wasCollected());
            holder.itemView.setBackgroundColor(Color.parseColor(item.getColour()));

            if (item.mPhoto!=null){
                holder.mPhotoView.setImageBitmap(item.mPhoto);
            }
            else{
                holder.mPhotoView.setImageBitmap(null);
                item.fetchPhoto(BuyerShoppingListActivity.this, new PhotoDownloadListener() {
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
        inflater.inflate(R.menu.shopping_list, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_item_edit_shopping_list:
                Intent i = ShoppingListActivity.newIntent(BuyerShoppingListActivity.this,UUID.fromString(mShoppingListId));
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
