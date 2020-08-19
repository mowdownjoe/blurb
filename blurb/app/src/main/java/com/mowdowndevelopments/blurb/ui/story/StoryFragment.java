package com.mowdowndevelopments.blurb.ui.story;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.mowdowndevelopments.blurb.R;
import com.mowdowndevelopments.blurb.database.entities.Story;
import com.mowdowndevelopments.blurb.databinding.StoryFragmentBinding;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Objects;

public class StoryFragment extends Fragment {

    public static final String ARG_STORY = "story_for_fragment";
    private StoryViewModel viewModel;
    StoryFragmentBinding binding;

    public static StoryFragment newInstance(Story story) {
        StoryFragment fragment = new StoryFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_STORY, story);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = StoryFragmentBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Story story = requireArguments().getParcelable(ARG_STORY);

        viewModel = new ViewModelProvider(requireActivity()).get(StoryViewModel.class);
        viewModel.setActiveStory(story);

        binding.storyTopBar.tvStoryAuthor.setText(Objects.requireNonNull(story).getAuthors());
        binding.storyTopBar.tvStoryTitle.setText(story.getTitle());
        Instant instant = Instant.ofEpochMilli(story.getTimestamp());
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        binding.storyTopBar.tvStoryTime.setText(dateTime
                .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)));
        binding.wvStoryContent.loadData(story.getContent(), "text/html", null);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.story_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.mi_view_in_browser:
                int toolbarColor;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    toolbarColor = requireContext().getColor(R.color.primaryColor);
                } else {
                    toolbarColor = requireContext().getResources().getColor(R.color.primaryColor);
                }
                CustomTabsIntent intent = new CustomTabsIntent.Builder()
                        .setToolbarColor(toolbarColor)
                        .build();
                intent.launchUrl(requireContext(), Uri.parse(viewModel.getActiveStory().getPermalink()));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}