package ga.piscos.shoppinglist.product


class ProductStoreSection(val  code: String, val  name: String){
    override fun toString(): String {
        return name
    }
}
class ProductStoreItem (val code:String, val name:String, val photoURL:String, val sections:List<ProductStoreSection>)