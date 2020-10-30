package ga.piscos.shoppinglist.collection

import android.content.DialogInterface
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
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
import com.jakewharton.rxbinding4.widget.itemSelections
import ga.piscos.shoppinglist.R
import ga.piscos.shoppinglist.observe
import ga.piscos.shoppinglist.plus
import ga.piscos.shoppinglist.product.ProductActivity
import ga.piscos.shoppinglist.stickyrecycler.StickyAdapter
import ga.piscos.shoppinglist.stickyrecycler.StickyHeaderItemDecorator
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.allproducts_housesection_header.view.*
import kotlinx.android.synthetic.main.collection_product_item.view.*
import kotlinx.android.synthetic.main.collection_products_list_fragment.*
import kotlinx.android.synthetic.main.selected_product_item.view.*
import java.util.concurrent.TimeUnit

class ProductListFragment: Fragment() {


    companion object {
        fun newInstance(): ProductListFragment {
            return  ProductListFragment()
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.collection_products_list_fragment, container, false)
    }


    var itemDisposables = CompositeDisposable()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv_collection_products_list.layoutManager = LinearLayoutManager(activity)
        rv_collection_products_list.addItemDecoration(
            DividerItemDecoration(
                rv_collection_products_list.context,
                DividerItemDecoration.VERTICAL
            )
        )
        rv_collection_products_list.setHasFixedSize(true)
        val adapter = ProductsListItemAdapter{
            val productItem = it as? ProductItem
            productItem?.selectItem()
        }
        val decorator =
            StickyHeaderItemDecorator(
                adapter
            )
        decorator.attachToRecyclerView(rv_collection_products_list)
        rv_collection_products_list.adapter = adapter
        val selectedAdapter = SelectedProductsListItemAdapter{
            val indexToMove = adapter.indexOf(it)
            (rv_collection_products_list.layoutManager!! as LinearLayoutManager).scrollToPositionWithOffset(
                indexToMove,
                (27 *  requireContext().resources.displayMetrics.density).toInt()
            )
        }
        rv_colselectedProducts_collection.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        rv_colselectedProducts_collection.setHasFixedSize(true)
        rv_colselectedProducts_collection.adapter = selectedAdapter
        val model by viewModels<ProductsListViewModel>()
        observe(model.data){
            adapter.updateElements(selectedStore = it.selectedStore, stockList = it.list)

            val selectedElements = it.list.filterIsInstance<ProductItem>().filter { p->p.picked.pickedTimeStamp!=null }
            selectedAdapter.updateElements(selectedElements.sortedByDescending { p -> p.picked.pickedTimeStamp })
            if (selectedElements.any()) {
                rv_colselectedProducts_collection.visibility=View.VISIBLE
            }
            else{
                rv_colselectedProducts_collection.visibility=View.GONE
            }
        }
        observe(model.storesData){

            val sectionsAdapter: ArrayAdapter<ProductItem.Store.Template> =
                ArrayAdapter(requireActivity(), R.layout.spinner_item, it)
            sectionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            store_spinner.adapter = sectionsAdapter

        }
    }

    override fun onResume() {
        super.onResume()
        val model by viewModels<ProductsListViewModel>()
        model.loadData(store_spinner.itemSelections())
    }

    override fun onPause() {
        super.onPause()
        itemDisposables.clear()
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
    interface ItemRowHolder{//holder
    fun bind(selectedStore: ProductItem.Store.Template, item: CollectionItemRow, allItems:List<CollectionItemRow>, listener: (CollectionItemRow) -> Unit)
    }
    private inner class ProductsListItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView),ItemRowHolder {
        override fun bind(selectedStore: ProductItem.Store.Template, item: CollectionItemRow, allItems:List<CollectionItemRow>, listener: (CollectionItemRow) -> Unit)= with(
            itemView
        ){
            val product = item as ProductItem
            tvCollectionProductProductName.text = product.name
            tvCollectionProductQty.text = "x${product.picked.neededQty}"
            if (product.picked.hasPicked){
                imCollectionRemove.visibility=View.VISIBLE
                imCollectionCheck.visibility = View.VISIBLE
                tvCollectionProductProductName.paintFlags = tvCollectionProductProductName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                tvCollectionProductQty.paintFlags = tvCollectionProductQty.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                imCollectionRemove.setOnClickListener { product.unSelect()  }
            }
            else{
                imCollectionRemove.visibility=View.GONE
                imCollectionCheck.visibility = View.GONE
                imCollectionRemove.setOnClickListener {}
                tvCollectionProductProductName.paintFlags = tvCollectionProductProductName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                tvCollectionProductQty.paintFlags = tvCollectionProductQty.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            product.getCurrentVisibleStore(selectedStoreCode = selectedStore.code).let {
                Glide.with(itemView)
                    .load(it?.photoURL)
                    //.transition(DrawableTransitionOptions.with(DrawableAlwaysCrossFadeFactory()))
                    .into(imCollectionProductPhotoView)

                imCollectionProductPhotoViewLogo.alpha = 0.7F
                Glide.with(itemView)
                    .load(it?.logoURL)
                    .into(imCollectionProductPhotoViewLogo)
            }

            setOnLongClickListener {
                val intent = ProductActivity.newIntent(requireActivity(), product.code)
                startActivity(intent)
                return@setOnLongClickListener true
            }
            setOnClickListener { listener(product) }
        }
    }

    private inner class StoreSectionItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        ItemRowHolder {
        override fun bind(selectedStore: ProductItem.Store.Template, item: CollectionItemRow, allItems: List<CollectionItemRow>, listener: (CollectionItemRow) -> Unit)= with(
            itemView
        ){
            val section = item as StoreSection
            this.setBackgroundResource(if (section.finishedSection) R.color.colorFinished else R.color.colorUnFinished)
            tvAllProductsHouseSection.text = "\uD83C\uDFE0 ${section.name}"
            if (section.finishedSection){
                tvAllProductsHouseSection.paintFlags = tvAllProductsHouseSection.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
            else{
                tvAllProductsHouseSection.paintFlags = tvAllProductsHouseSection.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
            setOnClickListener {


                onSectionsClick(allItems)

            }
        }
    }

    private inner class ProductsListItemAdapter(
        private var elements: MutableList<CollectionItemRow> = arrayListOf(),
        private var selectedStore: ProductItem.Store.Template? = null,
        val listener: (CollectionItemRow) -> Unit
    ) : StickyAdapter<StoreSectionItemHolder,RecyclerView.ViewHolder>() {


        fun updateElements(selectedStore:ProductItem.Store.Template, stockList: List<CollectionItemRow>){
            this.selectedStore = selectedStore
            itemDisposables.clear()
            elements.clear()
            elements.addAll(stockList)
            notifyDataSetChanged()
            val elementsReference = elements
            itemDisposables += Observable.interval(4,4, TimeUnit.SECONDS)
                .map { elementsReference.filterIsInstance<ProductItem>().filter { it.moveNextStoreIndex(selectedStore.code) } }
                .flatMap { Observable.fromIterable(it) }
                .map {  elementsReference.indexOf(element = it) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    notifyItemChanged(it)
                }
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

            return when(viewType){
                CollectionItemRow.Type.Item.intValue -> ProductsListItemHolder(
                    LayoutInflater.from(activity).inflate(
                        R.layout.collection_product_item,
                        viewGroup,
                        false
                    ))
                else -> StoreSectionItemHolder(
                    LayoutInflater.from(activity).inflate(
                        R.layout.allproducts_housesection_header,
                        viewGroup,
                        false
                    )
                )
            }
        }
        override fun getItemViewType(position: Int)=elements[position].type.intValue
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = (holder as ItemRowHolder).bind(
            selectedStore = selectedStore!!,
            item = elements[position],
            allItems = elements,
            listener = listener
        )

        override fun getItemCount(): Int {  return elements.size  }


        override fun getHeaderPositionForItem(itemPosition: Int): Int {
            val product = elements[itemPosition] as? ProductItem
            val section = if (product!= null){
                product.storeSectionInstance!!
            } else{
                elements[itemPosition] as StoreSection
            }
            return elements.indexOf(section)
        }

        override fun onBindHeaderViewHolder(holder: StoreSectionItemHolder, position: Int) {
            holder.bind(
                selectedStore = selectedStore!!,
                item = elements[position],
                allItems = elements,
                listener = listener
            )
        }

        override fun onCreateHeaderViewHolder(viewGroup: ViewGroup?): StoreSectionItemHolder{
            return StoreSectionItemHolder(
                LayoutInflater.from(activity).inflate(
                    R.layout.allproducts_housesection_header,
                    viewGroup,
                    false
                )
            )
        }

        override fun handleHeaderClickAtPosition(headerPosition: Int) {
            onSectionsClick(elements)
        }

        fun indexOf(product: ProductItem) = elements.indexOf(product)
    }

    private inner class ProductsListSelectedItemHolder(itemView: View) : RecyclerView.ViewHolder(
        itemView
    ) {
        fun bind(item: ProductItem, listener: (ProductItem) -> Unit)= with(
            itemView
        ){
            item.currentVisibleStore.let {
                Glide.with(itemView)
                    .load(it?.photoURL)
                    .into(imPlanningSelectedImage)
            }
            setOnClickListener { listener(item) }
        }
    }
    private inner class SelectedProductsListItemAdapter(
        private var elements: MutableList<ProductItem> = arrayListOf(),
        val listener: (ProductItem) -> Unit
    ) : RecyclerView.Adapter<ProductsListSelectedItemHolder>() {


        fun updateElements(stockList: List<ProductItem>){
            elements.clear()
            elements.addAll(stockList)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ProductsListSelectedItemHolder {

            return ProductsListSelectedItemHolder(
                LayoutInflater.from(activity).inflate(
                    R.layout.selected_product_item,
                    viewGroup,
                    false
                )
            )
        }
        override fun onBindViewHolder(holder: ProductsListSelectedItemHolder, position: Int) {
            holder.bind(elements[position], listener)

        }

        override fun getItemCount(): Int {  return elements.size  }




    }




    private fun onSectionsClick(elements: List<CollectionItemRow>) {
        val builderSingle = AlertDialog.Builder(requireContext())
        builderSingle.setTitle("Store Section")

        val arrayAdapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.select_dialog_item
        )
        val houseSections = elements.filterIsInstance<StoreSection>()

        houseSections.forEach { arrayAdapter.add(it.name) }

        builderSingle.setNegativeButton("cancel",
            DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })

        builderSingle.setAdapter(arrayAdapter,
            DialogInterface.OnClickListener { dialog, which ->
                val houseSection = houseSections[which]
                val indexToMove = elements.indexOf(houseSection)
                (rv_collection_products_list.layoutManager!! as LinearLayoutManager).scrollToPositionWithOffset(
                    indexToMove,
                    0
                )
            })
        builderSingle.show()
    }
}