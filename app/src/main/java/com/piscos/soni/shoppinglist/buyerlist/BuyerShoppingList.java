package com.piscos.soni.shoppinglist.buyerlist;

import java.util.List;
import java.util.UUID;

public class BuyerShoppingList {
    public UUID mId;
    public String mName;
    public Long mLastLocalTimeStamp;
    public void setLastLocalTimeStamp(Long lastLocalTimeStamp) {
        mLastLocalTimeStamp = lastLocalTimeStamp;
        BuyerShoppingListManager.updateShoppingList(this);
    }

    public List<BuyerShoppingListItem> Items;

    public BuyerShoppingList(UUID id,String name,Long lastLocalTimeStamp){
        this.mId = id;
        this.mName = name;
        this.mLastLocalTimeStamp = lastLocalTimeStamp;
    }

    public static BuyerShoppingList getShoppingListById(UUID id){
        BuyerShoppingList sl = BuyerShoppingListManager.getShoppingList(id);
        sl.Items = BuyerShoppingListManager.getShoppingListItems(id);
        return sl;
    }


     public void updateItem(BuyerShoppingListItem item){
        BuyerShoppingListManager.updateShoppingListProduct(this.mId,item);
        this.setLastLocalTimeStamp(System.currentTimeMillis()/1000);
        BuyerShoppingListManager.updateMyShoppingListsInfo(this.mLastLocalTimeStamp);
    }
 }
