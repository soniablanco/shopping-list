package ga.piscos.shoppinglist.collection

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import ga.piscos.shoppinglist.observable
import ga.piscos.shoppinglist.planning.ProductItem
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit

class ProductItem(
    val code:String,
    val name: String,
    val picked:PickedData,
    val stores:List<Store>,
    var storeSectionInstance:StoreSection?=null,
    var currentVisibleStoreIndex:Int = 0

    ):CollectionItemRow{
class Store(val code:String, val photoURL:String?, val logoURL: String, val sectionCode:String?){
    class Template(val code:String,val name:String, val logoURL:String, val sections:List<Section>){

        override fun toString() = name

        class Section(val code: String, val index: Int, val name: String)
    }
}
    class PickedData(val code:String, val neededQty:Int,val  pickedQty:Int?){
        val hasPicked get() = pickedQty!=null && pickedQty==neededQty
    }

    fun getCurrentVisibleStore (selectedStoreCode:String): Store? {
        val store = stores.firstOrNull { it.code==selectedStoreCode && it.photoURL!=null }
        val filteredStores =  if (store!=null) listOf(store) else stores.filter { it.photoURL!=null }
        return if (filteredStores.count()==0){
            null
        }else {
            filteredStores[currentVisibleStoreIndex]
        }
    }
    fun moveNextStoreIndex(selectedStoreCode:String):Boolean{
        val store = stores.firstOrNull { it.code==selectedStoreCode && it.photoURL!=null }
        val filteredStores =  if (store!=null) listOf(store) else stores.filter { it.photoURL!=null }
        if (!filteredStores.any())
            return false
        val nextIndex = (currentVisibleStoreIndex + 1) % filteredStores.count()
        val indexChanged = nextIndex != currentVisibleStoreIndex
        currentVisibleStoreIndex = nextIndex
        return indexChanged
    }



    fun selectItem() {
        Firebase.database.reference.child("lists/current/products/${code}/pickedQty").setValue(picked.neededQty).observable().subscribe()
    }

    fun unSelect() {
        Firebase.database.reference.child("lists/current/products/${code}/pickedQty").setValue(null).observable().subscribe()
    }
    override val type get() = CollectionItemRow.Type.Item
}