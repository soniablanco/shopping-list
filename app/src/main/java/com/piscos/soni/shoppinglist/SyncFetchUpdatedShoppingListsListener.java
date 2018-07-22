package com.piscos.soni.shoppinglist;

import java.util.List;

public interface SyncFetchUpdatedShoppingListsListener {
    void onReady(List<SyncShoppingList> shoppingLists);
}
