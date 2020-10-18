package ga.piscos.shoppinglist

import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import ga.piscos.shoppinglist.allproducts.AllProductsFragment
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.attributes.screenBrightness = 1F
        loadFragment(ga.piscos.shoppinglist.planning.ProductListFragment.newInstance())
        bottom_navigation.setOnNavigationItemSelectedListener(this)
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.plan -> loadFragment(ga.piscos.shoppinglist.planning.ProductListFragment.newInstance())
            R.id.allproducts -> loadFragment(AllProductsFragment.newInstance())
            R.id.collection -> loadFragment(ga.piscos.shoppinglist.collection.ProductListFragment.newInstance())
        }
        return true
    }

    private fun loadFragment(fragment: Fragment?): Boolean {
        //switching fragment
        if (fragment != null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
            return true
        }
        return false
    }
}