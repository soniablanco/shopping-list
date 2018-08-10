package com.piscos.soni.shoppinglist.lists;

import java.util.List;

public interface SyncFetchUpdatedShoppingListProductsListener {
    void onReady(List<SyncShoppingListProduct> products);
}
