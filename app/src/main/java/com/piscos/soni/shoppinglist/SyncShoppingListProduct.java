package com.piscos.soni.shoppinglist;

public class SyncShoppingListProduct {
    public String mName;
    public String mCode;
    public int mQuantity;
    public boolean mWasCollected;

    public SyncShoppingListProduct(String name, String code, int quantity,boolean wasCollected){
        this.mCode = code;
        this.mName = name;
        this.mQuantity = quantity;
        this.mWasCollected = wasCollected;
    }
}
