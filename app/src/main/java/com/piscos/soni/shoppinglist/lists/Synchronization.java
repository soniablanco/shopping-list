package com.piscos.soni.shoppinglist.lists;

import com.piscos.soni.shoppinglist.shoppinglist.ShoppingList;
import com.piscos.soni.shoppinglist.shoppinglist.ShoppingListItem;
import com.piscos.soni.shoppinglist.shoppinglist.ShoppingListManager;

import java.util.ArrayList;
import java.util.List;

public class Synchronization {

    FBShoppingListsRepository repo = new FBShoppingListsRepository();

    public void syncData(final SyncShoppingListsSyncListener listener){
        pushLocalData(new SyncShoppingListsPushListener() {
            @Override
            public void onReady() {
                listener.onPushReady();
                pullServerData(new SyncShoppingListsPullListener() {
                    @Override
                    public void onReady() {
                        listener.onPullReady();
                    }
                });
            }
        });
    }

    public void pushLocalData(final SyncShoppingListsPushListener listener){
        final MyShoppingListsCtrlInfo localCtrlInfo = ShoppingListManager.getMyShoppingListsInfo();
        repo.getMyShoppingListsLastUpdateTimeStamp(new SyncGetMyShoppingListsLastTSListener() {
            @Override
            public void onReady(final Long lastTimeStamp) {
                List<SyncShoppingList> toUpload = SynchronizationManager.getShoppingListsToUpload();
                for (final SyncShoppingList sl : toUpload) {
                    if (sl.mLastSyncTS != null) {
                        if (sl.mLastSyncTS > 0) {

                            repo.getShoppingListLastUpdateTimeStamp(sl, new SyncShoppingListLastUpdateTSListener() {
                                @Override
                                public void onReady(Long serverTS) {
                                    if (serverTS < sl.mLastUpdateTS) {
                                        repo.pushShoppingList(sl);
                                       if((localCtrlInfo.mLastLocalUpdateTS ==null?0:localCtrlInfo.mLastLocalUpdateTS) > lastTimeStamp){
                                            repo.updateMyShoppingListsLastUpdateTimestamp(localCtrlInfo.mLastLocalUpdateTS);
                                        }
                                        listener.onReady();
                                    }
                                }
                            });
                        } else {
                            repo.pushShoppingList(sl);
                           if((localCtrlInfo.mLastLocalUpdateTS ==null?0:localCtrlInfo.mLastLocalUpdateTS) > lastTimeStamp){
                                repo.updateMyShoppingListsLastUpdateTimestamp(localCtrlInfo.mLastLocalUpdateTS);
                            }
                            listener.onReady();
                        }
                    } else {
                        repo.pushShoppingList(sl);
                        if((localCtrlInfo.mLastLocalUpdateTS ==null?0:localCtrlInfo.mLastLocalUpdateTS) > lastTimeStamp){
                            repo.updateMyShoppingListsLastUpdateTimestamp(localCtrlInfo.mLastLocalUpdateTS);
                        }
                        listener.onReady();
                    }
                }
                if(toUpload.size() == 0){
                    listener.onReady();
                }
            }
        });
    }

    public void pullServerData(final SyncShoppingListsPullListener listener){
        final MyShoppingListsCtrlInfo localCtrlInfo = ShoppingListManager.getMyShoppingListsInfo();
        repo.getMyShoppingListsLastUpdateTimeStamp(new SyncGetMyShoppingListsLastTSListener() {
            @Override
            public void onReady(final Long lastTimeStamp) {
                pullShoppingLists(localCtrlInfo.mLastSyncTS, new SyncGetUpdatedShoppingListsListener() {
                    @Override
                    public void onReady(List<SyncShoppingList> shoppingLists) {
                        //process list
                        updateDownloadedShoppingLists(shoppingLists);
                        //update local last sync
                        ShoppingListManager.updateMyShoppingListsInfo(null,lastTimeStamp);
                        listener.onReady();
                    }
                });
            }
        });
    }

    private void pullShoppingLists(Long lastSyncTS, final SyncGetUpdatedShoppingListsListener listener){
        repo.getShoppingListsUpdated(lastSyncTS == null?0:lastSyncTS, new SyncFetchUpdatedShoppingListsListener() {
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
                            if(listsCount[0] == shoppingLists.size()){
                                listener.onReady(shoppingLists);
                            }
                         }
                    });
                }
            }
        });
    }

    private void updateDownloadedShoppingLists(List<SyncShoppingList> shoppingLists){
        List<ShoppingList> sl = convertToShoppingList(shoppingLists);
        for (ShoppingList s:sl
             ) {
            ShoppingListManager.deleteShoppingList(s.getId());
            ShoppingListManager.createShoppingList(s);
            ShoppingListManager.addShoppingListProducts(s.getId(),s.Items);
        }
    }
    private List<ShoppingList> convertToShoppingList(List<SyncShoppingList> shoppingLists){
        List<ShoppingList> sl = new ArrayList<>();
        for(SyncShoppingList s:shoppingLists){
            ShoppingList item = new ShoppingList(s.mId,s.mName,s.mLastUpdateTS,s.mLastSyncTS);
            item.Items = convertToShoppingListItems(s.Items);
            sl.add(item);
        }
        return sl;
    }

    private List<ShoppingListItem> convertToShoppingListItems(List<SyncShoppingListProduct> products){
        List<ShoppingListItem> list = new ArrayList<>();
        for(SyncShoppingListProduct p:products){
            list.add(new ShoppingListItem(p.mName, p.mCode, null, p.mQuantity,null,false,p.mWasCollected));
        }
        return list;
    }
}
