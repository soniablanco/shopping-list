package ga.piscos.shoppinglist.allproducts

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ga.piscos.shoppinglist.R
import kotlinx.android.synthetic.main.allproducts_all_products_fragment.*
import kotlinx.android.synthetic.main.allproducts_product_item.view.*

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
        adapter.updateProducts(listOf(ProductItem("coco"), ProductItem("suares")))
    }

    private inner class ProductsListItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(product: ProductItem, onclickListener: (ProductItem) -> Unit)= with(itemView){

            tvProductName.text = product.name.replace("++","").replace("**","")


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