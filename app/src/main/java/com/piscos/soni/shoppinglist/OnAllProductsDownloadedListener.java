package com.piscos.soni.shoppinglist;

import java.util.List;

public interface OnAllProductsDownloadedListener {
    void onReady(List<ProductListItem> products);
}
