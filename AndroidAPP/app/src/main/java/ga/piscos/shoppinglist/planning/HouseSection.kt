package ga.piscos.shoppinglist.planning

import java.util.*

class HouseSection(val code:String,val  name:String,val index:Int, var products:List<ProductItem>?=null):AllProductItemRow {
    fun getAllRows(): List<AllProductItemRow> {
        val list = mutableListOf<AllProductItemRow>()
        list.add(this)
        list.addAll(products!!.sortedBy { it.name.toLowerCase(Locale.ROOT) })
        return  list
    }

    fun assignSection(){
        products!!.forEach { it.houseSectionInstance=this }
    }

    override val type get() = AllProductItemRow.Type.Header


}