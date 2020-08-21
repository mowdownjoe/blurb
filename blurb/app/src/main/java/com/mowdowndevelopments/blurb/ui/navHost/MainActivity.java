package com.mowdowndevelopments.blurb.ui.navHost;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.mowdowndevelopments.blurb.NavGraphDirections;
import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.databinding.ActivityMainBinding;
import com.mowdowndevelopments.blurb.network.LoadingStatus;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.getLogoutStatus().observe(this, loadingStatus -> {
            if (loadingStatus == LoadingStatus.DONE){
                Navigation.findNavController(this, R.id.nav_host_fragment)
                        .navigate(NavGraphDirections.actionLoginFlow());
            }
        });
        viewModel.getErrorMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()){
                Snackbar.make(binding.getRoot(), message, BaseTransientBottomBar.LENGTH_LONG).show();
            }
        });

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                    if (menu != null) {
                        if (destination.getId() == R.id.login_fragment ||
                                destination.getId() == R.id.account_creation_fragment){
                            menu.findItem(R.id.action_show_favorites).setVisible(false);
                            menu.findItem(R.id.action_logout).setVisible(false);
                        } else {
                            menu.findItem(R.id.action_show_favorites).setVisible(true);
                            menu.findItem(R.id.action_logout).setVisible(true);
                        }
                    }
                });
        AppBarConfiguration appBarConfig = new AppBarConfiguration
                .Builder(R.id.FeedListFragment, R.id.login_fragment).build();
        NavigationUI.setupWithNavController(binding.toolbar, navController, appBarConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!getSharedPreferences(getString(R.string.shared_pref_file), 0)
                .getBoolean(getString(R.string.logged_in_key), false)){
            Navigation.findNavController(this, R.id.nav_host_fragment)
                    .navigate(NavGraphDirections.actionLoginFlow());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        boolean isLoggedIn = getSharedPreferences(getString(R.string.shared_pref_file), 0)
                .getBoolean(getString(R.string.logged_in_key), false);
        menu.findItem(R.id.action_show_favorites).setVisible(isLoggedIn);
        menu.findItem(R.id.action_logout).setVisible(isLoggedIn);

        this.menu = menu;

        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        switch (item.getItemId()) {
            case R.id.action_show_favorites:
                navController.navigate(NavGraphDirections.actionGlobalFavoriteStories());
                return true;
            case R.id.action_logout:
                if (viewModel.getLogoutStatus().getValue() != LoadingStatus.LOADING) {
                    viewModel.logout();
                }
                return true;
            case R.id.action_settings:
                navController.navigate(NavGraphDirections.actionGlobalPreferences());
                return true;
            case R.id.action_donate:
                //TODO Implement In-app billing
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        NavDestination destination = Navigation.findNavController(this, R.id.nav_host_fragment)
                .getCurrentDestination();
        boolean isLoggedIn = getSharedPreferences(getString(R.string.shared_pref_file), 0)
                .getBoolean(getString(R.string.logged_in_key), false);
        if (!isLoggedIn && Objects.requireNonNull(destination).getId() == R.id.login_fragment) {
            finishAndRemoveTask();
        } else {
            super.onBackPressed();
        }
    }
}