package ga.piscos.shoppinglist.allproducts

class HouseSection(val code:String,val  name:String,val index:Int, var products:List<ProductItem>?=null):AllProductItemRow {
    fun getAllRows(): List<AllProductItemRow> {
        val list = mutableListOf<AllProductItemRow>()
        list.add(this)
        list.addAll(products!!)
        return  list
    }

    override val type get() = AllProductItemRow.Type.Header


}