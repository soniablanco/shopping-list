package ga.piscos.shoppinglist.product




class ProductModel(val template: Template, val editing: Editing, val saved:Saved?){
    class Editing(var name:String?, var houseSection:String?, val stores: MutableList<Store> = mutableListOf()){
        class Store(val code:String, var photoFile: String?, var section: String?)
    }




    class Saved (val code:String, val name:String, val houseSection:String, val stores:List<Store>){
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

