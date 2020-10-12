package com.mowdowndevelopments.blurb.ui.story

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mowdowndevelopments.blurb.database.entities.Story
import com.mowdowndevelopments.blurb.databinding.FragmentMultiPaneStoryListBinding
import com.mowdowndevelopments.blurb.ui.feeds.StoryClickListener

class MultiPaneStoryListFragment : Fragment(), StoryClickListener {

    private val viewModel: StoryViewModel by activityViewModels()
    private lateinit var binding: FragmentMultiPaneStoryListBinding
    private lateinit var adapter: MultiPaneStoryListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentMultiPaneStoryListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = MultiPaneStoryListAdapter(this)
        binding.rvStoriesMp.setHasFixedSize(true)
        binding.rvStoriesMp.layoutManager = LinearLayoutManager(requireContext())
        binding.rvStoriesMp.adapter = adapter

        viewModel.stories.observe(viewLifecycleOwner, { stories: Array<Story>? ->
            if (stories != null) {
                adapter.setStories(stories)
            }
        })
    }

    override fun onStoryClick(position: Int) {
        viewModel.setIndexToView(position)
    }
}