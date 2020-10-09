package ga.piscos.shoppinglist.product

import android.net.Uri


class ProductStoreModel(val template: ProductModel.Template.Store, var editing: ProductModel.Editing.Store, val saved: ProductModel.Saved.Store?){

    fun getEditingSectionIndex():Int? {
        val houseSection = template.sections.firstOrNull { it.code == editing.section }
        return if (houseSection!=null) template.sections.indexOf(houseSection) else null
    }

}

class ProductModel(val template: Template, val editing: Editing, val saved:Saved?){


    fun getStoresModel() = template.stores.map { storeTemplate->
        ProductStoreModel(template = storeTemplate,editing = editing.stores.first { it.code == storeTemplate.code },saved = saved?.stores?.firstOrNull { it.code == storeTemplate.code })
            }

    fun getEditingHouseSectionIndex():Int? {
        val houseSection = template.houseSections.firstOrNull { it.code == editing.houseSection }
        return if (houseSection!=null) template.houseSections.indexOf(houseSection) else null
    }

    class Editing(var code:String?, var name:String?, var houseSection:String?, val stores: List<Store>){
        val isNew get() = code==null
        fun getFirebaseEditingNode() =  mapOf(
            "/${code}" to mapOf(
                "houseSection" to houseSection,
                "name" to name,
                "stores" to stores.associateBy({store-> store.code},{store-> mapOf(
                    "photoURL" to store.photoFirebaseUrl,
                    "section" to store.section,
                )})
            )
        )

        class Store(val code:String, var photoTakenURI: Uri? =null, var photoFirebaseUrl: String? =null, var section: String? =null)
    }




    class Saved (val code:String, val name:String?, val houseSection:String?, val stores:List<Store>){
        class Store (val code:String, val photoURL:String?, val section:String?)
    }

    class Template(val houseSections:List<HouseSection>, val stores: List<Store>){
        class HouseSection(val code:String, val name:String){
            override fun toString(): String {
                return name
            }
        }
        class Store(val code:String, val logoURL:String,val sections:List<Section>){
            class Section(val code:String, val name:String){
                override fun toString(): String {
                    return name
                }
            }
        }
    }

}

