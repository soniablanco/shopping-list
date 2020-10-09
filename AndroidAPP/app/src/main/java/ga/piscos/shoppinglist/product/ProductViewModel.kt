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
    private  val editingModel = ProductModel.Editing(name = null,houseSection = null,code = null)
    fun updateProductName(value:String){
        editingModel.name=value
    }
    fun updateHouseSectionCode(code: String?) {
        editingModel.houseSection = code
    }

    fun sync():Observable<Any> {


        val storageRef = Firebase.storage.reference
        editingModel.code = UUID.randomUUID().toString()
        val filesToUpload = editingModel.stores.filter { it.photoTakenURI != null }
        val photosStorageObservable = Observable.fromIterable(filesToUpload)
            .map {
                Pair(
                    first = it,
                    second = storageRef.child("allproducts/${editingModel.code}/stores/${it.code}/${it.photoTakenURI!!.lastPathSegment}")
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


    fun createImageFile(): Uri {

        val appFilesDir = (getApplication() as Context).filesDir
        val filesRoot = File(appFilesDir, "attachments")
        if (!filesRoot.exists()) {
            filesRoot.mkdirs()
        }
        val file = File(filesRoot, UUID.randomUUID().toString()+".jpg")



        return FileProvider.getUriForFile(
            getApplication(),
            BuildConfig.APPLICATION_ID +".fileprovider",
            file
        )
    }

    fun updateStoreSection(storeCode: String, sectionCode: String?) {
        val store = editingModel.stores.filter { it.code==storeCode }
        if (store.any()){
            store.first().section=sectionCode
        }
        else{
            editingModel.stores.add(
                ProductModel.Editing.Store(
                    code = storeCode,
                    photoTakenURI = null,
                    section = sectionCode
                )
            )
        }
    }

    fun updateStorePhoto(storeCode: String, photoUri: Uri?) {
        val store = editingModel.stores.filter { it.code==storeCode }
        if (store.any()){
            store.first().photoTakenURI=photoUri
        }
        else{
            editingModel.stores.add(
                ProductModel.Editing.Store(
                    code = storeCode,
                    photoTakenURI = photoUri,
                    section = null
                )
            )
        }
    }


    fun loadData() {

        val storesObservable = Firebase.database.reference.child("stores")
            .observable()
            .map {
                it.children.map { storeSnapShot ->
                    ProductModel.Template.Store(
                        code = storeSnapShot.key!!,
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
                        name = sec.child("name").value.toString()
                    )
                }
            }


        val templateObservable = Observable.combineLatest(
            storesObservable,
            houseSectionsObservable
        , BiFunction { stores:List<ProductModel.Template.Store>, houseSections:List<ProductModel.Template.HouseSection>->
            ProductModel.Template(
                houseSections = houseSections,
                stores = stores
            )
        })


        templateObservable.subscribe {
            data.value = ProductModel(template = it,editing = editingModel,saved = null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }




}