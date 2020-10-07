package ga.piscos.shoppinglist.allproducts

import android.util.Log
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit

class ProductItem(
    val code:String,
    val name: String,
    val aldiPhotoURL:String?,
    val lidPhotoURL:String?

    ){

    fun getPhotoChangeObservable(index:Int):Observable<String>{
        val images = listOfNotNull(aldiPhotoURL, lidPhotoURL)
        val observable1 =  Observable.just(1).map { it.toInt() }
        val observable2 =  Observable.just(2).delay(index.toLong()*150,TimeUnit.MILLISECONDS).map { it.toInt() }
        val observable3 =  Observable.interval(3, TimeUnit.SECONDS).map{it+3}.map { it.toInt() }
        return Observable.concat(observable1,observable2,observable3)
                .map { images[it % images.count()]}
                .observeOn(AndroidSchedulers.mainThread())
    }
}