package ga.piscos.shoppinglist.planning

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import ga.piscos.shoppinglist.observable
import ga.piscos.shoppinglist.plus
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable

class ProductsListViewModel(application: Application) : AndroidViewModel(application){

    var disposables = CompositeDisposable()
    val data= MutableLiveData<List<ProductItem>>()

        fun loadData(){


            val storesObservable = Firebase.database.reference.child("stores")
                .observable()
                .map {
                    it.children.map { storeSnapShot ->
                        ProductItem.Store.Template(
                            code = storeSnapShot.key!!,
                            logoURL = storeSnapShot.child("photoURL").value.toString()
                        )
                    }
                }

            val allProductsObservable = { stores:List<ProductItem.Store.Template> ->
                Firebase.database.reference.child("allproducts").observable().map { dataSnapshot ->
                    dataSnapshot.children.map {
                        ProductItem(
                            code = it.key!!,
                            name = it.child("name").value.toString(),
                            stores = it.child("stores").children.map { stRef ->
                                ProductItem.Store(
                                    code = stRef.key!!,
                                    photoURL = stRef.child("photoURL").value?.toString(),
                                    logoURL = stores.first { st -> st.code == stRef.key!! }.logoURL
                                )
                            }
                        )
                    }
                }
            }
            storesObservable.flatMap {
                allProductsObservable(it)
            }
            .map {list-> list.sortedBy { it.name } }
            .subscribe {
                    data.value = it
            }
        }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }




}