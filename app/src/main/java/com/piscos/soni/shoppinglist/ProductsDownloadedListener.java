package com.piscos.soni.shoppinglist;

import java.util.List;

public interface ProductsDownloadedListener {
    void onReady(List<ProductListItem> products);
}
