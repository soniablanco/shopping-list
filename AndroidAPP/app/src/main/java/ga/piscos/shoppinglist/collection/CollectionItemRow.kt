package ga.piscos.shoppinglist.collection

interface CollectionItemRow {
    val type: Type
    enum class Type(val intValue: Int) {
        Item(0),
        Header(1)
    }
}

