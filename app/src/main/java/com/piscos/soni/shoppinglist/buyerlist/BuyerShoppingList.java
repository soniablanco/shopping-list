package com.piscos.soni.shoppinglist.buyerlist;

import com.piscos.soni.shoppinglist.shoppinglist.ShoppingList;
import com.piscos.soni.shoppinglist.shoppinglist.ShoppingListItem;
import com.piscos.soni.shoppinglist.shoppinglist.ShoppingListManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BuyerShoppingList {
    public UUID mId;
    public String mName;
    public List<BuyerShoppingListItem> Items;

    public static BuyerShoppingList getShoppingListById(UUID id){
        ShoppingList s = ShoppingListManager.getShoppingList(id);
        s.Items = ShoppingListManager.getShoppingListItems(id);
        BuyerShoppingList sl = convertFromShoppingList(s);
        return sl;
    }

    private static BuyerShoppingList convertFromShoppingList(ShoppingList list){
        BuyerShoppingList sl = new BuyerShoppingList();
        sl.mId = list.getId();
        sl.mName = list.getName();
        sl.Items = convertFromShoppingListItems(list.Items);
        return sl;
    }

    private static List<BuyerShoppingListItem> convertFromShoppingListItems(List<ShoppingListItem> items){
        List<BuyerShoppingListItem> list = new ArrayList<>();
        for(ShoppingListItem i : items){
            BuyerShoppingListItem bi= new BuyerShoppingListItem();
            bi.setName(i.getName());
            bi.mQuantity = i.getQuantity();
            bi.setCode(i.getCode());
            //bi.mPhotoUrl = i.mPhotoUrl;
            bi.setWasCollected(i.mWasCollected);
            list.add(bi);
        }
        return list;
    }

    private ShoppingList convertToShoppingList(BuyerShoppingList shoppingList){
        ShoppingList sl = new ShoppingList( shoppingList.mId, shoppingList.mName,null,null);
        return sl;
    }

    private  ShoppingListItem convertToShoppingListItem(BuyerShoppingListItem item){
        ShoppingListItem si =  new ShoppingListItem();
        si.setName(item.getName());
        si.setQuantity(item.mQuantity);
        si.setCode(item.getCode());
        //si.mPhotoUrl = item.mPhotoUrl;
        si.mWasCollected = item.wasCollected();
        return si;
    }

     public void updateItem(BuyerShoppingListItem item){
         ShoppingListItem si =  convertToShoppingListItem(item);
         ShoppingListManager.addShoppingListProduct(this.mId,si);
         ShoppingList s = convertToShoppingList(this);
         Long ts = System.currentTimeMillis()/1000;
         s.setLastUpdateTS(ts);
         ShoppingListManager.updateMyShoppingListsInfo(ts,null);
    }
 }
