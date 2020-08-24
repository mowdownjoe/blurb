package com.mowdowndevelopments.blurb.ui.story;

import android.content.Intent;
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

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Objects;

public class StoryFragment extends Fragment {

    public static final String ARG_STORY = "story_for_fragment";

    StoryViewModel viewModel;
    StoryFragmentBinding binding;
    private Menu menu;

    @NotNull
    public static StoryFragment newInstance(@NonNull Story story) {
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
        viewModel.setActiveStory(Objects.requireNonNull(story));

        binding.storyTopBar.tvStoryAuthor.setText(story.getAuthors());
        binding.storyTopBar.tvStoryTitle.setText(story.getTitle());
        binding.wvStoryContent.loadData(story.getContent(), "text/html", null);

        Instant instant = Instant.ofEpochMilli(story.getTimestamp());
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        binding.storyTopBar.tvStoryTime.setText(dateTime
                .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)));

        viewModel.enqueueMarkAsRead(story);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.story_fragment_menu, menu);
        this.menu = menu;
        toggleMenuItemVisibility(viewModel.getIsActiveStoryStarred().getValue());
        viewModel.getIsActiveStoryStarred().observe(getViewLifecycleOwner(), this::toggleMenuItemVisibility);
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
                        .setStartAnimations(requireContext(), R.anim.slide_in_top, R.anim.fast_fade_out)
                        .setExitAnimations(requireContext(), R.anim.slide_out_top, R.anim.fast_fade_in)
                        .build();
                intent.launchUrl(requireContext(), Uri.parse(viewModel.getActiveStory().getPermalink()));
                return true;
            case R.id.mi_mark_as_unread:
                viewModel.removeFromMarkAsReadQueue(viewModel.getActiveStory());
                item.setVisible(false);
                return true;
            case R.id.mi_share:
                Intent shareIntent = new Intent(Intent.ACTION_SEND)
                        .setType("text/plain")
                        .putExtra(Intent.EXTRA_TITLE, viewModel.getActiveStory().getTitle())
                        .putExtra(Intent.EXTRA_TEXT, viewModel.getActiveStory().getPermalink());
                startActivity(Intent.createChooser(shareIntent, null));
                return true;
            case R.id.mi_star:
                viewModel.markStoryAsStarred(viewModel.getActiveStory());
                return true;
            case R.id.mi_unstar:
                viewModel.removeStoryFromStarred(viewModel.getActiveStory());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleMenuItemVisibility(Boolean isStarred) {
        if (isStarred == null) return;
        if (menu == null) return;
        if (isStarred){
            menu.findItem(R.id.mi_unstar).setVisible(true);
            menu.findItem(R.id.mi_star).setVisible(false);
        } else {
            menu.findItem(R.id.mi_unstar).setVisible(false);
            menu.findItem(R.id.mi_star).setVisible(true);
        }
    }
}