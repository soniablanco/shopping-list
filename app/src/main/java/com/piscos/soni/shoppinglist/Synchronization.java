package com.piscos.soni.shoppinglist;

import java.util.List;

public class Synchronization {

    FBShoppingListsRepository repo = new FBShoppingListsRepository();

    public void PushLocalData(final SyncShoppingListsPushListener listener){
        List<SyncShoppingList> toUpload = SynchronizationManager.GetShoppingListsToUpload();
        for(final SyncShoppingList sl:toUpload){
            if(sl.mLastSyncTS != null){
                if(sl.mLastSyncTS >0){

                    repo.getShoppingListLastUpdateTimeStamp(sl, new SyncShoppingListLastUpdateTSListener() {
                        @Override
                        public void onReady(Long serverTS) {
                            if(serverTS < sl.mLastUpdateTS){
                                repo.pushShoppingList(sl);
                                listener.onReady();
                            }
                        }
                    });
                }
                else{
                    repo.pushShoppingList(sl);
                    listener.onReady();
                }
            }
            else {
                repo.pushShoppingList(sl);
                listener.onReady();
            }
        }
    }
}
