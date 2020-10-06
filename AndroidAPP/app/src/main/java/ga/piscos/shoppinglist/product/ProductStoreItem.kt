package ga.piscos.shoppinglist.product

import android.graphics.Bitmap


class ProductStoreSection(val  code: String, val  name: String){
    override fun toString(): String {
        return name
    }
}
class ProductStoreItem (val code:String, val name:String, val photoURL:String, val sections:List<ProductStoreSection>, var photoBMP:Bitmap?=null)