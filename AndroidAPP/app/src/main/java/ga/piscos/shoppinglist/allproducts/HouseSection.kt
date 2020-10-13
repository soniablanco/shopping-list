package ga.piscos.shoppinglist.allproducts

import java.util.*

class HouseSection(val code:String,val  name:String,val index:Int, var products:List<ProductItem>?=null):AllProductItemRow {
    fun getAllRows(): List<AllProductItemRow> {
        val list = mutableListOf<AllProductItemRow>()
        list.add(this)
        list.addAll(products!!.sortedBy { it.name.toLowerCase(Locale.ROOT) })
        return  list
    }

    override val type get() = AllProductItemRow.Type.Header


}