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
import ga.piscos.shoppinglist.R
import ga.piscos.shoppinglist.allproducts.AllProductsViewModel
import ga.piscos.shoppinglist.observable
import ga.piscos.shoppinglist.observe
import ga.piscos.shoppinglist.plus
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.functions.BiFunction
import io.reactivex.rxjava3.kotlin.Observables
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.android.synthetic.main.product_product_layout.*
import kotlinx.android.synthetic.main.product_product_store_item.view.*


class ProductFragment : Fragment() {

    companion object {
        fun newInstance(): ProductFragment {
            return ProductFragment()
        }


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.product_product_layout, container, false)

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv_stores_list.layoutManager = LinearLayoutManager(activity)
        rv_stores_list.addItemDecoration(DividerItemDecoration(rv_stores_list.context, DividerItemDecoration.VERTICAL))
        rv_stores_list.setHasFixedSize(true)
        val adapter = ProductsListItemAdapter{
        }
        rv_stores_list.adapter = adapter



        val model by viewModels<ProductViewModel>()
        observe(model.data){
            adapter.updateProducts(it.stores)
            val sections = mutableListOf(TemplateHouseSection("noselect","Select Section:"))
            sections.addAll(it.houseSections)
            val sectionsAdapter: ArrayAdapter<TemplateHouseSection> =
                ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, sections)
            sectionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            house_spinner.adapter = sectionsAdapter
        }
        model.loadData()

    }
    private inner class ProductsListItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(store: TemplateStore, onclickListener: (TemplateStoreSection) -> Unit)= with(itemView){



            val sections = mutableListOf(TemplateStoreSection("noselect","Select Section:"))
            sections.addAll(store.sections)
            val adapter: ArrayAdapter<TemplateStoreSection> =
                ArrayAdapter(activity!!, android.R.layout.simple_spinner_item, sections)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter

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
    private var selectedStore:TemplateStoreSection? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 45 && resultCode == Activity.RESULT_OK) {
            val extras: Bundle = data!!.extras!!
            val imageBitmap = extras["data"] as Bitmap?
            //selectedStore!!.photoBMP = imageBitmap
            val adapter = rv_stores_list.adapter as ProductFragment.ProductsListItemAdapter
            adapter.notifyDataSetChanged()
        }
    }

    private inner class ProductsListItemAdapter(private var elements:MutableList<TemplateStore> = arrayListOf(), val onclickListener: (TemplateStoreSection) -> Unit
    ) : RecyclerView.Adapter<ProductsListItemHolder>() {


        fun updateProducts(stockList:List<TemplateStore>){
            elements.clear()
            elements.addAll(stockList)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ProductsListItemHolder {
            return ProductsListItemHolder(LayoutInflater.from(activity).inflate(R.layout.product_product_store_item, viewGroup, false))
        }

        override fun onBindViewHolder(holder: ProductsListItemHolder, position: Int) = holder.bind(elements[position], onclickListener)

        override fun getItemCount(): Int {  return elements.size  }
    }

}