package ga.piscos.shoppinglist.collection

import java.util.*

class StoreSection (val code:String,val  name:String,val index:Int,val finishedSection:Boolean, var products:List<ProductItem>?=null):
    CollectionItemRow {
    fun getAllRows(): List<CollectionItemRow> {
        val list = mutableListOf<CollectionItemRow>()
        if (products!!.any()) {
            list.add(this)
            list.addAll(products!!.sortedBy { it.name.toLowerCase(Locale.ROOT) })
        }
        return  list
    }

    fun assignSection(){
        products!!.forEach { it.storeSectionInstance=this }
    }


    override val type get() = CollectionItemRow.Type.Header


}