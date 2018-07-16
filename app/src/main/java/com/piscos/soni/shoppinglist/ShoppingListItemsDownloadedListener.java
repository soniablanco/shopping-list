package com.piscos.soni.shoppinglist;

import java.util.List;

public interface ShoppingListItemsDownloadedListener {
    void onReady(List<ShoppingListItem> items);
}
