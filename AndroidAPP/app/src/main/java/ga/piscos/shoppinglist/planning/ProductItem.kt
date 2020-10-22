package ga.piscos.shoppinglist.planning

import android.util.Log
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import ga.piscos.shoppinglist.observable
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit

class ProductItem(
    val code:String,
    val name: String,
    val selectedData:SelecteData?,
    val houseSection:String?,
    val stores:List<Store>,
    var houseSectionInstance: HouseSection?=null

    ): AllProductItemRow {
class Store(val code:String, val photoURL:String?, val logoURL: String){
    class Template(val code:String, val logoURL:String)
}
    class SelecteData(val code:String, val neededQty:Int)

    fun getPhotoChangeObservable(index:Int):Observable<Store>{
        val filteredStores = stores.filter { it.photoURL!=null }
        if (filteredStores.count()==0){
            return  Observable.just(Store("","",""))
        }
        else if (filteredStores.count()==1){
            return Observable.just(filteredStores[0])
        }
        val observable2 =  Observable.interval(4, TimeUnit.SECONDS).map { it.toInt() }
        return observable2
                .map { filteredStores[it % filteredStores.count()]}
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun selectItem() {
        val neededQty = if (selectedData==null) 1 else (selectedData!!.neededQty+1)
        val firebaseDatabaseObservable =
            Observable.just(1).flatMap { Observable.just(mapOf("neededQty" to neededQty)) }
                .flatMap { Firebase.database.reference.child("lists/current/products/${code}").updateChildren(it).observable()}
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