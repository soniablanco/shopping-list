package ga.piscos.shoppinglist.collection

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

            val allProductsObservable = { stores:List<ProductItem.Store.Template>, pickedProducts:List<ProductItem.PickedData> ->
                Firebase.database.reference.child("allproducts").observable().map { dataSnapshot ->
                    dataSnapshot.children.filter {sna-> pickedProducts.any { s->s.code==sna.key!! }  }
                    .map {
                        ProductItem(
                            code = it.key!!,
                            name = it.child("name").value.toString(),
                            picked  = pickedProducts.first {s-> s.code==it.key!! },
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

            val selectedProductsObervable = Firebase.database.reference.child("lists/current/products").observable().map { dataSnapshot ->
                dataSnapshot.children.map {
                    ProductItem.PickedData(
                        code = it.key!!,
                        neededQty = (it.child("neededQty").value as Long).toInt(),
                        pickedQty = (it.child("pickedQty").value as Long?)?.toInt()
                    )
                }
            }

            val infoObservable = Observable.combineLatest(storesObservable,selectedProductsObervable,{ sto,selected ->
                Pair(sto,selected)
            })

            infoObservable.flatMap {
                allProductsObservable(it.first,it.second)
            }
            .map {list-> list.sortedBy { it.picked.hasPicked } }
            .subscribe {
                    data.value = it
            }
        }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }




}