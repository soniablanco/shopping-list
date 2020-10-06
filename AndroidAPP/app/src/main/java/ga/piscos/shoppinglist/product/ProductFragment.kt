package ga.piscos.shoppinglist.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
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
    }

    private val postListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val products = dataSnapshot.children.map {
                ProductStoreItem(
                    code = it.key!!,
                    name = it.child("name").value.toString(),
                    photoURL = it.child("photoURL").value.toString(),
                    sections = it.child("sections").children.map {sec-> ProductStoreSection(code = sec.key!!,name = sec.child("name").value.toString()) }
                )
            }
            val adapter = rv_stores_list.adapter as ProductFragment.ProductsListItemAdapter
            adapter.updateProducts(products)
        }

        override fun onCancelled(databaseError: DatabaseError) {
        }
    }
    override fun onResume() {
        super.onResume()
        Firebase.database.reference.child("stores").addValueEventListener(postListener)
    }

    override fun onPause() {
        super.onPause()
        Firebase.database.reference.child("stores").removeEventListener(postListener)
    }
    private inner class ProductsListItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(product: ProductStoreItem, onclickListener: (ProductStoreItem) -> Unit)= with(itemView){



            val sections = mutableListOf(ProductStoreSection("noselect","Select Section:"))
            sections.addAll(product.sections)
            val adapter: ArrayAdapter<ProductStoreSection> =
                ArrayAdapter(activity!!, android.R.layout.simple_spinner_item, sections)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter


            Glide.with(this)
                .load(product.photoURL)
                .into(imPhotoView)
            setOnClickListener { onclickListener(product) }
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