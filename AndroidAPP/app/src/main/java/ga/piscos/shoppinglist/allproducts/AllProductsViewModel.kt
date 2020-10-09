package ga.piscos.shoppinglist.allproducts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import ga.piscos.shoppinglist.observable
import ga.piscos.shoppinglist.plus
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.allproducts_all_products_fragment.*

class AllProductsViewModel(application: Application) : AndroidViewModel(application){

    var disposables = CompositeDisposable()
    val data= MutableLiveData<List<ProductItem>>()

        fun loadData(){
            disposables += Firebase.database.reference.child("allproducts").observable().subscribe { dataSnapshot->
                val products = dataSnapshot.children.map {
                    ProductItem(
                        code = it.key!!,
                        name = it.child("name").value.toString(),
                        stores = it.child("stores").children.map {stRef -> ProductItem.Store(code = stRef.key!!, photoURL = stRef.child("photoURL").value.toString()) }
                    )
                }
                data.value = products
            }
        }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }




}