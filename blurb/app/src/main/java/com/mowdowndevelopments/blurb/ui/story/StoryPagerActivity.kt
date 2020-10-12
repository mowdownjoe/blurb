package com.mowdowndevelopments.blurb.ui.story

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ActivityNavigator
import androidx.navigation.navArgs
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.mowdowndevelopments.blurb.R
import com.mowdowndevelopments.blurb.database.entities.Story
import com.mowdowndevelopments.blurb.databinding.StoryPagerActivityBinding

class StoryPagerActivity : AppCompatActivity() {
    lateinit var binding: StoryPagerActivityBinding
    private val args: StoryPagerActivityArgs by navArgs()
    val viewModel: StoryViewModel by viewModels() //ViewModel shared between activity and fragments.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = StoryPagerActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        setUpViewModelObservers()
        viewModel.setStories(args.stories)

        if (binding.guideMidline != null) { //If in tablet multipane UI
            binding.vp2StoryPager.isUserInputEnabled = false
            supportActionBar?.setTitle(R.string.dest_stories)
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.markQueueAsRead()
    }

    private fun setUpViewModelObservers() {
        viewModel.snackbarMessage.observe(this, { message: String? ->
            if (message != null && message.isNotEmpty()) {
                Snackbar.make(binding.root, message, BaseTransientBottomBar.LENGTH_LONG).show()
            }
        })
        viewModel.stories.observe(this, { stories: Array<Story>? ->  //Observer should only be called once.
            if (stories != null) {
                viewModel.indexToView.observe(this, { storyIndex: Int? -> setPage(storyIndex) })
                binding.vp2StoryPager.adapter = StoryPagerAdapter(this, stories)
                viewModel.setIndexToView(args.initialStory)
            }
        })
    }

    private fun setPage(storyIndex: Int?) { //May be called multiple times in tablet UI
        if (storyIndex != null) {
            binding.vp2StoryPager.setCurrentItem(storyIndex, false)
        }
    }

    override fun finish() {
        ActivityNavigator.applyPopAnimationsToPendingTransition(this)
        super.finish()
    }
}