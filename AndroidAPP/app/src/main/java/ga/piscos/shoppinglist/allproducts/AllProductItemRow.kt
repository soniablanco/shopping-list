package ga.piscos.shoppinglist.allproducts

interface AllProductItemRow {
    val type: Type
    enum class Type(val intValue: Int) {
        Item(0),
        Header(1)
    }
}

