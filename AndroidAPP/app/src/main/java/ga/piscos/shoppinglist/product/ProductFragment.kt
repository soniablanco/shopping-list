package ga.piscos.shoppinglist.product

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jakewharton.rxbinding4.widget.itemSelections
import com.jakewharton.rxbinding4.widget.textChanges
import com.yalantis.ucrop.UCrop
import ga.piscos.shoppinglist.R
import ga.piscos.shoppinglist.observe
import ga.piscos.shoppinglist.plus
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.default
import id.zelory.compressor.constraint.destination
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.product_product_layout.*
import kotlinx.android.synthetic.main.product_product_store_item.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File


class ProductFragment : Fragment() {

    companion object {
        private const val PROD_ID = "PROD_ID"
        fun newInstance(productId: String?): ProductFragment {
            val args = Bundle()
            if (productId!=null) {
                args.putSerializable(PROD_ID, productId)
            }
            val fragment = ProductFragment()
            fragment.arguments = args
            return fragment
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.product_product_layout, container, false)

    }
    private val viewModel by viewModels<ProductViewModel>()
    var disposables = CompositeDisposable()
    override fun onStop() {
        super.onStop()
        disposables.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv_stores_list.layoutManager = LinearLayoutManager(activity)
        rv_stores_list.addItemDecoration(
            DividerItemDecoration(
                rv_stores_list.context,
                DividerItemDecoration.VERTICAL
            )
        )
        rv_stores_list.setHasFixedSize(true)
        val storesAdapter = StoresListItemAdapter{
        }
        rv_stores_list.adapter = storesAdapter


        fabSyncProduct.setOnClickListener {
            if (viewModel.data.value!!.editing.name.isNullOrBlank())
                return@setOnClickListener
            fabSyncProduct.hide()
            disposables+=viewModel.sync().subscribe {
                requireActivity().finish()
            }
        }

        observe(viewModel.data){ productModel ->

            val sections = mutableListOf(
                ProductModel.Template.HouseSection(
                    "noselect",
                    "Select Section:",
                    0
                )
            )
            sections.addAll(productModel.template.houseSections)
            val sectionsAdapter: ArrayAdapter<ProductModel.Template.HouseSection> =
                ArrayAdapter(requireActivity(), R.layout.spinner_item, sections)
            sectionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            house_spinner.adapter = sectionsAdapter

            val sectionIndex = productModel.getEditingHouseSectionIndex()
            house_spinner.setSelection(if (sectionIndex == null) 0 else sectionIndex + 1)
            house_spinner.itemSelections().subscribe { index->
                val code = if (index>0)  sections[index].code else null
                viewModel.updateHouseSectionCode(code)
            }

            dialogTextEdit.setText(productModel.editing.name ?: "")
            dialogTextEdit.textChanges().subscribe { prodName ->
                viewModel.updateProductName(prodName.toString())
            }

            storesAdapter.updateElements(productModel.getStoresModel())

        }
        viewModel.loadData(requireArguments().getString(PROD_ID))

    }

    private var storeCode:String? = null
    private var storePhotoFile: File? = null




    val cropContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == RESULT_OK ) {
            val resultUri = UCrop.getOutput(it.data!!)
            val contentUri = viewModel.convertToUri(File(resultUri!!.path!!))
            viewModel.updateStorePhoto(storeCode!!,contentUri)
            (rv_stores_list.adapter as StoresListItemAdapter).updateElements(viewModel.data.value!!.getStoresModel())
            storeCode = null
            storePhotoFile = null
        } else if (it.resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(it.data!!)
            storeCode = null
            storePhotoFile = null
        }
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.TakePicture()) {

        GlobalScope.launch {
            val compressedImageFile = Compressor.compress(requireActivity(), storePhotoFile!!) {
                default()
                destination(viewModel.createImageFile())
            }


            val compressedUri = Uri.fromFile(compressedImageFile)
            val croppedUri = Uri.fromFile(viewModel.createImageFile())
            GlobalScope.launch(Dispatchers.Main) {
                val intent = UCrop.of(compressedUri, croppedUri)
                    .withAspectRatio(4F, 4F)
                    .getIntent(requireContext())
                cropContent.launch(intent)

            }
        }



    }
    private inner class StoresListItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(store: ProductStoreModel, onclickListener: (ProductModel.Template.Store) -> Unit)= with(
            itemView
        ){



            val sections = mutableListOf(
                ProductModel.Template.Store.Section(
                    "noselect",
                    "Select Section:"
                )
            )
            sections.addAll(store.template.sections)
            val adapter: ArrayAdapter<ProductModel.Template.Store.Section> =
                ArrayAdapter(activity!!, R.layout.spinner_item, sections)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            val sectionIndex = store.getEditingSectionIndex()
            spinner.setSelection(if (sectionIndex == null) 0 else sectionIndex + 1)
            spinner.itemSelections().subscribe { index->
                val code = if (index>0)  sections[index].code else null
                viewModel.updateStoreSection(storeCode = store.template.code, sectionCode = code)
            }
            if (store.editing.photoTakenURI!=null){
                imPhotoView.alpha = 1F
                Glide.with(itemView)
                    .load(store.editing.photoTakenURI!!)
                    .into(imPhotoView)
                imPhotoViewLogo.alpha = 0.7F
                Glide.with(itemView)
                    .load(store.template.logoURL)
                    .into(imPhotoViewLogo)
            }
            else if (store.saved?.photoURL!=null){
                imPhotoView.alpha = 1F
                Glide.with(itemView)
                    .load(store.saved.photoURL)
                    .into(imPhotoView)
                imPhotoViewLogo.alpha = 0.7F
                Glide.with(itemView)
                    .load(store.template.logoURL)
                    .into(imPhotoViewLogo)
            }
            else {
                imPhotoView.alpha = 0.3F
                Glide.with(itemView)
                    .load(store.template.logoURL)
                    .into(imPhotoView)
            }

            imPhotoView.setOnClickListener {
                val file = viewModel.createImageFile()
                val storePhotoUri  = viewModel.convertToUri(file)
                storeCode = store.template.code
                storePhotoFile = file
                getContent.launch(storePhotoUri)
            }

            setOnClickListener {  }
        }
    }


    private inner class StoresListItemAdapter(
        private var elements: MutableList<ProductStoreModel> = arrayListOf(),
        val onclickListener: (ProductModel.Template.Store) -> Unit
    ) : RecyclerView.Adapter<StoresListItemHolder>() {


        fun updateElements(stores: List<ProductStoreModel>){
            elements.clear()
            elements.addAll(stores)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): StoresListItemHolder {
            return StoresListItemHolder(
                LayoutInflater.from(activity).inflate(
                    R.layout.product_product_store_item,
                    viewGroup,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: StoresListItemHolder, position: Int) = holder.bind(
            elements[position],
            onclickListener
        )

        override fun getItemCount(): Int {  return elements.size  }
    }

}