package com.piscos.soni.shoppinglist;

import java.util.List;
import java.util.UUID;

public class SyncShoppingList {
    public UUID mId;
    public String mName;
    public Long mLastUpdateTS;
    public Long mLastSyncTS;
    public List<SyncShoppingListProduct> Items;

    public SyncShoppingList(UUID id,String name,Long lastUpdateTS, Long lastSyncTS){
        this.mId = id;
        this.mName = name;
        this.mLastSyncTS = lastSyncTS;
        this.mLastUpdateTS = lastUpdateTS;
        this.Items = SynchronizationManager.GetShoppingListItems(this.mId);
    }
}
