package com.piscos.soni.shoppinglist;

import java.util.ArrayList;
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

    public void PullServerData(final SyncShoppingListsPullListener listener){
        final MyShoppingListsCtrlInfo localCtrlInfo = ShoppingListManager.GetMyShoppingListsInfo();
        repo.getMyShoppingListsLastUpdateTimeStamp(new SyncGetMyShoppingListsLastTSListener() {
            @Override
            public void onReady(Long lastTimeStamp) {
                PullShoppingLists(localCtrlInfo.mLastSyncTS, new SyncGetUpdatedShoppingListsListener() {
                    @Override
                    public void onReady(List<SyncShoppingList> shoppingLists) {
                        //process list
                        //update local last sync and local last update
                        listener.onReady();
                    }
                });
            }
        });
    }

    private void PullShoppingLists(Long lastSyncTS, final SyncGetUpdatedShoppingListsListener listener){
        repo.getShoppingListsUpdated(lastSyncTS, new SyncFetchUpdatedShoppingListsListener() {
            @Override
            public void onReady(final List<SyncShoppingList> shoppingLists) {
                final int[] listsCount = new int[1];
                listsCount[0] = 0;
                for(final SyncShoppingList sl: shoppingLists){
                    repo.getUpdatedShoppingListProducts(sl.mId, new SyncFetchUpdatedShoppingListProductsListener() {
                        @Override
                        public void onReady(List<SyncShoppingListProduct> products) {
                            sl.Items = products;
                            listsCount[0] = listsCount[0]+1;
                            if(listsCount[0] == listsCount[0]){
                                listener.onReady(shoppingLists);
                            }
                         }
                    });
                }
            }
        });
    }

    private void UpdateDownloadedShoppingLists(List<SyncShoppingList> shoppingLists){
        List<ShoppingList> sl = ConvertToShoppingList(shoppingLists);
    }
    private List<ShoppingList> ConvertToShoppingList(List<SyncShoppingList> shoppingLists){
        List<ShoppingList> sl = new ArrayList<>();
        for(SyncShoppingList s:shoppingLists){
            ShoppingList item = new ShoppingList(s.mId,s.mName,s.mLastUpdateTS,s.mLastSyncTS);
            item.Items = ConvertToShoppingListItems(s.Items);
            sl.add(item);
        }
        return sl;
    }

    private List<ShoppingListItem> ConvertToShoppingListItems(List<SyncShoppingListProduct> products){
        List<ShoppingListItem> list = new ArrayList<>();
        for(SyncShoppingListProduct p:products){
            list.add(new ShoppingListItem());
        }
        return list;
    }
}
