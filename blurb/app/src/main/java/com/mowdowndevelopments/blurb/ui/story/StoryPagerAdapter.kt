package com.mowdowndevelopments.blurb.ui.story

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mowdowndevelopments.blurb.database.entities.Story

class StoryPagerAdapter(fragmentActivity: FragmentActivity, private val stories: Array<Story>?) : FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(position: Int): Fragment {
        if (stories != null) {
            return StoryFragment.newInstance(stories[position])
        }
        throw IllegalStateException("Attempted to create a fragment from a null set of stories.")
    }

    override fun getItemCount(): Int {
        return stories?.size ?: 0
    }
}