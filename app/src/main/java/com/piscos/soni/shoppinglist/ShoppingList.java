package com.piscos.soni.shoppinglist;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;


public class ShoppingList {
    private UUID mId;
    private String mName;
    public List<ShoppingListItem> Items;

    public ShoppingList(){

    }

    public static ShoppingList GetNewShoppingList(){
        ShoppingList sl = new ShoppingList();
        DateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss.SSS");
        sl.mName = df.format(Calendar.getInstance().getTime());
        sl.mId = UUID.randomUUID();
        ShoppingListManager.CreateShoppingList(sl.mName,sl.mId);
        sl.Items = ShoppingListManager.GetAllProducts();
        return sl;
    }

    public static ShoppingList GetShoppingListById(UUID id){
        ShoppingList sl = new ShoppingList();
        sl.mName = ShoppingListManager.GetShoppingListName(id);
        sl.mId = id;
        sl.Items = ShoppingListManager.GetShoppingListProducts(id);
        return sl;
    }

    private static List<ShoppingListItem> GetItems(List<ShoppingListItem> selectedItems){
        List<ShoppingListItem> items = ShoppingListManager.GetAllProducts();
        for(ShoppingListItem i:items){
            for(ShoppingListItem j:selectedItems){
                if (i.getCode() == j.getCode()){
                    i.setQuantity(j.getQuantity());
                }
            }
        }
        return items;
    }

    public UUID getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public void UpdateItem(ShoppingListItem item){
        if(item.getQuantity() == 0){
            ShoppingListManager.DeleteShoppingListProduct(this.mId,item);
        }
        else{
            ShoppingListManager.AddShoppingListProduct(this.mId,item);
        }
    }

    public void synchronizeList(){
        ShoppingList toUpload = new ShoppingList();
        toUpload.mName = this.mName;
        toUpload.mId = this.getId();
        toUpload.Items = ShoppingListManager.GetShoppingListItems(this.getId());

        FBShoppingListsRepository repo = new FBShoppingListsRepository();
        repo.updloadList(toUpload);
    }
}
