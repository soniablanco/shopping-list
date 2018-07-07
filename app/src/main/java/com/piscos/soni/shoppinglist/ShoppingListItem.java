package com.piscos.soni.shoppinglist;

public class ShoppingListItem extends ProductListItem{

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

    public ShoppingListItem(String name, String code, String photoUrl){
        super.setName(name);
        super.setCode(code);
        super.mPhotoUrl = photoUrl;
        setQuantity(0);

    }

    public ShoppingListItem(String name, String code, String photoUrl, int quantity){
        super.setName(name);
        super.setCode(code);
        super.mPhotoUrl = photoUrl;
        setQuantity(quantity);

    }
}
