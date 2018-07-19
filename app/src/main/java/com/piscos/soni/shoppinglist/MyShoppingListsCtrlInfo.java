package com.piscos.soni.shoppinglist;

public class MyShoppingListsCtrlInfo {
    public Long mLastLocalUpdateTS;
    public Long mLastSyncTS;

    public MyShoppingListsCtrlInfo(Long lastLocalUpdateTS,Long lastSyncTS){
        this.mLastLocalUpdateTS = lastLocalUpdateTS;
        this.mLastSyncTS = lastSyncTS;
    }
}
