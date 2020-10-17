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
    val data= MutableLiveData<List<CollectionItemRow>>()
    val storesData= MutableLiveData<List<ProductItem.Store.Template>>()


        fun loadData(){


            val storesObservable = Firebase.database.reference.child("stores")
                .observable()
                .map {
                    it.children.map { storeSnapShot ->
                        ProductItem.Store.Template(
                            code = storeSnapShot.key!!,
                            name = storeSnapShot.child("name").value.toString(),
                            logoURL = storeSnapShot.child("photoURL").value.toString(),
                            sections = storeSnapShot.child("sections").children.map { seR-> ProductItem.Store.Template.Section(
                                code = seR.key!!,
                                name = seR.child("name").value.toString(),
                                index = (seR.child("index").value!! as Long).toInt()
                            ) }.sortedBy { section -> section.index }
                        )
                    }
                }.share()

            val selectedStoreCodeObservable = Observable.just("aldi")
            val selectedStoreObservable = Observable.combineLatest(storesObservable,selectedStoreCodeObservable,{ sto,selectedStoreCode ->
                sto.first { it.code == selectedStoreCode }
            })

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
                                    sectionCode = stRef.child("section").value?.toString(),
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

            val productListObservable = infoObservable.flatMap {
                allProductsObservable(it.first,it.second)
            }







            val resultObservable = Observable.combineLatest(
                selectedStoreObservable,
                productListObservable,
                { selectedStore:ProductItem.Store.Template, products:List<ProductItem> ->
                    val storeSections =selectedStore.sections.map { ss-> StoreSection(code = ss.code,name = ss.name, index = ss.index)}
                    storeSections.forEach {hs-> hs.products =
                        products
                            .filter { p-> p.stores.any   { store -> store.code==selectedStore.code }}
                            .filter { p-> p.stores.first { store -> store.code==selectedStore.code }.sectionCode!=null }
                            .filter { p-> p.stores.first { store -> store.code==selectedStore.code }.sectionCode!! == hs.code }

                    }
                    val productsWithSections = mutableListOf<ProductItem>()
                    storeSections.forEach { hs->productsWithSections.addAll(hs.products!!)}
                    val productsWithNoSection = products.filter { !productsWithSections.contains(it) }
                    val rows = mutableListOf<CollectionItemRow>()
                    if (productsWithNoSection.count()>0){
                        val houseSectionNotEntered = StoreSection(code = "unknown",name = "Need to Assign Section",index = -1,products = productsWithNoSection)
                        houseSectionNotEntered.assignSection()
                        rows.addAll(houseSectionNotEntered.getAllRows())
                    }
                    storeSections.forEach { hs-> rows.addAll(hs.getAllRows()) }
                    storeSections.forEach { hs-> hs.assignSection() }
                    rows
                })


            resultObservable
            .subscribe {
                    data.value = it
            }

            storesObservable.subscribe {
                storesData.value = it
            }
        }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }




}