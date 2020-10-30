package ga.piscos.shoppinglist.planning

import android.content.Context
import android.content.DialogInterface
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
import com.bumptech.glide.request.transition.DrawableCrossFadeTransition
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.request.transition.TransitionFactory
import ga.piscos.shoppinglist.R
import ga.piscos.shoppinglist.observe
import ga.piscos.shoppinglist.plus
import ga.piscos.shoppinglist.product.ProductActivity
import ga.piscos.shoppinglist.stickyrecycler.StickyAdapter
import ga.piscos.shoppinglist.stickyrecycler.StickyHeaderItemDecorator
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.allproducts_housesection_header.view.*
import kotlinx.android.synthetic.main.planning_product_item.view.*
import kotlinx.android.synthetic.main.planning_products_list_fragment.*
import kotlinx.android.synthetic.main.planning_selected_product_item.view.*
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

        return inflater.inflate(R.layout.planning_products_list_fragment, container, false)
    }


    var itemDisposables = CompositeDisposable()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv_planning_products_list.layoutManager = LinearLayoutManager(activity)
        rv_planning_products_list.addItemDecoration(
            DividerItemDecoration(
                rv_planning_products_list.context,
                DividerItemDecoration.VERTICAL
            )
        )
        rv_planning_products_list.setHasFixedSize(true)

        val adapter = ProductsListItemAdapter{
            val productItem = it as? ProductItem
            productItem?.selectItem()
        }
        val decorator =
            StickyHeaderItemDecorator(
                adapter
            )
        decorator.attachToRecyclerView(rv_planning_products_list)
        rv_planning_products_list.adapter = adapter
        val selectedAdapter = SelectedProductsListItemAdapter{
            val indexToMove = adapter.indexOf(it)
            (rv_planning_products_list.layoutManager!! as LinearLayoutManager).scrollToPositionWithOffset(
                indexToMove,
                (27 *  requireContext().resources.displayMetrics.density).toInt()
            )
        }
        rv_selectedProducts.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        rv_selectedProducts.setHasFixedSize(true)
        rv_selectedProducts.adapter = selectedAdapter
        val model by viewModels<ProductsListViewModel>()
        observe(model.data){
            adapter.updateElements(it)
            val selectedElements = it.filterIsInstance<ProductItem>().filter { p->p.selectedData!=null }
            selectedAdapter.updateElements(selectedElements.sortedByDescending { p -> p.selectedData!!.addedTimeStamp })
            if (selectedElements.any()) {
                rv_selectedProducts.visibility=View.VISIBLE
            }
            else{
                rv_selectedProducts.visibility=View.GONE
            }
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

    interface AllProductsItemRowHolder{//holder
    fun bind(
        item: AllProductItemRow,
        allItems: List<AllProductItemRow>,
        listener: (AllProductItemRow) -> Unit
    )
    }
    private inner class ProductsListItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        AllProductsItemRowHolder {
        override fun bind(
            item: AllProductItemRow,
            allItems: List<AllProductItemRow>,
            listener: (AllProductItemRow) -> Unit
        )= with(
            itemView
        ){
            val product = item as ProductItem
                    tvPlanningProductProductName.text = product.name
            if (product.selectedData!=null){
                imPlanningCheck.visibility=View.VISIBLE
                tvPlanningProductQty.visibility = View.VISIBLE
                tvPlanningProductQty.text = "x${product.selectedData!!.neededQty}"
                imRemove.visibility=View.VISIBLE
                imRemove.setOnClickListener { product.unSelect()  }
            }
            else{
                imPlanningCheck.visibility=View.GONE
                tvPlanningProductQty.visibility = View.GONE
                imRemove.visibility=View.GONE
                imRemove.setOnClickListener {}
            }
            product.currentVisibleStore.let {
                Glide.with(itemView)
                    .load(it?.photoURL)
                    //.transition(DrawableTransitionOptions.with(DrawableAlwaysCrossFadeFactory()))
                    .into(imPlanningProductPhotoView)

                imPlanningProductPhotoViewLogo.alpha = 0.7F
                Glide.with(itemView)
                    .load(it?.logoURL)
                    .into(imPlanningProductPhotoViewLogo)
            }

            setOnLongClickListener {
                val intent = ProductActivity.newIntent(requireActivity(), product.code)
                startActivity(intent)
                return@setOnLongClickListener true
            }
            setOnClickListener { listener(item) }
        }
    }

    private inner class HouseSectionItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        AllProductsItemRowHolder {
        override fun bind(
            item: AllProductItemRow,
            allItems: List<AllProductItemRow>,
            listener: (AllProductItemRow) -> Unit
        )= with(
            itemView
        ){
            val houseSection = item as HouseSection
            tvAllProductsHouseSection.text = "\uD83C\uDFE0 ${houseSection.name}"
            setOnClickListener {


                onHouseSectionClick(allItems)

            }
        }
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
                    R.layout.planning_selected_product_item,
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
    private fun onHouseSectionClick(elements: List<AllProductItemRow>) {
        val builderSingle = AlertDialog.Builder(requireContext())
        builderSingle.setTitle("House section")

        val arrayAdapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.select_dialog_item
        )
        val houseSections = elements.filterIsInstance<HouseSection>()

        houseSections.forEach { arrayAdapter.add(it.name) }

        builderSingle.setNegativeButton("cancel",
            DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })

        builderSingle.setAdapter(arrayAdapter,
            DialogInterface.OnClickListener { dialog, which ->
                val houseSection = houseSections[which]
                val indexToMove = elements.indexOf(houseSection)
                (rv_planning_products_list.layoutManager!! as LinearLayoutManager).scrollToPositionWithOffset(
                    indexToMove,
                    0
                )
            })
        builderSingle.show()
    }







    private inner class ProductsListItemAdapter(
        private var elements: MutableList<AllProductItemRow> = arrayListOf(),
        val listener: (AllProductItemRow) -> Unit
    ) : StickyAdapter<HouseSectionItemHolder, RecyclerView.ViewHolder>() {


        fun updateElements(stockList: List<AllProductItemRow>){
            itemDisposables.clear()
            elements.clear()
            elements.addAll(stockList)
            notifyDataSetChanged()
            val elementsReference = elements
            itemDisposables += Observable.interval(4, 4, TimeUnit.SECONDS)
                .map { elementsReference.filterIsInstance<ProductItem>().filter { it.moveNextStoreIndex() } }
                .flatMap { Observable.fromIterable(it) }
                .map {  elementsReference.indexOf(element = it) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    notifyItemChanged(it)
                }
        }
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

            return when(viewType){
                AllProductItemRow.Type.Item.intValue -> ProductsListItemHolder(
                    LayoutInflater.from(
                        activity
                    ).inflate(R.layout.planning_product_item, viewGroup, false)
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
            elements[position], elements, listener
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

        override fun onBindHeaderViewHolder(holder: HouseSectionItemHolder, position: Int) {
            holder.bind(elements[position], elements, listener)
        }

        override fun onCreateHeaderViewHolder(viewGroup: ViewGroup?): HouseSectionItemHolder {
            return HouseSectionItemHolder(
                LayoutInflater.from(activity).inflate(
                    R.layout.allproducts_housesection_header,
                    viewGroup,
                    false
                )
            )
        }

        override fun handleHeaderClickAtPosition(headerPosition: Int) {
            onHouseSectionClick(elements)
        }

        fun indexOf(product: ProductItem) = elements.indexOf(product)

    }

}