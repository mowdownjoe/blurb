package com.mowdowndevelopments.blurb.ui.navHost

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.work.WorkManager
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.mowdowndevelopments.blurb.NavGraphDirections
import com.mowdowndevelopments.blurb.R
import com.mowdowndevelopments.blurb.database.BlurbDb
import com.mowdowndevelopments.blurb.databinding.ActivityMainBinding
import com.mowdowndevelopments.blurb.network.LoadingStatus
import com.mowdowndevelopments.blurb.work.FetchStarredStoriesWorker

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var menu: Menu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val prefs = getSharedPreferences(getString(R.string.shared_pref_file), 0)
        FirebaseCrashlytics.getInstance()
                .setCrashlyticsCollectionEnabled(prefs.getBoolean(getString(R.string.pref_crashlytics_key), false))

        viewModel.logoutStatus.observe(this, { loadingStatus: LoadingStatus ->
            if (loadingStatus === LoadingStatus.DONE) {
                lifecycleScope.launchWhenCreated { BlurbDb.getInstance(this@MainActivity).clearAllTables() }
                WorkManager.getInstance(this).cancelAllWorkByTag(FetchStarredStoriesWorker.WORK_TAG)
                findNavController(R.id.nav_host_fragment).navigate(NavGraphDirections.actionLoginFlow())
            }
        })
        viewModel.errorMessage.observe(this, { message: String? ->
            if (message != null && message.isNotEmpty()) {
                Snackbar.make(binding.root, message, BaseTransientBottomBar.LENGTH_LONG).show()
            }
        })

        val navController = findNavController(R.id.nav_host_fragment)
        navController.addOnDestinationChangedListener { _: NavController?, destination: NavDestination, _: Bundle? ->
            if (prefs.getBoolean(getString(R.string.pref_analytics_key), false)) {
                val bundle = Bundle()
                bundle.putString(FirebaseAnalytics.Param.DESTINATION, destination.navigatorName)
                FirebaseAnalytics.getInstance(this).logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
            }
            if (::menu.isInitialized) {
                val isInLoginFlow = destination.id == R.id.login_fragment ||
                        destination.id == R.id.account_creation_fragment
                menu.findItem(R.id.action_show_favorites).isVisible = !isInLoginFlow
                menu.findItem(R.id.action_logout).isVisible = !isInLoginFlow
            }
        }
        val appBarConfig = AppBarConfiguration.Builder(R.id.FeedListFragment, R.id.login_fragment).build()
        NavigationUI.setupWithNavController(binding.toolbar, navController, appBarConfig)
    }

    override fun onResume() {
        super.onResume()
        if (!getSharedPreferences(getString(R.string.shared_pref_file), 0)
                        .getBoolean(getString(R.string.logged_in_key), false)) {
            findNavController(R.id.nav_host_fragment).navigate(NavGraphDirections.actionLoginFlow())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        val isLoggedIn = getSharedPreferences(getString(R.string.shared_pref_file), 0)
                .getBoolean(getString(R.string.logged_in_key), false)
        menu.findItem(R.id.action_show_favorites).isVisible = isLoggedIn
        menu.findItem(R.id.action_logout).isVisible = isLoggedIn
        this.menu = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val navController = findNavController(R.id.nav_host_fragment)
        when (item.itemId) {
            R.id.action_show_favorites -> {
                navController.navigate(NavGraphDirections.actionGlobalFavoriteStories())
                return true
            }
            R.id.action_logout -> {
                if (viewModel.logoutStatus.value !== LoadingStatus.LOADING) {
                    viewModel.logout()
                }
                return true
            }
            R.id.action_settings -> {
                navController.navigate(NavGraphDirections.actionGlobalPreferences())
                return true
            }
            R.id.action_donate -> {
                navController.navigate(NavGraphDirections.actionGlobalInAppPurchaseDialogFragment())
                return true
            }
            R.id.action_about -> {
                navController.navigate(NavGraphDirections.actionGlobalAboutDialog())
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}