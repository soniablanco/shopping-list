package ga.piscos.shoppinglist.product

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import ga.piscos.shoppinglist.BuildConfig
import ga.piscos.shoppinglist.observable
import ga.piscos.shoppinglist.uploadObservable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.functions.BiFunction
import io.reactivex.rxjava3.internal.operators.observable.ObservableAny
import java.io.File
import java.util.*
import kotlin.math.log


class ProductViewModel (application: Application) : AndroidViewModel(application) {

    var disposables = CompositeDisposable()
    val data = MutableLiveData<ProductModel>()
    fun updateProductName(value:String){
        editingModel.name=value
    }
    fun updateHouseSectionCode(code: String?) {
        editingModel.houseSection = code
    }
    private  val editingModel get() = data.value!!.editing
    fun sync():Observable<Any> {


        val storageRef = Firebase.storage.reference
        val filesToUpload = editingModel.stores.filter { it.photoTakenURI != null }
        val photosStorageObservable = Observable.fromIterable(filesToUpload)
            .map {
                Pair(
                    first = it,
                    second = storageRef.child("v1/allproducts/${editingModel.code}/stores/${it.code}/photo.jpg")
                )
            }
            .flatMap { pair ->
                pair.second.uploadObservable(pair.first.photoTakenURI!!)
                    .map { pair }
            }
            .flatMap { pair ->
                pair.second.downloadUrl.observable().map { Pair(first = pair.first, second = it) }
            }
            .doOnNext {
                it.first.photoFirebaseUrl = it.second.toString()
            }
        val firebaseDatabaseObservable =
            Observable.just(1).flatMap { Observable.just(editingModel.getFirebaseEditingNode()) }
            .flatMap {Firebase.database.reference.child("allproducts").updateChildren(it).observable()}

       return Observable.concat(photosStorageObservable, firebaseDatabaseObservable).last(1).toObservable()


    }


    fun convertToUri(file: File):Uri{
        return FileProvider.getUriForFile(
            getApplication(),
            BuildConfig.APPLICATION_ID +".fileprovider",
            file
        )
    }
    fun createImageFile(): File {

        val appFilesDir = (getApplication() as Context).filesDir
        val filesRoot = File(appFilesDir, "attachments")
        if (!filesRoot.exists()) {
            filesRoot.mkdirs()
        }
        return File(filesRoot, UUID.randomUUID().toString()+".jpg")




    }

    fun updateStoreSection(storeCode: String, sectionCode: String?) {
        val store = editingModel.stores.filter { it.code==storeCode }
        store.first().section=sectionCode
    }

    fun updateStorePhoto(storeCode: String, photoUri: Uri?) {
        val store = editingModel.stores.filter { it.code==storeCode }
        store.first().photoTakenURI=photoUri
    }

    fun loadData(productId: String?){
        getLoadDataObservable(productId).subscribe {
            data.value = it
        }
    }

    private fun getLoadDataObservable(productId:String?):Observable<ProductModel> {

        val storesObservable = Firebase.database.reference.child("stores")
            .observable()
            .map {
                it.children.map { storeSnapShot ->
                    ProductModel.Template.Store(
                        code = storeSnapShot.key!!,
                        index = (storeSnapShot.child("index").value as Long).toInt(),
                        logoURL = storeSnapShot.child("photoURL").value.toString(),
                        sections = storeSnapShot.child("sections").children.map { sec ->
                            ProductModel.Template.Store.Section(
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
                    ProductModel.Template.HouseSection(
                        code = sec.key!!,
                        name = sec.child("name").value.toString(),
                        index = (sec.child("index").value as Long).toInt()
                    )
                }.sortedBy {s-> s.index  }
            }


        if (productId!=null) {

            val savedObservable =
                Firebase.database.reference.child("allproducts/${productId}")
                    .observable()
                    .map { productSnap ->
                        ProductModel.Saved(
                            code = productSnap.key!!,
                            name = productSnap.child("name").value?.toString(),
                            houseSection = productSnap.child("houseSection").value?.toString(),
                            stores = productSnap.child("stores").children.map { sto ->
                                ProductModel.Saved.Store(
                                    code = sto.key!!,
                                    section = sto.child("section").value?.toString(),
                                    photoURL = sto.child("photoURL").value?.toString()
                                )
                            }
                        )
                    }
            return Observable.combineLatest(
                storesObservable,
                houseSectionsObservable,
                savedObservable,
                { templateStores: List<ProductModel.Template.Store>, houseSections: List<ProductModel.Template.HouseSection>, saved ->

                    ProductModel(template = ProductModel.Template(
                        houseSections = houseSections,
                        stores = templateStores
                    ), editing = ProductModel.Editing(
                        code = saved.code,
                        name = saved.name,
                        houseSection = saved.houseSection,
                        stores =
                        templateStores.map { templateStore ->
                            ProductModel.Editing.Store(code = templateStore.code,
                                section = saved.stores.firstOrNull { savedStore -> savedStore.code == templateStore.code }?.section,
                                photoFirebaseUrl = saved.stores.firstOrNull { savedStore -> savedStore.code == templateStore.code }?.photoURL
                            )
                        }), saved = saved
                    )

                })
        }
        else{
            return Observable.combineLatest(
                storesObservable,
                houseSectionsObservable,
                { templateStores: List<ProductModel.Template.Store>, houseSections: List<ProductModel.Template.HouseSection>->

                    ProductModel(template = ProductModel.Template(
                        houseSections = houseSections,
                        stores = templateStores
                    ), editing = ProductModel.Editing(
                        code = UUID.randomUUID().toString(),
                        name = null,
                        houseSection = null,
                        stores =
                        templateStores.map { templateStore ->
                            ProductModel.Editing.Store(code = templateStore.code,
                                section = null
                            )
                        }), saved = null
                    )

                })
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }




}