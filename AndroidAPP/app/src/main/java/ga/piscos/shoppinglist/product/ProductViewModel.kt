package ga.piscos.shoppinglist.product

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import ga.piscos.shoppinglist.observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.Observables
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.BiFunction
import java.util.*

class ProductViewModel (application: Application) : AndroidViewModel(application) {

    var disposables = CompositeDisposable()
    val data = MutableLiveData<ProductModel>()
    private  val editingModel = EditingProduct(productName = null,houseSection = null)
    fun updateProductName(value:String){
        editingModel.productName=value
    }
    fun updateHouseSectionCode(code: String?) {
        editingModel.houseSection = code
    }
    fun updateStoreSection(storeCode: String, sectionCode: String?) {
        val store = editingModel.stores.filter { it.code==storeCode }
        if (store.any()){
            store.first().section=sectionCode
        }
        else{
            editingModel.stores.add(
                EditingProductStore(
                    code = storeCode,
                    photoFile = null,
                    section = sectionCode
                )
            )
        }
    }
    fun loadData() {

        val storesObservable = Firebase.database.reference.child("stores")
            .observable()
            .map {
                it.children.map { storeSnapShot ->
                    TemplateStore(
                        code = storeSnapShot.key!!,
                        logoURL = storeSnapShot.child("photoURL").value.toString(),
                        sections = storeSnapShot.child("sections").children.map { sec ->
                            TemplateStoreSection(
                                code = sec.key!!,
                                name = sec.child("name").value.toString()
                            )
                        }
                    )
                }
            }

        val houseSectionsObservable = Firebase.database.reference.child("house/sections")
            .observable()
            .map {
                it.children.map { sec ->
                    TemplateHouseSection(
                        code = sec.key!!,
                        name = sec.child("name").value.toString()
                    )
                }
            }


        val templateObservable = Observable.combineLatest(
            storesObservable,
            houseSectionsObservable
        , BiFunction { stores:List<TemplateStore>, houseSections:List<TemplateHouseSection>->
            ProductTemplate(
                houseSections = houseSections,
                stores = stores
            )
        })


        templateObservable.subscribe {
            data.value = ProductModel(productTemplate = it,editingProduct = editingModel)
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }




}