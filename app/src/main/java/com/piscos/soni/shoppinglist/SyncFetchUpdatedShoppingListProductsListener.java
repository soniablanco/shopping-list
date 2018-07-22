package com.piscos.soni.shoppinglist;

import java.util.List;

public interface SyncFetchUpdatedShoppingListProductsListener {
    void onReady(List<SyncShoppingListProduct> products);
}
