package com.mowdowndevelopments.blurb.ui.feeds.single

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.mowdowndevelopments.blurb.R
import com.mowdowndevelopments.blurb.database.entities.Story
import com.mowdowndevelopments.blurb.databinding.SingleFeedFragmentBinding
import com.mowdowndevelopments.blurb.network.LoadingStatus
import com.mowdowndevelopments.blurb.ui.dialogs.SortOrderDialogFragment
import com.mowdowndevelopments.blurb.ui.feeds.StoryClickListener
import com.squareup.picasso.Picasso
import timber.log.Timber
import java.util.*

class SingleFeedFragment : Fragment(), StoryClickListener {
    lateinit var binding: SingleFeedFragmentBinding
    private lateinit var viewModel: SingleFeedViewModel
    private lateinit var adapter: SingleFeedAdapter
    private val args by navArgs<SingleFeedFragmentArgs>()

    companion object {
        fun newInstance(): SingleFeedFragment = SingleFeedFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Picasso.get().load(args.feedToShow.favIconUrl).fetch() //Warm up Picasso's cache.
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = SingleFeedFragmentBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        requireNotNull((requireActivity() as AppCompatActivity).supportActionBar).title = args.feedToShow.feedTitle
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = SingleFeedAdapter(args.feedToShow, this)
        binding.content.rvStoryList.adapter = adapter
        binding.content.rvStoryList.layoutManager = LinearLayoutManager(requireContext())
        viewModel = ViewModelProvider(this, SingleFeedViewModel.Factory(requireActivity().application, args.feedToShow.id))
                .get(SingleFeedViewModel::class.java)
        setupObservers()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val controller = NavHostFragment.findNavController(this)
        val handle = requireNotNull(controller.currentBackStackEntry).savedStateHandle
        binding.content.srfRefreshTab.setProgressBackgroundColorSchemeResource(R.color.secondaryColor)
        binding.content.srfRefreshTab.setOnRefreshListener {
            if (handle.contains(SortOrderDialogFragment.ARG_RESULT)) {
                val result = requireNotNull(handle.get<EnumMap<SortOrderDialogFragment.ResultKeys, String>>(SortOrderDialogFragment.ARG_RESULT))
                refreshList(result)
            } else {
                viewModel.simpleRefresh()
            }
        }
        handle.getLiveData<EnumMap<SortOrderDialogFragment.ResultKeys, String>>(SortOrderDialogFragment.ARG_RESULT)
                .observe(viewLifecycleOwner, { result: EnumMap<SortOrderDialogFragment.ResultKeys, String>? -> result?.let { refreshList(it) } })
    }

    private fun setupObservers() {
        viewModel.errorMessage.observe(viewLifecycleOwner, { message: String? ->
            if (message != null && message.isNotEmpty()) {
                Snackbar.make(binding.root, message, BaseTransientBottomBar.LENGTH_LONG).show()
            }
        })
        viewModel.storyList.observe(viewLifecycleOwner,  { stories: PagedList<Story>? -> adapter.submitList(stories) })
        viewModel.loadingStatus.observe(viewLifecycleOwner, { loadingStatus: LoadingStatus? ->
            when (loadingStatus) {
                LoadingStatus.LOADING -> {
                    binding.content.rvStoryList.visibility = View.INVISIBLE
                    binding.content.tvErrorText.visibility = View.INVISIBLE
                    binding.content.pbLoadingSpinner.visibility = View.VISIBLE
                }
                LoadingStatus.WAITING, LoadingStatus.DONE -> {
                    binding.content.rvStoryList.visibility = View.VISIBLE
                    binding.content.tvErrorText.visibility = View.INVISIBLE
                    binding.content.pbLoadingSpinner.visibility = View.INVISIBLE
                    binding.content.srfRefreshTab.isRefreshing = false
                }
                LoadingStatus.ERROR -> {
                    binding.content.tvErrorText.visibility = View.VISIBLE
                    binding.content.pbLoadingSpinner.visibility = View.INVISIBLE
                    binding.content.rvStoryList.visibility = View.VISIBLE
                    binding.content.srfRefreshTab.isRefreshing = false
                }
            }
        })
        viewModel.pageLoadingStatus.observe(viewLifecycleOwner, { loadingStatus: LoadingStatus? ->
            when (loadingStatus) {
                LoadingStatus.LOADING -> binding.content.srfRefreshTab.isRefreshing = true
                LoadingStatus.ERROR, LoadingStatus.WAITING, LoadingStatus.DONE -> binding.content.srfRefreshTab.isRefreshing = false
            }
        })
    }

    private fun refreshList(result: EnumMap<SortOrderDialogFragment.ResultKeys, String>) {
        val sortOrder = result[SortOrderDialogFragment.ResultKeys.SORT]
        val filter = result[SortOrderDialogFragment.ResultKeys.FILTER]
        Timber.d("New parameters received: %s, %s", sortOrder, filter)
        viewModel.refreshWithNewParameters(sortOrder!!, filter!!)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.feed_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mi_mark_all_read -> {
                viewModel.markAllAsRead()
                findNavController().popBackStack()
                return true
            }
            R.id.mi_sort_filter -> {
                findNavController().navigate(SingleFeedFragmentDirections
                        .actionSingleFeedStoryFragmentToSortFilterDialog())
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStoryClick(position: Int) {
        val storyList: List<Story> = requireNotNull(adapter.currentList)
        findNavController().navigate(SingleFeedFragmentDirections
                .actionSingleFeedStoryFragmentToStoryPagerActivity(storyList.toTypedArray())
                .setInitialStory(position))
    }
}