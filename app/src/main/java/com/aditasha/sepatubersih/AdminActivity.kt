package com.aditasha.sepatubersih

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.PopupMenu
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.aditasha.sepatubersih.databinding.ActivityMainBinding
import com.aditasha.sepatubersih.presentation.auth.AuthViewModel
import com.google.android.material.appbar.AppBarLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdminActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private val authViewModel: AuthViewModel by viewModels()
    lateinit var navHostFragment: NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loading.isVisible = false
        setSupportActionBar(binding.toolbar)

        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment

        val navController = navHostFragment.navController
        val graph = navController.navInflater.inflate(R.navigation.admin_nav_graph)
        navController.graph = graph

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.orderListAdminFragment,
                R.id.articleFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.appBar.forceLayout()

        binding.navigation.apply {
            menu.clear()
            menuInflater.inflate(R.menu.admin_bottom_navigation, menu)
            setupWithNavController(navController)
            setOnItemSelectedListener { item ->
                @OptIn(NavigationUiSaveStateControl::class)
                NavigationUI.onNavDestinationSelected(item, navController, false)
                true
            }
            setPadding(0, 0, 0, 0)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val param =
                binding.navHostFragmentContentMain.layoutParams as CoordinatorLayout.LayoutParams
            binding.apply {
                when (destination.id) {
                    R.id.orderListAdminFragment -> {
                        appBar.isVisible = false
                        param.behavior = null
                    }
                    else -> {
                        appBar.isVisible = true
                        navigation.isVisible = true
                        param.behavior = AppBarLayout.ScrollingViewBehavior()
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_driver_admin, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_menu_logout -> {
                val popupMenu = PopupMenu(this, binding.toolbar.findViewById(item.itemId))
                popupMenu.menuInflater.inflate(R.menu.menu_logout_popup, popupMenu.menu)

                popupMenu.setOnMenuItemClickListener { popup ->
                    if (popup.itemId == R.id.logout) {
                        authViewModel.logout()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        true
                    } else false
                }
                popupMenu.show()
                true
            }
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