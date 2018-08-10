package com.piscos.soni.shoppinglist.shoppinglist;

import com.piscos.soni.shoppinglist.lists.FBShoppingListsRepository;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;


public class ShoppingList {
    private UUID mId;
    private String mName;

    public Long getLastUpdateTS() {
        return mLastUpdateTS;
    }

    public void setLastSyncTS(Long lastSyncTS) {
        mLastSyncTS = lastSyncTS;
        ShoppingListManager.updateShoppingList(this);
    }

    public Long getLastSyncTS() {
        return mLastSyncTS;
    }

    public void setLastUpdateTS(Long lastUpdateTS) {
        mLastUpdateTS = lastUpdateTS;
        ShoppingListManager.updateShoppingList(this);
    }

    private Long mLastUpdateTS;
    private Long mLastSyncTS;
    public List<ShoppingListItem> Items;

    public ShoppingList(){

    }
    public ShoppingList(UUID id, String name,Long lastUpdateTS,Long lastSyncTS){
        mId = id;
        mName = name;
        mLastUpdateTS = lastUpdateTS;
        mLastSyncTS = lastSyncTS;
    }

    public static ShoppingList newShoppingList(){
        ShoppingList sl = new ShoppingList();
        DateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss.SSS");
        sl.mName = df.format(Calendar.getInstance().getTime());
        sl.mId = UUID.randomUUID();
        sl.mLastUpdateTS = System.currentTimeMillis()/1000;
        ShoppingListManager.createShoppingList(sl);
        ShoppingListManager.updateMyShoppingListsInfo(sl.getLastUpdateTS(),sl.getLastSyncTS());
        sl.Items = ShoppingListManager.getAllProducts();
        return sl;
    }

    /*public static ShoppingList newShoppingList(UUID id, String name,Long lastUpdateTS,Long lastSyncTS){
        ShoppingList sl = new ShoppingList(id,name,lastUpdateTS,lastSyncTS);
        ShoppingListManager.createShoppingList(sl.mName,sl.mId,sl.getLastUpdateTS(),sl.getLastSyncTS());
        sl.Items = ShoppingListManager.getAllProducts();
        return sl;
    }*/

    public static ShoppingList getShoppingListById(UUID id){
        ShoppingList sl = ShoppingListManager.getShoppingList(id);
        sl.Items = ShoppingListManager.getShoppingListProducts(id);
        return sl;
    }

    private static List<ShoppingListItem> getItems(List<ShoppingListItem> selectedItems){
        List<ShoppingListItem> items = ShoppingListManager.getAllProducts();
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

    public void updateItem(ShoppingListItem item){
        if(item.getQuantity() == 0){
            ShoppingListManager.deleteShoppingListProduct(this.mId,item);
        }
        else{
            ShoppingListManager.addShoppingListProduct(this.mId,item);
        }

        this.setLastUpdateTS(System.currentTimeMillis()/1000);
        ShoppingListManager.updateMyShoppingListsInfo(this.getLastUpdateTS(),this.getLastSyncTS());
    }

    public void synchronizeList(){
        if(this.getLastUpdateTS() > this.getLastSyncTS()) {
            final ShoppingList toUpload = new ShoppingList(this.getId(), this.mName, this.getLastUpdateTS(), this.getLastSyncTS());
            toUpload.Items = ShoppingListManager.getShoppingListItems(this.getId());

            final FBShoppingListsRepository repo = new FBShoppingListsRepository();
            repo.uploadList(toUpload);
            repo.uploadListItems(toUpload.getId(),toUpload.Items);

        }
    }

    /*public void synchronizeList(){
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
                    ShoppingListManager.updateShoppingListProduct(toUpload.mId,i);
                }
            }
        });

    }

    private List<ShoppingListItem> getUpdatedItems(){
        List<ShoppingListItem> items = ShoppingListManager.getShoppingListItems(this.getId());
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
    }*/

}
