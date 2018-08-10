package com.piscos.soni.shoppinglist.lists;

import java.util.List;

public interface SyncFetchUpdatedShoppingListsListener {
    void onReady(List<SyncShoppingList> shoppingLists);
}
