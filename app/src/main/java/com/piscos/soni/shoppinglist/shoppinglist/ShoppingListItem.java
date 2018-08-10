package com.piscos.soni.shoppinglist.shoppinglist;

import com.piscos.soni.shoppinglist.products.ProductListItem;

public class ShoppingListItem extends ProductListItem {

    public static final String SELECTED_COLOUR = "#7986CB";
    public static final String UNSELECTED_COLOUR = "#FFFFFF";

    public void setQuantity(int quantity) {
        mQuantity = quantity;
        if(quantity >0){
            mItemColor = SELECTED_COLOUR;
        }
        else{
            mItemColor = UNSELECTED_COLOUR;
        }
        mTimestamp = System.currentTimeMillis()/1000;
        mUpdated = true;
    }

    public int getQuantity() {
        return mQuantity;
    }

    private int mQuantity;

    public String getItemColor() {
        return mItemColor;
    }

    private String mItemColor;
    public ShoppingListItem(){
        setQuantity(0);

    }

    public Long getTimestamp() {
        return mTimestamp;
    }

    private Long mTimestamp;

    public ShoppingListItem(String name, String code, String photoUrl){
        super.setName(name);
        super.setCode(code);
        super.mPhotoUrl = photoUrl;
        setQuantity(0);
        mUpdated = false;
        mWasCollected = false;
    }

    public ShoppingListItem(String name, String code, String photoUrl, int quantity,Long timestamp,boolean updated,boolean wasCollected){
        super.setName(name);
        super.setCode(code);
        super.mPhotoUrl = photoUrl;
        setQuantity(quantity);
        mTimestamp = timestamp;
        mUpdated = updated;
        mWasCollected = wasCollected;
    }

    /*public boolean isUpdated() {
        return mUpdated;
    }*/

    public boolean mUpdated;
    public boolean mWasCollected;
}
