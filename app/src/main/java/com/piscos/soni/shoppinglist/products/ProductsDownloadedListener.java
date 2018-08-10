package com.piscos.soni.shoppinglist.products;

import java.util.List;

public interface ProductsDownloadedListener {
    void onReady(List<ProductListItem> products);
}
