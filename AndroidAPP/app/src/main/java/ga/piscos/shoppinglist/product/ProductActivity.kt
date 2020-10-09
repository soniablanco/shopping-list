package ga.piscos.shoppinglist.product

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ga.piscos.shoppinglist.R
import ga.piscos.shoppinglist.product.ProductFragment

class ProductActivity  : AppCompatActivity() {

    companion object {
        const val PROD_ID = "PROD_ID"

        fun newIntent(context: Context, productId: String?): Intent {
            val intent = Intent(context, ProductActivity::class.java)
            if (productId!=null) {
                intent.putExtra(PROD_ID, productId)
            }
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var listFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as ProductFragment?
        if (listFragment == null) {
            listFragment = ProductFragment.newInstance(intent.getStringExtra(PROD_ID))
            supportFragmentManager.beginTransaction().add(R.id.fragment_container, listFragment).commit()
        }

    }
}