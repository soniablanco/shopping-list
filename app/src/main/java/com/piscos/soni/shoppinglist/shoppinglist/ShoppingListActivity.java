package com.piscos.soni.shoppinglist.shoppinglist;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.piscos.soni.shoppinglist.ProductPhotoReadyListener;
import com.piscos.soni.shoppinglist.R;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import java.util.List;
import java.util.UUID;

import me.himanshusoni.quantityview.QuantityView;

public class ShoppingListActivity extends AppCompatActivity {

    private RecyclerView mProductsRecyclerView;
    private ShoppingListActivity.ShoppingListAdapter mAdapter;

    private static final String EXTRA_SHOPPING_LIST_ID = "com.piscos.soni.shoppinglist.shoppingListId";

    private ShoppingList mShoppingList;

    public static Intent newIntent(Context packageContext, UUID shoppingListId){
        Intent i = new Intent(packageContext,ShoppingListActivity.class);
        if(shoppingListId != null) {
            i.putExtra(EXTRA_SHOPPING_LIST_ID, shoppingListId.toString());
        }
        return i;
    }

    public static Intent newIntent(Context packageContext){
        return newIntent(packageContext,null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_shopping_list);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        //getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);


        mProductsRecyclerView = (RecyclerView) findViewById(R.id.rv_new_shopping_list);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mProductsRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mProductsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        String shoppingListId = getIntent().getStringExtra(EXTRA_SHOPPING_LIST_ID);
        if (shoppingListId == null) {
            mShoppingList = ShoppingList.newShoppingList();//ShoppingList.getShoppingListById(7);
            getSupportActionBar().setTitle(R.string.new_list_activity_title);
        }
        else if (shoppingListId.isEmpty()) {
            getSupportActionBar().setTitle(R.string.new_list_activity_title);
            mShoppingList = ShoppingList.newShoppingList();
        }
        else{
            mShoppingList = ShoppingList.getShoppingListById(UUID.fromString(shoppingListId));
            getSupportActionBar().setTitle((String)mShoppingList.getName());
        }

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
                    mShoppingList.updateItem(mModel);
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
                item.loadPhoto(ShoppingListActivity.this,new ProductPhotoReadyListener(){
                    @Override
                    public void onReady(){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (item.getCode() == holder.mModel.getCode())
                                {
                                    holder.mPhotoView.setImageBitmap(item.mPhoto);
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

}
