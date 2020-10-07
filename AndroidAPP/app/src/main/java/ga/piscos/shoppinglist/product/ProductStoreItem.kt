package ga.piscos.shoppinglist.product

import android.graphics.Bitmap



class StoredProductStoreItem (val code:String, val photoURL:String, val section:String)
class StoredProductItem (val code:String, val name:String, val houseSection:String, val stores:List<StoredProductStoreItem>)

class TemplateHouseSection(val code:String, val name:String){
    override fun toString(): String {
        return name
    }
}
class TemplateStoreSection(val code:String, val name:String){
    override fun toString(): String {
        return name
    }
}
class TemplateStore(val code:String, val logoURL:String,val sections:List<TemplateStoreSection>)

class ProductTemplate(val houseSections:List<TemplateHouseSection>, val stores: List<TemplateStore>)

class EditingProductStore(val code:String, var photoFile: String?, var section: String?)
class EditingProduct(var productName:String?, var houseSection: String?, val  stores: MutableList<EditingProductStore> = mutableListOf())

class ProductModel(val productTemplate: ProductTemplate, val editingProduct: EditingProduct)

