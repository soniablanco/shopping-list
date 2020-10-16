package ga.piscos.shoppinglist.allproducts

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
import com.shuhart.stickyheader.StickyAdapter
import com.shuhart.stickyheader.StickyHeaderItemDecorator
import ga.piscos.shoppinglist.R
import ga.piscos.shoppinglist.observe
import ga.piscos.shoppinglist.plus
import ga.piscos.shoppinglist.product.ProductActivity
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.allproducts_all_products_fragment.*
import kotlinx.android.synthetic.main.allproducts_housesection_header.view.*
import kotlinx.android.synthetic.main.allproducts_product_item.view.*


class AllProductsFragment: Fragment() {


    companion object {
        fun newInstance(): AllProductsFragment? {
            return  AllProductsFragment()
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.allproducts_all_products_fragment, container, false)
    }


    var itemDisposables = CompositeDisposable()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv_products_list.layoutManager = LinearLayoutManager(activity)
        rv_products_list.addItemDecoration(
            DividerItemDecoration(
                rv_products_list.context,
                DividerItemDecoration.VERTICAL
            )
        )
        rv_products_list.setHasFixedSize(true)
        val adapter = ProductsListItemAdapter{
            val productItem = it as ProductItem?
            if (productItem!=null) {
                val intent = ProductActivity.newIntent(requireActivity(), it.code)
                startActivityForResult(intent, 323)
            }
        }
        val decorator = StickyHeaderItemDecorator(adapter)
        decorator.attachToRecyclerView(rv_products_list)
        rv_products_list.adapter = adapter

        fabAddProduct.setOnClickListener {
            val intent = ProductActivity.newIntent(requireActivity(), null)
            startActivityForResult(intent, 323)
        }
        val model by viewModels<AllProductsViewModel>()
        observe(model.data){
            adapter.updateElements(it)
        }

    }

    override fun onResume() {
        super.onResume()
        val model by viewModels<AllProductsViewModel>()
        model.loadData()
    }

    override fun onPause() {
        super.onPause()
        itemDisposables.clear()
        viewsObservable.clear()
    }

    class DrawableAlwaysCrossFadeFactory : TransitionFactory<Drawable> {
        private val resourceTransition: DrawableCrossFadeTransition = DrawableCrossFadeTransition(
            300,
            true
        ) //customize to your own needs or apply a builder pattern
        override fun build(dataSource: DataSource?, isFirstResource: Boolean): Transition<Drawable> {
            return resourceTransition
        }
    }

    private val viewsObservable = hashMapOf<View, Disposable>()
    interface AllProductsItemRowHolder{//holder
    fun bind(item: AllProductItemRow, listener: (AllProductItemRow) -> Unit)
    }
    private inner class ProductsListItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView),AllProductsItemRowHolder {
        override fun bind(item: AllProductItemRow, listener: (AllProductItemRow) -> Unit)= with(
            itemView
        ){
            val product = item as ProductItem
            tvProductName.text = product.name
            val prevObservable = viewsObservable[itemView]
            if (prevObservable!=null) {
                itemDisposables.remove(prevObservable)
                viewsObservable.remove(itemView)
            }
            val disposable = product.getPhotoChangeObservable(1).subscribe {

                Glide.with(this)
                    .load(it.photoURL)
                    .transition(DrawableTransitionOptions.with(DrawableAlwaysCrossFadeFactory()))
                    .into(imPhotoView)

                imPhotoViewLogo.alpha = 0.7F
                Glide.with(itemView)
                    .load(it.logoURL)
                    .into(imPhotoViewLogo)
            }
            itemDisposables +=disposable
            viewsObservable[itemView] = disposable
            setOnClickListener { listener(item) }
        }
    }


    private inner class HouseSectionItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView),AllProductsItemRowHolder {
        override fun bind(item: AllProductItemRow, listener: (AllProductItemRow) -> Unit)= with(
            itemView
        ){
            val houseSection = item as HouseSection
            tvAllProductsHouseSection.text = "\uD83C\uDFE0 ${houseSection.name}"
            setOnClickListener { listener(item) }
        }
    }



    private inner class ProductsListItemAdapter(
        private var elements: MutableList<AllProductItemRow> = arrayListOf(),
        val listener: (AllProductItemRow) -> Unit
    ) : StickyAdapter<RecyclerView.ViewHolder, RecyclerView.ViewHolder>() {


        fun updateElements(stockList: List<AllProductItemRow>){
            itemDisposables.clear()
            viewsObservable.clear()
            elements.clear()
            elements.addAll(stockList)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

            return when(viewType){
                AllProductItemRow.Type.Item.intValue -> ProductsListItemHolder(
                    LayoutInflater.from(
                        activity
                    ).inflate(R.layout.allproducts_product_item, viewGroup, false)
                )
                else ->HouseSectionItemHolder(
                    LayoutInflater.from(activity).inflate(
                        R.layout.allproducts_housesection_header,
                        viewGroup,
                        false
                    )
                )
            }
        }
        override fun getItemViewType(position: Int)=elements[position].type.intValue
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = (holder as AllProductsItemRowHolder).bind(
            elements[position],
            listener
        )

        override fun getItemCount(): Int {  return elements.size  }
        override fun getHeaderPositionForItem(itemPosition: Int): Int {
            val product = elements[itemPosition] as? ProductItem
            val section = if (product!= null){
                product.houseSectionInstance!!
            } else{
                elements[itemPosition] as HouseSection
            }
            return elements.indexOf(section)
        }

        override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
            (holder as AllProductsItemRowHolder).bind(elements[position], listener)
        }

        override fun onCreateHeaderViewHolder(viewGroup: ViewGroup?): RecyclerView.ViewHolder {
           return HouseSectionItemHolder(
               LayoutInflater.from(activity).inflate(
                   R.layout.allproducts_housesection_header,
                   viewGroup,
                   false
               )
           )
        }
    }


}




