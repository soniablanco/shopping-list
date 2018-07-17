package com.piscos.soni.shoppinglist;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;


public class ShoppingList {
    private UUID mId;
    private String mName;

    public long getLastUpdateTS() {
        return mLastUpdateTS;
    }

    public long getLastSyncTS() {
        return mLastSyncTS;
    }

    private void setLastUpdateTS(long lastUpdateTS) {
        mLastUpdateTS = lastUpdateTS;
        ShoppingListManager.UpdateShoppingList(this);
    }

    private long mLastUpdateTS;
    private long mLastSyncTS;
    public List<ShoppingListItem> Items;

    public ShoppingList(){

    }
    public ShoppingList(UUID id, String name,long lastUpdateTS,long lastSyncTS){
        mId = id;
        mName = name;
        mLastUpdateTS = lastUpdateTS;
        mLastSyncTS = lastSyncTS;
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
        ShoppingList sl = ShoppingListManager.GetShoppingList(id);
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
        this.setLastUpdateTS(System.currentTimeMillis()/1000);
    }

    public void synchronizeList(){
        final ShoppingList toUpload = new ShoppingList();
        toUpload.mName = this.mName;
        toUpload.mId = this.getId();

        final FBShoppingListsRepository repo = new FBShoppingListsRepository();
        repo.fetchListItems(new ShoppingListItemsDownloadedListener() {
            @Override
            public void onReady(List<ShoppingListItem> items) {

                toUpload.Items = getItemsForUpdate(items);
                repo.updloadList(toUpload);
                for(ShoppingListItem i: toUpload.Items){
                    repo.uploadListItem(toUpload.mId,i);
                    i.mUpdated =false;
                    ShoppingListManager.UpdateShoppingListProduct(toUpload.mId,i);
                }
            }
        });

    }

    private List<ShoppingListItem> getUpdatedItems(){
        List<ShoppingListItem> items = ShoppingListManager.GetShoppingListItems(this.getId());
        List<ShoppingListItem> forUpdate = new ArrayList<>();
        for(ShoppingListItem i:items) {
            if(i.mUpdated) {
                forUpdate.add(i);
            }
        }
        return forUpdate;
    }

    private List<ShoppingListItem> getItemsForUpdate(List<ShoppingListItem> items) {
        List<ShoppingListItem> localItems =  getUpdatedItems();
        List<ShoppingListItem> toUpdateItems = new ArrayList<>();
        for(ShoppingListItem i:localItems) {

            boolean isNew = true;

            for (ShoppingListItem j:items) {

                if(j.getCode() == i.getCode()){
                    isNew = false;
                }
                if(i.getTimestamp() > j.getTimestamp()){
                    toUpdateItems.add(i);
                }
            }
            if(isNew){
                toUpdateItems.add(i);
            }

        }
        return toUpdateItems;
    }

}
