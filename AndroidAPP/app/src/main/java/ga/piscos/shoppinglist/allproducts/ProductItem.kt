package ga.piscos.shoppinglist.allproducts

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit

class ProductItem(
    val code:String,
    val name: String,
    val houseSection:String?,
    val stores:List<Store>,
    var houseSectionInstance:HouseSection?=null

    ):AllProductItemRow{
        class Store(val code:String, val photoURL:String?, val logoURL: String){
            class Template(val code:String, val logoURL:String)
        }

    fun getPhotoChangeObservable(index:Int):Observable<Store>{
        val filteredStores = stores.filter { it.photoURL!=null }
        if (filteredStores.count()==0){
            return  Observable.just(Store("","",""))
        }
        else if (filteredStores.count()==1){
            return Observable.just(filteredStores[0])
        }
        val observable1 =  Observable.just(1).map { it.toInt() }
        val observable2 =  Observable.interval(4, TimeUnit.SECONDS).delay(index.toLong()*150,TimeUnit.MILLISECONDS).map { it.toInt() }
        return Observable.concat(observable1,observable2)
                .map { filteredStores[it % filteredStores.count()]}
                .observeOn(AndroidSchedulers.mainThread())
    }

    override val type get() = AllProductItemRow.Type.Item
}