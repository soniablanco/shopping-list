package com.piscos.soni.shoppinglist;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BuyerShoppingList {
    public UUID mId;
    public String mName;
    public List<BuyerShoppingListItem> Items;

    public static BuyerShoppingList GetShoppingListById(UUID id){
        ShoppingList s = ShoppingListManager.GetShoppingList(id);
        s.Items = ShoppingListManager.GetShoppingListItems(id);
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
            bi.mPhotoUrl = i.mPhotoUrl;
            list.add(bi);
        }
        return list;
    }
}
