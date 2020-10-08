package ga.piscos.shoppinglist.product

import android.R.attr
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
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
    private val model by viewModels<ProductViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv_stores_list.layoutManager = LinearLayoutManager(activity)
        rv_stores_list.addItemDecoration(DividerItemDecoration(rv_stores_list.context, DividerItemDecoration.VERTICAL))
        rv_stores_list.setHasFixedSize(true)
        val adapter = StoresListItemAdapter{
        }
        rv_stores_list.adapter = adapter




        observe(model.data){
            adapter.updateProducts(it.template.stores)
            val sections = mutableListOf(ProductModel.Template.HouseSection("noselect","Select Section:"))
            sections.addAll(it.template.houseSections)
            val sectionsAdapter: ArrayAdapter<ProductModel.Template.HouseSection> =
                ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, sections)
            sectionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            house_spinner.adapter = sectionsAdapter
            dialogTextEdit.textChanges().subscribe { prodName ->
                model.updateProductName(prodName.toString())
            }
            house_spinner.itemSelections().subscribe {index->
                val code = if (index>0)  sections[index].code else null
                model.updateHouseSectionCode(code)
            }

        }
        model.loadData()

    }
    private inner class StoresListItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(store: ProductModel.Template.Store, onclickListener: (ProductModel.Template.Store) -> Unit)= with(itemView){



            val sections = mutableListOf(ProductModel.Template.Store.Section("noselect","Select Section:"))
            sections.addAll(store.sections)
            val adapter: ArrayAdapter<ProductModel.Template.Store.Section> =
                ArrayAdapter(activity!!, android.R.layout.simple_spinner_item, sections)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            spinner.itemSelections().subscribe {index->
                val code = if (index>0)  sections[index].code else null
                model.updateStoreSection(storeCode = store.code, sectionCode = code)
            }
            Glide.with(itemView)
                .load(store.logoURL)
                .into(imPhotoView)
            imPhotoView.setOnClickListener {
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (takePictureIntent.resolveActivity(activity!!.packageManager) != null) {
                    startActivityForResult(takePictureIntent, 45)
                    //selectedStore = product
                }
            }

            setOnClickListener {  }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 45 && resultCode == Activity.RESULT_OK) {
            val extras: Bundle = data!!.extras!!
            val imageBitmap = extras["data"] as Bitmap?
            //selectedStore!!.photoBMP = imageBitmap
            val adapter = rv_stores_list.adapter as ProductFragment.StoresListItemAdapter
            adapter.notifyDataSetChanged()
        }
    }

    private inner class StoresListItemAdapter(private var elements:MutableList<ProductModel.Template.Store> = arrayListOf(), val onclickListener: (ProductModel.Template.Store) -> Unit
    ) : RecyclerView.Adapter<StoresListItemHolder>() {


        fun updateProducts(stockList:List<ProductModel.Template.Store>){
            elements.clear()
            elements.addAll(stockList)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): StoresListItemHolder {
            return StoresListItemHolder(LayoutInflater.from(activity).inflate(R.layout.product_product_store_item, viewGroup, false))
        }

        override fun onBindViewHolder(holder: StoresListItemHolder, position: Int) = holder.bind(elements[position], onclickListener)

        override fun getItemCount(): Int {  return elements.size  }
    }

}