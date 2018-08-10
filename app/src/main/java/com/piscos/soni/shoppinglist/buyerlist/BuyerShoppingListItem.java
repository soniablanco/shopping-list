package com.piscos.soni.shoppinglist.buyerlist;

import com.piscos.soni.shoppinglist.products.ProductListItem;

public class BuyerShoppingListItem extends ProductListItem {
    public static final String COLLECTED_COLOUR = "#7986CB";
    public static final String UNCOLLECTED_COLOUR = "#FFFFFF";
    public int mQuantity;

    public void setWasCollected(boolean wasCollected) {
        mWasCollected = wasCollected;
        if(wasCollected){
            mColour = COLLECTED_COLOUR;
        }
        else{
            mColour = UNCOLLECTED_COLOUR;
        }
    }

    public boolean wasCollected() {
        return mWasCollected;
    }

    private boolean mWasCollected;

    public String getColour() {
        return mColour;
    }

    private String mColour;


}
