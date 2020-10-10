package ga.piscos.shoppinglist.collection

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.transition.DrawableCrossFadeTransition
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.request.transition.TransitionFactory
import ga.piscos.shoppinglist.R
import ga.piscos.shoppinglist.observe
import ga.piscos.shoppinglist.plus
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.collection_product_item.view.*
import kotlinx.android.synthetic.main.collection_products_list_fragment.*

class ProductListFragment: Fragment() {


    companion object {
        fun newInstance(): ProductListFragment {
            return  ProductListFragment()
        }


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.collection_products_list_fragment, container, false)
    }


    var itemDisposables = CompositeDisposable()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv_collection_products_list.layoutManager = LinearLayoutManager(activity)
        rv_collection_products_list.addItemDecoration(DividerItemDecoration(rv_collection_products_list.context, DividerItemDecoration.VERTICAL))
        rv_collection_products_list.setHasFixedSize(true)
        val adapter = ProductsListItemAdapter{
            it.selectItem()
        }
        rv_collection_products_list.adapter = adapter

        val model by viewModels<ProductsListViewModel>()
        observe(model.data){
            adapter.updateProducts(it)
        }

    }

    override fun onResume() {
        super.onResume()
        val model by viewModels<ProductsListViewModel>()
        model.loadData()
    }

    override fun onPause() {
        super.onPause()
        itemDisposables.clear()
        viewsObservable.clear()
    }

    class DrawableAlwaysCrossFadeFactory : TransitionFactory<Drawable> {
        private val resourceTransition: DrawableCrossFadeTransition = DrawableCrossFadeTransition(300, true) //customize to your own needs or apply a builder pattern
        override fun build(dataSource: DataSource?, isFirstResource: Boolean): Transition<Drawable> {
            return resourceTransition
        }
    }

    private val viewsObservable = hashMapOf<View, Disposable>()
    private inner class ProductsListItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(index:Int, product: ProductItem, onclickListener: (ProductItem) -> Unit)= with(itemView){
            tvCollectionProductProductName.text = product.name
            tvCollectionProductQty.text = "x${product.picked.neededQty}"
            if (product.picked.pickedQty!=null){
                imCollectionRemove.visibility=View.VISIBLE
                imCollectionRemove.setOnClickListener { product.unSelect()  }
            }
            else{
                imCollectionRemove.visibility=View.GONE
                imCollectionRemove.setOnClickListener {}
            }
            val prevObservable = viewsObservable[itemView]
            if (prevObservable!=null) {
                itemDisposables.remove(prevObservable)
                viewsObservable.remove(itemView)
            }
            val disposable = product.getPhotoChangeObservable(index).subscribe {

                Glide.with(this)
                    .load(it.photoURL)
                    .transition(DrawableTransitionOptions.with(DrawableAlwaysCrossFadeFactory()))
                    .into(imCollectionProductPhotoView)

                imCollectionProductPhotoViewLogo.alpha = 0.7F
                Glide.with(itemView)
                    .load(it.logoURL)
                    .into(imCollectionProductPhotoViewLogo)
            }
            itemDisposables +=disposable
            viewsObservable[itemView] = disposable
            setOnClickListener { onclickListener(product) }
        }
    }



    private inner class ProductsListItemAdapter(private var elements:MutableList<ProductItem> = arrayListOf(), val onclickListener: (ProductItem) -> Unit
    ) : RecyclerView.Adapter<ProductsListItemHolder>() {


        fun updateProducts(stockList:List<ProductItem>){
            itemDisposables.clear()
            viewsObservable.clear()
            elements.clear()
            elements.addAll(stockList)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ProductsListItemHolder {
            return ProductsListItemHolder(LayoutInflater.from(activity).inflate(R.layout.collection_product_item, viewGroup, false))
        }

        override fun onBindViewHolder(holder: ProductsListItemHolder, position: Int) = holder.bind(position,elements[position], onclickListener)

        override fun getItemCount(): Int {  return elements.size  }
    }


}