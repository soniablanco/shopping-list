package com.piscos.soni.shoppinglist;

import java.util.List;

public interface SyncGetUpdatedShoppingListsListener {
    void onReady(List<SyncShoppingList> shoppingLists);
}
