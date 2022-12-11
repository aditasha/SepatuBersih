package com.aditasha.sepatubersih

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.aditasha.sepatubersih.databinding.ActivityMainBinding
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    lateinit var navHostFragment: NavHostFragment

    @Inject
    lateinit var firebaseAuth: FirebaseAuth
    @Inject
    lateinit var firebaseDatabase: FirebaseDatabase

    private var notAdmin = false
    private var notDriver = false

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        firebaseDatabase.reference.child("admin")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    fetchAdmin()
                }

                override fun onCancelled(error: DatabaseError) {
                    if (error.code == -3) {
                        notAdmin = true
                        fetchCustomer()
                    }
                }

            })

        firebaseDatabase.reference.child("driver")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    fetchDriver()
                }

                override fun onCancelled(error: DatabaseError) {
                    if (error.code == -3) {
                        notDriver = true
                        fetchCustomer()
                    }
                }

            })
    }

    private fun fetchAdmin() {
        val intent = Intent(this, AdminActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun fetchDriver() {
        val intent = Intent(this, DriverActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun fetchCustomer() {
        if (notAdmin && notDriver) {
            binding.loading.isVisible = false
            navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
            val navController = navHostFragment.navController
            val graph = navController.navInflater.inflate(R.navigation.nav_graph)
            navController.graph = graph

            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.loginFragment,
                    R.id.homeFragment,
                    R.id.orderFragment,
                    R.id.profileFragment
                )
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
            binding.appBar.forceLayout()

            navController.addOnDestinationChangedListener { _, destination, _ ->
                val param =
                    binding.navHostFragmentContentMain.layoutParams as CoordinatorLayout.LayoutParams
                binding.apply {
                    when (destination.id) {
                        R.id.servicesDetailFragment -> {
                            appBar.isVisible = false
                            navigation.isVisible = false
                            param.behavior = null
                        }
                        R.id.loginFragment -> {
                            navigation.isVisible = false
                        }
                        R.id.registerFragment -> {
                            navigation.isVisible = false
                        }
                        R.id.profileEditFragment -> {
                            navigation.isVisible = false
                        }
                        R.id.addAddressFragment -> {
                            appBar.isVisible = false
                            navigation.isVisible = false
                            param.behavior = null
                        }
                        R.id.addShoesFragment -> {
                            navigation.isVisible = false
                        }
                        R.id.orderFormFragment -> {
                            appBar.isVisible = true
                            navigation.isVisible = false
                            param.behavior = AppBarLayout.ScrollingViewBehavior()
                        }
                        else -> {
                            appBar.isVisible = true
                            navigation.isVisible = true
                            param.behavior = AppBarLayout.ScrollingViewBehavior()
                        }
                    }
                }
            }

            binding.navigation.apply {
                menu.clear()
                menuInflater.inflate(R.menu.bottom_navigation, menu)
                setupWithNavController(navController)
                setOnItemSelectedListener { item ->
                    @OptIn(NavigationUiSaveStateControl::class)
                    NavigationUI.onNavDestinationSelected(item, navController, false)
                    true
                }
                setPadding(0, 0, 0, 0)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigateUp(): Boolean {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp(appBarConfiguration)
                || super.onNavigateUp()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}