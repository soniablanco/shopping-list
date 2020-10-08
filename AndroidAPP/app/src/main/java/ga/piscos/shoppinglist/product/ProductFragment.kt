package ga.piscos.shoppinglist.product

import android.R.attr
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jakewharton.rxbinding4.view.changeEvents
import com.jakewharton.rxbinding4.widget.itemSelections
import com.jakewharton.rxbinding4.widget.textChanges
import ga.piscos.shoppinglist.R
import ga.piscos.shoppinglist.allproducts.AllProductsViewModel
import ga.piscos.shoppinglist.observable
import ga.piscos.shoppinglist.observe
import ga.piscos.shoppinglist.plus
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.functions.BiFunction
import io.reactivex.rxjava3.kotlin.Observables
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.android.synthetic.main.product_product_layout.*
import kotlinx.android.synthetic.main.product_product_store_item.view.*
import java.util.*


class ProductFragment : Fragment() {

    companion object {
        fun newInstance(): ProductFragment {
            return ProductFragment()
        }


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.product_product_layout, container, false)

    }
    private val viewModel by viewModels<ProductViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv_stores_list.layoutManager = LinearLayoutManager(activity)
        rv_stores_list.addItemDecoration(DividerItemDecoration(rv_stores_list.context, DividerItemDecoration.VERTICAL))
        rv_stores_list.setHasFixedSize(true)
        val storesAdapter = StoresListItemAdapter{
        }
        rv_stores_list.adapter = storesAdapter




        observe(viewModel.data){ productModel ->

            val sections = mutableListOf(ProductModel.Template.HouseSection("noselect","Select Section:"))
            sections.addAll(productModel.template.houseSections)
            val sectionsAdapter: ArrayAdapter<ProductModel.Template.HouseSection> =
                ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, sections)
            sectionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            house_spinner.adapter = sectionsAdapter

            val sectionIndex = productModel.getEditingHouseSectionIndex()
            house_spinner.setSelection( if (sectionIndex==null) 0 else sectionIndex+1)
            house_spinner.itemSelections().subscribe {index->
                val code = if (index>0)  sections[index].code else null
                viewModel.updateHouseSectionCode(code)
            }

            dialogTextEdit.setText(productModel.editing.name?:"")
            dialogTextEdit.textChanges().subscribe { prodName ->
                viewModel.updateProductName(prodName.toString())
            }

            storesAdapter.updateElements(productModel.getStoresModel())

        }
        viewModel.loadData()

    }

    private var storeCode:String? = null
    private var storePhotoUri: Uri? = null
    val getContent = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        viewModel.updateStorePhoto(storeCode!!,storePhotoUri!!)
        storeCode = null
        storePhotoUri = null
        (rv_stores_list.adapter as StoresListItemAdapter).updateElements(viewModel.data.value!!.getStoresModel())
    }
    private inner class StoresListItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(store: ProductStoreModel, onclickListener: (ProductModel.Template.Store) -> Unit)= with(itemView){



            val sections = mutableListOf(ProductModel.Template.Store.Section("noselect","Select Section:"))
            sections.addAll(store.template.sections)
            val adapter: ArrayAdapter<ProductModel.Template.Store.Section> =
                ArrayAdapter(activity!!, android.R.layout.simple_spinner_item, sections)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            val sectionIndex = store.getEditingSectionIndex()
            spinner.setSelection( if (sectionIndex==null) 0 else sectionIndex+1)
            spinner.itemSelections().subscribe {index->
                val code = if (index>0)  sections[index].code else null
                viewModel.updateStoreSection(storeCode = store.template.code, sectionCode = code)
            }
            if (store.editing?.photoURI!=null){
                imPhotoView.alpha = 1F
                Glide.with(itemView)
                    .load(store.editing!!.photoURI!!)
                    .into(imPhotoView)
                imPhotoViewLogo.alpha = 0.7F
                Glide.with(itemView)
                    .load(store.template.logoURL)
                    .into(imPhotoViewLogo)
            }
            else {
                imPhotoView.alpha = 0.5F
                Glide.with(itemView)
                    .load(store.template.logoURL)
                    .into(imPhotoView)
            }

            imPhotoView.setOnClickListener {
                val uri = viewModel.createImageFile()
                storePhotoUri  = uri
                storeCode = store.template.code
                getContent.launch(uri)
            }

            setOnClickListener {  }
        }
    }


    private inner class StoresListItemAdapter(private var elements:MutableList<ProductStoreModel> = arrayListOf(), val onclickListener: (ProductModel.Template.Store) -> Unit
    ) : RecyclerView.Adapter<StoresListItemHolder>() {


        fun updateElements(stores:List<ProductStoreModel>){
            elements.clear()
            elements.addAll(stores)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): StoresListItemHolder {
            return StoresListItemHolder(LayoutInflater.from(activity).inflate(R.layout.product_product_store_item, viewGroup, false))
        }

        override fun onBindViewHolder(holder: StoresListItemHolder, position: Int) = holder.bind(elements[position], onclickListener)

        override fun getItemCount(): Int {  return elements.size  }
    }

}