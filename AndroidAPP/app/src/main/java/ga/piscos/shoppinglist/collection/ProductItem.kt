package ga.piscos.shoppinglist.collection

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import ga.piscos.shoppinglist.observable
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit

class ProductItem(
    val code:String,
    val name: String,
    val picked:PickedData,
    val stores:List<Store>,
    var storeSectionInstance:StoreSection?=null

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

    fun getPhotoChangeObservable(selectedStoreCode:String):Observable<Store>{
        if (stores.any { it.code==selectedStoreCode && it.photoURL!=null }){
            return Observable.just(stores.first {  it.code==selectedStoreCode} )
        }

        val filteredStores = stores.filter { it.photoURL!=null }
        if (filteredStores.count()==0){
            return  Observable.just(Store("","","",sectionCode = null))
        }
        else if (filteredStores.count()==1){
            return Observable.just(filteredStores[0])
        }
        val observable1 =  Observable.just(1).map { it.toInt() }
        val observable2 =  Observable.interval(4, TimeUnit.SECONDS).delay(0.toLong()*150,TimeUnit.MILLISECONDS).map { it.toInt() }
        return Observable.concat(observable1,observable2)
                .map { filteredStores[it % filteredStores.count()]}
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun selectItem() {
        Firebase.database.reference.child("lists/current/products/${code}/pickedQty").setValue(picked.neededQty).observable().subscribe()
    }

    fun unSelect() {
        Firebase.database.reference.child("lists/current/products/${code}/pickedQty").setValue(null).observable().subscribe()
    }
    override val type get() = CollectionItemRow.Type.Item
}