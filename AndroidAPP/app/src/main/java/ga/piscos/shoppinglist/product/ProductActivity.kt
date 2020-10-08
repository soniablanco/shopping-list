package ga.piscos.shoppinglist.product

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ga.piscos.shoppinglist.R
import ga.piscos.shoppinglist.product.ProductFragment

class ProductActivity  : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var listFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as ProductFragment?
        if (listFragment == null) {
            listFragment = ProductFragment.newInstance()
            supportFragmentManager.beginTransaction().add(R.id.fragment_container, listFragment).commit()
        }

    }
}