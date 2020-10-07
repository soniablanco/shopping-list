package ga.piscos.shoppinglist.product

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import ga.piscos.shoppinglist.observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.Observables

class ProductViewModel (application: Application) : AndroidViewModel(application) {

    var disposables = CompositeDisposable()
    val data = MutableLiveData<ProductTemplate>()

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


        val templateObservable = Observables.combineLatest<List<TemplateStore>,List<TemplateHouseSection>,ProductTemplate>(
            storesObservable,
            houseSectionsObservable
        ) { stores, houseSections->
            ProductTemplate(
                houseSections = houseSections,
                stores = stores
            )
        }

        templateObservable.subscribe {
            data.value = it
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

}