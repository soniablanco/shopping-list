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
import ga.piscos.shoppinglist.R
import ga.piscos.shoppinglist.observe
import ga.piscos.shoppinglist.plus
import ga.piscos.shoppinglist.stickyrecycler.StickyAdapter
import ga.piscos.shoppinglist.stickyrecycler.StickyHeaderItemDecorator
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.allproducts_housesection_header.view.*
import kotlinx.android.synthetic.main.collection_product_item.view.*
import kotlinx.android.synthetic.main.collection_products_list_fragment.*

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

        val model by viewModels<ProductsListViewModel>()
        observe(model.data){
            adapter.updateElements(it)
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
    interface ItemRowHolder{//holder
    fun bind(item: CollectionItemRow, allItems:List<CollectionItemRow>, listener: (CollectionItemRow) -> Unit)
    }
    private inner class ProductsListItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView),ItemRowHolder {
        override fun bind(item: CollectionItemRow, allItems:List<CollectionItemRow>, listener: (CollectionItemRow) -> Unit)= with(
            itemView
        ){
            val product = item as ProductItem
            tvCollectionProductProductName.text = product.name
            tvCollectionProductQty.text = "x${product.picked.neededQty}"
            if (product.picked.pickedQty!=null && product.picked.pickedQty==product.picked.neededQty){
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
            val prevObservable = viewsObservable[itemView]
            if (prevObservable!=null) {
                itemDisposables.remove(prevObservable)
                viewsObservable.remove(itemView)
            }
            val disposable = product.getPhotoChangeObservable(0).subscribe {

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
            setOnClickListener { listener(product) }
        }
    }

    private inner class StoreSectionItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        ItemRowHolder {
        override fun bind(item: CollectionItemRow, allItems: List<CollectionItemRow>, listener: (CollectionItemRow) -> Unit)= with(
            itemView
        ){
            val section = item as StoreSection
            tvAllProductsHouseSection.text = "\uD83C\uDFE0 ${section.name}"
            setOnClickListener {


                onSectionsClick(allItems)

            }
        }
    }

    private inner class ProductsListItemAdapter(
        private var elements: MutableList<CollectionItemRow> = arrayListOf(),
        val listener: (CollectionItemRow) -> Unit
    ) : StickyAdapter<StoreSectionItemHolder,RecyclerView.ViewHolder>() {


        fun updateElements(stockList: List<CollectionItemRow>){
            itemDisposables.clear()
            viewsObservable.clear()
            elements.clear()
            elements.addAll(stockList)
            notifyDataSetChanged()
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
            elements[position], elements,listener
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
            holder.bind(elements[position],elements, listener)
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