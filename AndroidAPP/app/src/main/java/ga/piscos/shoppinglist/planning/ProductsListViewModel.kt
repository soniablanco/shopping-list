package ga.piscos.shoppinglist.planning

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import ga.piscos.shoppinglist.observable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable

class ProductsListViewModel(application: Application) : AndroidViewModel(application){

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

            val allProductsObservable = { stores:List<ProductItem.Store.Template>, selectedProducts:List<ProductItem.SelecteData> ->
                Firebase.database.reference.child("allproducts").observable().map { dataSnapshot ->
                   // dataSnapshot.children.filter {sna-> selectedProducts.any { s->s.code==sna.key!! }  }
                    dataSnapshot.children.map {
                        ProductItem(
                            code = it.key!!,
                            name = it.child("name").value.toString(),
                            selectedData  = selectedProducts.firstOrNull {s-> s.code==it.key!! },
                            houseSection = it.child("houseSection").value?.toString(),
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

            val selectedProductsObervable = Firebase.database.reference.child("lists/current/products").observable().map { dataSnapshot ->
                dataSnapshot.children.map {
                    ProductItem.SelecteData(
                        code = it.key!!,
                        neededQty = (it.child("planning/neededQty").value as Long).toInt(),
                        addedTimeStamp = (it.child("planning/addedTimeStamp").value as Long).toInt()
                    )
                }
            }

            val infoObservable = Observable.combineLatest(storesObservable,selectedProductsObervable,{ sto,selected ->
                Pair(sto,selected)
            })

            val resultingProductsObservable = infoObservable.flatMap {
                allProductsObservable(it.first,it.second)
            }


            val resultObservable = Observable.combineLatest(
                houseSectionsObservable,
                resultingProductsObservable,
                { houseSections:List<HouseSection>, products:List<ProductItem> ->
                    houseSections.forEach {hs-> hs.products = products.filter {p->p.houseSection!=null && p.houseSection==hs.code } }
                    val productsWithSections = mutableListOf<ProductItem>()
                    houseSections.forEach { hs->productsWithSections.addAll(hs.products!!)}
                    val productsWithNoSection = products.filter { !productsWithSections.contains(it) }
                    val rows = mutableListOf<AllProductItemRow>()
                    if (productsWithNoSection.count()>0){
                        val houseSectionNotEntered = HouseSection(code = "unknown",name = "Need to Assign Section",index = -1,products = productsWithNoSection)
                        houseSectionNotEntered.assignSection()
                        rows.addAll(houseSectionNotEntered.getAllRows())
                    }
                    houseSections.forEach { hs-> rows.addAll(hs.getAllRows()) }
                    houseSections.forEach { hs-> hs.assignSection() }
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