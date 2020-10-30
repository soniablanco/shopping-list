package ga.piscos.shoppinglist.planning

import android.util.Log
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import ga.piscos.shoppinglist.observable
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import java.util.*
import java.util.concurrent.TimeUnit

class ProductItem(
    val code:String,
    val name: String,
    val selectedData:SelecteData?,
    val houseSection:String?,
    val stores:List<Store>,
    var houseSectionInstance: HouseSection?=null,
    var currentVisibleStoreIndex:Int=0

    ): AllProductItemRow {
class Store(val code:String, val photoURL:String?, val logoURL: String){
    class Template(val code:String, val logoURL:String)
}
    class SelecteData(val code:String, val neededQty:Int, val addedTimeStamp:Int)

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

    fun selectItem() {
        val neededQty = if (selectedData==null) 1 else (selectedData.neededQty+1)
        val firebaseDatabaseObservable =
            Observable.just(1).flatMap { Observable.just(mapOf("neededQty" to neededQty, "addedTimeStamp" to (Date().time/1000).toInt())) }
                .flatMap { Firebase.database.reference.child("lists/current/products/${code}/planning").updateChildren(it).observable()}
        firebaseDatabaseObservable.subscribe()
    }

    fun unSelect() {
        val firebaseDatabaseObservable =
            Observable.just(1).flatMap { Observable.just(mapOf(code to null)) }
                .flatMap { Firebase.database.reference.child("lists/current/products").updateChildren(it).observable()}
        firebaseDatabaseObservable.subscribe()
    }
    override val type get() = AllProductItemRow.Type.Item
}