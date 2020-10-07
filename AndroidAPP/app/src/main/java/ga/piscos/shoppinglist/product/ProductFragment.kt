package ga.piscos.shoppinglist.product

import android.R.attr
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
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
import ga.piscos.shoppinglist.observable
import ga.piscos.shoppinglist.plus
import io.reactivex.rxjava3.disposables.CompositeDisposable
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

        disposables += Firebase.database.reference.child("stores").observable().subscribe { dataSnapshot->
            val products = dataSnapshot.children.map {
                ProductStoreItem(
                    code = it.key!!,
                    name = it.child("name").value.toString(),
                    photoURL = it.child("photoURL").value.toString(),
                    sections = it.child("sections").children.map {sec-> ProductStoreSection(code = sec.key!!,name = sec.child("name").value.toString()) }
                )
            }
            adapter.updateProducts(products)
        }
    }

    var disposables = CompositeDisposable()


    override fun onPause() {
        super.onPause()
         disposables.clear()
    }
    private inner class ProductsListItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(product: ProductStoreItem, onclickListener: (ProductStoreItem) -> Unit)= with(itemView){



            val sections = mutableListOf(ProductStoreSection("noselect","Select Section:"))
            sections.addAll(product.sections)
            val adapter: ArrayAdapter<ProductStoreSection> =
                ArrayAdapter(activity!!, android.R.layout.simple_spinner_item, sections)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter

            if (product.photoBMP==null) {
                imPhotoViewLogo.visibility = View.GONE
                imPhotoView.alpha=0.4F
                Glide.with(itemView)
                    .load(product.photoURL)
                    .into(imPhotoView)
            }
            else{
                imPhotoViewLogo.visibility = View.VISIBLE
                imPhotoView.alpha=1F
                Glide.with(itemView)
                    .load(product.photoBMP!!)
                    .into(imPhotoView)
                Glide.with(itemView)
                    .load(product.photoURL)
                    .into(imPhotoViewLogo)
            }

            imPhotoView.setOnClickListener {
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (takePictureIntent.resolveActivity(activity!!.packageManager) != null) {
                    startActivityForResult(takePictureIntent, 45)
                    selectedStore = product
                }
            }

            setOnClickListener { onclickListener(product) }
        }
    }
    private var selectedStore:ProductStoreItem? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 45 && resultCode == Activity.RESULT_OK) {
            val extras: Bundle = data!!.extras!!
            val imageBitmap = extras["data"] as Bitmap?
            selectedStore!!.photoBMP = imageBitmap
            val adapter = rv_stores_list.adapter as ProductFragment.ProductsListItemAdapter
            adapter.notifyDataSetChanged()
        }
    }

    private inner class ProductsListItemAdapter(private var elements:MutableList<ProductStoreItem> = arrayListOf(), val onclickListener: (ProductStoreItem) -> Unit
    ) : RecyclerView.Adapter<ProductsListItemHolder>() {


        fun updateProducts(stockList:List<ProductStoreItem>){
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