package com.piscos.soni.shoppinglist;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class ProductListItemsTestData {
    private static ProductListItemsTestData sProductListItemsTestData;
    private List<ProductListItem> mProductListItems;

    public static ProductListItemsTestData get(Context context){
        if (sProductListItemsTestData == null){
            sProductListItemsTestData = new ProductListItemsTestData(context);
        }
        return sProductListItemsTestData;
    }

    private ProductListItemsTestData(Context context){
        mProductListItems = new ArrayList<>();
        for (int i=0; i<10;i++){
            ProductListItem item = new ProductListItem();
            item.setName("FBProduct " + i);
            mProductListItems.add(item);
        }
    }

    public List<ProductListItem> getProductListItems(){
        return mProductListItems;
    }
}
