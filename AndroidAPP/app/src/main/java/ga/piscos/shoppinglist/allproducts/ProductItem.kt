package ga.piscos.shoppinglist.allproducts

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit

class ProductItem(
    val code:String,
    val name: String,
    val houseSection:String?,
    val stores:List<Store>,
    var houseSectionInstance:HouseSection?=null,
    var currentVisibleStoreIndex: Int = 0

    ):AllProductItemRow{
        class Store(val code:String, val photoURL:String?, val logoURL: String){
            class Template(val code:String, val logoURL:String)
        }

    val currentVisibleStore: Store? get() {
        val filteredStores = stores.filter { it.photoURL!=null }
        return if (filteredStores.count()==0){
            null
        }else {
            filteredStores[currentVisibleStoreIndex]
        }
    }
    fun moveNextStoreIndex():Boolean{
        val filteredStores = stores.filter { it.photoURL!=null }
        if (!filteredStores.any())
            return false
        val nextIndex = (currentVisibleStoreIndex + 1) % filteredStores.count()
        val indexChanged = nextIndex != currentVisibleStoreIndex
        currentVisibleStoreIndex = nextIndex
        return indexChanged
    }



    override val type get() = AllProductItemRow.Type.Item
}