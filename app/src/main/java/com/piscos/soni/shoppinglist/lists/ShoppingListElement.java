package com.piscos.soni.shoppinglist.lists;

import java.util.UUID;

public class ShoppingListElement {
    public String mName;
    public int mId;
    public int mTotalItems;
    public UUID mUUID;

    public ShoppingListElement(int id, String name, int totalItems,UUID uuid){
        this.mId = id;
        this.mName = name;
        this.mTotalItems = totalItems;
        this.mUUID = uuid;
    }
}
