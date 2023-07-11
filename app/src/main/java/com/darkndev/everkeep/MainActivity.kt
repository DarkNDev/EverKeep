package com.darkndev.everkeep

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.widget.doAfterTextChanged
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.darkndev.everkeep.databinding.ActivityMainBinding
import com.darkndev.everkeep.recyclerview.adapters.NoteAdapter
import com.darkndev.everkeep.recyclerview.decoration.ItemOffsetDecoration
import com.google.android.material.chip.Chip
import com.google.android.material.search.SearchView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val viewModel: MainViewModel by viewModels()

    private lateinit var noteAdapter: NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()

        val ids = setOf(
            R.id.homeFragment,
            R.id.archivedFragment,
            R.id.noteRecycleFragment,
            R.id.labelFragment
        )

        appBarConfiguration = AppBarConfiguration(
            ids, binding.drawerLayout
        )

        noteAdapter = NoteAdapter {
            navController.navigate(R.id.noteEditFragment, bundleOf("NOTE" to it))
            binding.searchView.hide()
        }

        binding.apply {

            viewModelBinding = viewModel
            lifecycleOwner = this@MainActivity

            setSupportActionBar(toolbar)
            toolbar.setupWithNavController(navController, appBarConfiguration)

            navigationView.setupWithNavController(navController)

            navController.addOnDestinationChangedListener { _, destination, _ ->
                if (ids.contains(destination.id)) {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                } else {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                }
            }

            onBackPressedDispatcher.addCallback(this@MainActivity, callback)
        }

        searchEvents()
    }

    private fun searchEvents() {
        binding.apply {
            searchRecyclerView.apply {
                adapter = noteAdapter
                addItemDecoration(ItemOffsetDecoration(8))
            }

            viewModel.labels.observe(this@MainActivity) { labelList ->
                chipGroup.removeAllViews()
                labelList.forEach {
                    val chip = layoutInflater.inflate(
                        R.layout.layout_label_chip,
                        chipGroup,
                        false
                    ) as Chip
                    chip.text = it.label
                    chip.id = it.id.toInt()
                    chipGroup.addView(chip)
                }
            }

            viewModel.itemsQuery.observe(this@MainActivity) { items ->
                noteAdapter.submitList(items)
            }

            searchView.editText.doAfterTextChanged {
                viewModel.searchText.value = it.toString()
            }

            chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
                viewModel.searchPrefix.value = checkedIds.toMutableList()
            }

            searchView.addTransitionListener { _, _, newState ->
                if (newState == SearchView.TransitionState.SHOWN) {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    callback.isEnabled = true
                } else {
                    searchView.clearText()
                    chipGroup.clearCheck()
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    callback.isEnabled = false
                }
            }
        }
    }

    val callback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            if (binding.searchView.isShown) binding.searchView.hide()
        }
    }

    override fun onSupportNavigateUp() =
        navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()

}