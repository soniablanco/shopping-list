package com.piscos.soni.shoppinglist;

import android.support.annotation.NonNull;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class ProductsData {
    private DatabaseReference productsDB;
    private static final String NODE = "allproducts";

    public ProductsData(){
        productsDB = FirebaseDatabase.getInstance().getReference();
        productsDB = productsDB.child(NODE);
    }



    public  void  FetchProducts(final OnAllProductsDownloadedListener listener){
        final List<ProductListItem> productList = new ArrayList<>();
        productsDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Product product = ds.getValue(Product.class);
                    productList.add(new ProductListItem(product.name,product.code, product.thumbnailUrl));
                }
                listener.onReady(productList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    /*public List<ProductListItem> GetAllProducts() {
        final List<ProductListItem> productList = new ArrayList<>();
        productsDB.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                // Get the value from the DataSnapshot and add it to the products' list
                Product product = dataSnapshot.getValue(Product.class);
                ProductListItem productItem = new ProductListItem(product.name, product.code, product.thumbnailUrl);
                productList.add(productItem);
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
        return productList;
    }*/
}
