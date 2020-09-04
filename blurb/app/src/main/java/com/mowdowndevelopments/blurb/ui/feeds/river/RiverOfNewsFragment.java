package com.mowdowndevelopments.blurb.ui.feeds.river;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.mowdowndevelopments.blurb.R;

public class RiverOfNewsFragment extends Fragment {

    private RiverOfNewsViewModel mViewModel;

    public static RiverOfNewsFragment newInstance() {
        return new RiverOfNewsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.river_of_news_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(RiverOfNewsViewModel.class);
        // TODO: Use the ViewModel
    }

}