package com.piscos.soni.shoppinglist;

public class ShoppingListElement {
    public String mName;
    public int mId;
    public int mTotalItems;

    public ShoppingListElement(int id, String name, int totalItems){
        this.mId = id;
        this.mName = name;
        this.mTotalItems = totalItems;
    }
}
