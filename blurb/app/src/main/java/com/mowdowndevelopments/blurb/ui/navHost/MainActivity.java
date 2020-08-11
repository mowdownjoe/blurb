package com.mowdowndevelopments.blurb.ui.navHost;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.mowdowndevelopments.blurb.NavGraphDirections;
import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.databinding.ActivityMainBinding;
import com.mowdowndevelopments.blurb.network.LoadingStatus;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.getLogoutStatus().observe(this, loadingStatus -> {
            if (loadingStatus == LoadingStatus.DONE){
                Navigation.findNavController(binding.mainContent.getRoot())
                        .navigate(NavGraphDirections.actionLoginFlow());
            }
        });
        viewModel.getErrorMessage().observe(this, message -> Snackbar
                .make(binding.getRoot(), message, BaseTransientBottomBar.LENGTH_LONG)
                .show());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        NavController navController = Navigation.findNavController(binding.mainContent.getRoot());

        switch (item.getItemId()) {
            case R.id.action_show_favorites:
                navController.navigate(NavGraphDirections.actionGlobalFavoriteStories());
                return true;
            case R.id.action_logout:
                viewModel.logout();
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
}