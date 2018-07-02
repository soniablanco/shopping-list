package com.piscos.soni.shoppinglist;

public class ShoppingListItem extends ProductListItem{

    public double mQuantity;
    public ShoppingListItem(){
        mQuantity = 0;
    }

    public ShoppingListItem(String name, String code, String photoUrl){
        super.setName(name);
        super.setCode(code);
        super.mPhotoUrl = photoUrl;
        mQuantity = 0;
    }
}
