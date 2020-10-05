package ga.piscos.shoppinglist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ga.piscos.shoppinglist.allproducts.AllProductsFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var listFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as AllProductsFragment?
        if (listFragment == null) {
            listFragment = AllProductsFragment.newInstance()
            supportFragmentManager.beginTransaction().add(R.id.fragment_container, listFragment!!).commit()
        }
    }
}