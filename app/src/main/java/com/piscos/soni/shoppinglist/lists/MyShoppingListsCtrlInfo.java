package com.piscos.soni.shoppinglist.lists;

public class MyShoppingListsCtrlInfo {
    public Long mLastLocalUpdateTS;
    public Long mLastSyncTS;

    public MyShoppingListsCtrlInfo(){
        this.mLastLocalUpdateTS = null;
        this.mLastSyncTS = null;
    }

    public MyShoppingListsCtrlInfo(Long lastLocalUpdateTS,Long lastSyncTS){
        this.mLastLocalUpdateTS = lastLocalUpdateTS;
        this.mLastSyncTS = lastSyncTS;
    }
}
