package ga.piscos.shoppinglist.collection

import android.app.Application
import android.util.Log
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


        fun loadData(selectedStoreIndexObservable:Observable<Int>){


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
            val selectedStoreObservable = Observable.combineLatest(storesObservable,selectedStoreIndexObservable,{ sto,selectedStoreIndex ->
                val indexToUse =  if (selectedStoreIndex<0) 0 else selectedStoreIndex
                sto[indexToUse]
            }).distinct()

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
                    val rows = mutableListOf<CollectionItemRow>()
                    addRows(finishedProducts = false, selectedStore= selectedStore,products=products.filter { !it.picked.hasPicked },rows=rows)
                    addRows(finishedProducts = true, selectedStore= selectedStore,products=products.filter { it.picked.hasPicked },rows=rows)
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
    private fun addRows(finishedProducts:Boolean, selectedStore:ProductItem.Store.Template, products:List<ProductItem>, rows:MutableList<CollectionItemRow>){
        val sufix = if (finishedProducts) " ✔" else ""
        val storeSections =selectedStore.sections.map { ss-> StoreSection(finishedSection = finishedProducts, code = ss.code,name = ss.name + sufix, index = ss.index)}
        storeSections.forEach {hs-> hs.products =
            products
                .filter { p-> p.stores.any   { store -> store.code==selectedStore.code }}
                .filter { p-> p.stores.first { store -> store.code==selectedStore.code }.sectionCode!=null }
                .filter { p-> p.stores.first { store -> store.code==selectedStore.code }.sectionCode!! == hs.code }

        }
        val productsWithSections = mutableListOf<ProductItem>()
        storeSections.forEach { hs->productsWithSections.addAll(hs.products!!)}
        val productsWithNoSection = products.filter { !productsWithSections.contains(it) }
        if (productsWithNoSection.count()>0){
            val houseSectionNotEntered = StoreSection(finishedSection = finishedProducts, code = "unknown",name = "No Section$sufix",index = -1,products = productsWithNoSection)
            houseSectionNotEntered.assignSection()
            rows.addAll(houseSectionNotEntered.getAllRows())
        }
        storeSections.forEach { hs-> rows.addAll(hs.getAllRows()) }
        storeSections.forEach { hs-> hs.assignSection() }
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }




}