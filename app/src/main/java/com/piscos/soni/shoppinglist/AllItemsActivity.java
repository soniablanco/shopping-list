package com.piscos.soni.shoppinglist;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class AllItemsActivity extends AppCompatActivity {

    private RecyclerView mProductsRecyclerView;
    private ProductListAdapter mAdapter;


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

        updateUI();
    }

    private void updateUI(){
        ProductListItemsTestData data = ProductListItemsTestData.get(this);
        List<ProductListItem> productList = data.getProductListItems();

        mAdapter = new ProductListAdapter(productList);
        mProductsRecyclerView.setAdapter(mAdapter);
    }

    private class ProductHolder extends RecyclerView.ViewHolder{
        public TextView mNameTextView;
        public ProductHolder(View itemView){
            super(itemView);
            mNameTextView = (TextView)itemView.findViewById(R.id.tvProductName);
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
            holder.mNameTextView.setText(item.getName());
        }
        @Override
        public int getItemCount(){
            return mProductList.size();
        }
    }
}
