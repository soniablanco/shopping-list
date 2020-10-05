package ga.piscos.shoppinglist.allproducts

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.transition.DrawableCrossFadeTransition
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.request.transition.TransitionFactory
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import ga.piscos.shoppinglist.R
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import kotlinx.android.synthetic.main.allproducts_all_products_fragment.*
import kotlinx.android.synthetic.main.allproducts_product_item.view.*
import java.util.concurrent.TimeUnit


class AllProductsFragment: Fragment() {


    companion object {
        fun newInstance(): AllProductsFragment? {
            return  AllProductsFragment()
        }


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.allproducts_all_products_fragment, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv_products_list.layoutManager = LinearLayoutManager(activity)
        rv_products_list.addItemDecoration(DividerItemDecoration(rv_products_list.context, DividerItemDecoration.VERTICAL))
        rv_products_list.setHasFixedSize(true)
        val adapter = ProductsListItemAdapter{
        }
        rv_products_list.adapter = adapter

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val products = dataSnapshot.children.map {
                    ProductItem(
                        code = it.key!!,
                        name = it.child("name").value.toString(),
                        aldiPhotoURL = it.child("stores/aldi/photoURL").value?.toString(),
                        lidPhotoURL = it.child("stores/lidl/photoURL").value?.toString()
                    )
                }
                adapter.updateProducts(products)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        Firebase.database.reference.child("allproducts").addValueEventListener(postListener)
    }
    class DrawableAlwaysCrossFadeFactory : TransitionFactory<Drawable> {
        private val resourceTransition: DrawableCrossFadeTransition = DrawableCrossFadeTransition(300, true) //customize to your own needs or apply a builder pattern
        override fun build(dataSource: DataSource?, isFirstResource: Boolean): Transition<Drawable> {
            return resourceTransition
        }
    }
    private inner class ProductsListItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(product: ProductItem, onclickListener: (ProductItem) -> Unit)= with(itemView){
            tvProductName.text = product.name.replace("++","").replace("**","")
            val images = listOf(product.aldiPhotoURL,product.lidPhotoURL).filter {  it!=null }.toList()
            Observable.interval(2,TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                val value = it % images.count()
                val imageUrl = images[value.toInt()]

                Glide.with(this)
                    .load(imageUrl)
                    .transition(DrawableTransitionOptions.with(DrawableAlwaysCrossFadeFactory()))
                    .into(imPhotoView)
                    Log.d(product.name,value.toString())
                Log.d(product.name,imageUrl)
            }


            setOnClickListener { onclickListener(product) }
        }
    }



    private inner class ProductsListItemAdapter(private var elements:MutableList<ProductItem> = arrayListOf(), val onclickListener: (ProductItem) -> Unit
    ) : RecyclerView.Adapter<ProductsListItemHolder>() {


        fun updateProducts(stockList:List<ProductItem>){
            elements.clear()
            elements.addAll(stockList)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ProductsListItemHolder {
            return ProductsListItemHolder(LayoutInflater.from(activity).inflate(R.layout.allproducts_product_item, viewGroup, false))
        }

        override fun onBindViewHolder(holder: ProductsListItemHolder, position: Int) = holder.bind(elements[position], onclickListener)

        override fun getItemCount(): Int {  return elements.size  }
    }


}