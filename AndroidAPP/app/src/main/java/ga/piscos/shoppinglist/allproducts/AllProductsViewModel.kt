package ga.piscos.shoppinglist.allproducts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import ga.piscos.shoppinglist.observable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable

class AllProductsViewModel(application: Application) : AndroidViewModel(application){

    var disposables = CompositeDisposable()
    val data= MutableLiveData<List<AllProductItemRow>>()

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
                            houseSection = it.child("houseSection").value.toString(),
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

            val houseSectionsObservable = Firebase.database.reference.child("house/sections")
                .observable()
                .map {
                    it.children.map { sec ->
                            HouseSection(
                            code = sec.key!!,
                            name = sec.child("name").value.toString(),
                            index = (sec.child("index").value as Long).toInt()
                        )
                    }.sortedBy {s-> s.index  }
                }


            val productListObservable = storesObservable.flatMap {
                allProductsObservable(it)
            }

            val resultObservable = Observable.combineLatest(
                houseSectionsObservable,
                productListObservable,
                { houseSections:List<HouseSection>, products:List<ProductItem> ->
                    houseSections.forEach {hs-> hs.products = products.filter {p-> p.houseSection==hs.code } }
                    val rows = mutableListOf<AllProductItemRow>()
                    houseSections.forEach { hs-> rows.addAll(hs.getAllRows()) }
                    rows
                })


            resultObservable
            .subscribe {
                    data.value = it
            }
        }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }




}