package com.piscos.soni.shoppinglist.lists;

import java.util.List;

public interface SyncGetUpdatedShoppingListsListener {
    void onReady(List<SyncShoppingList> shoppingLists);
}
