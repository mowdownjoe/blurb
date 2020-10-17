package com.mowdowndevelopments.blurb.ui.feedList

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.mowdowndevelopments.blurb.R
import com.mowdowndevelopments.blurb.database.entities.Feed
import com.mowdowndevelopments.blurb.databinding.FragmentFeedListBinding
import com.mowdowndevelopments.blurb.network.LoadingStatus
import com.mowdowndevelopments.blurb.network.responseModels.GetFeedsResponse
import com.mowdowndevelopments.blurb.ui.dialogs.NewFolderDialogFragment
import com.mowdowndevelopments.blurb.ui.dialogs.newFeed.NewFeedDialogFragment
import com.mowdowndevelopments.blurb.ui.feedList.FeedListAdapter.ItemOnClickListener
import com.mowdowndevelopments.blurb.ui.login.LoginFragment
import com.mowdowndevelopments.blurb.work.FetchStarredStoriesWorker
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

class FeedListFragment : Fragment(), ItemOnClickListener {

    val viewModel: FeedListViewModel by viewModels()
    lateinit var binding: FragmentFeedListBinding
    lateinit var adapter: FeedListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        Timber.d("Lifecycle: Creating view")
        binding = FragmentFeedListBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("Lifecycle: View created")
        binding.srfRefreshTab.setProgressBackgroundColorSchemeResource(R.color.secondaryColor)
        binding.srfRefreshTab.setOnRefreshListener { viewModel.refreshFeeds() }
        viewModel.getFeedsResponseData().observe(viewLifecycleOwner, { getFeedsResponse: GetFeedsResponse? ->
            if (getFeedsResponse != null) {
                adapter.setData(getFeedsResponse)
                binding.srfRefreshTab.isRefreshing = false
                viewModel.postFeedsToDb(getFeedsResponse)
            }
        })
        viewModel.loadingStatus.observe(viewLifecycleOwner, { loadingStatus: LoadingStatus? ->
            when (loadingStatus) {
                LoadingStatus.LOADING -> {
                    binding.pbLoadingSpinner.visibility = View.VISIBLE
                    binding.rvFeedList.visibility = View.INVISIBLE
                    binding.tvErrorText.visibility = View.INVISIBLE
                }
                LoadingStatus.WAITING, LoadingStatus.DONE -> {
                    binding.pbLoadingSpinner.visibility = View.INVISIBLE
                    binding.rvFeedList.visibility = View.VISIBLE
                    binding.tvErrorText.visibility = View.INVISIBLE
                }
                LoadingStatus.ERROR -> {
                    binding.pbLoadingSpinner.visibility = View.INVISIBLE
                    binding.rvFeedList.visibility = View.INVISIBLE
                    binding.tvErrorText.visibility = View.VISIBLE
                }
            }
        })
        viewModel.getErrorMessage().observe(viewLifecycleOwner, { message: String? ->
            if (message != null && message.isNotEmpty()) {
                Snackbar.make(view, message, BaseTransientBottomBar.LENGTH_LONG).show()
            }
        })
        adapter = FeedListAdapter(this)
        binding.rvFeedList.adapter = adapter
        binding.rvFeedList.setHasFixedSize(true)
        binding.rvFeedList.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (requireActivity().getSharedPreferences(getString(R.string.shared_pref_file), 0)
                        .getBoolean(getString(R.string.logged_in_key), false)) {
            viewModel.loadFeeds()
        }
        val handle = requireNotNull(NavHostFragment.findNavController(this)
                .currentBackStackEntry).savedStateHandle
        handle.getLiveData<Boolean>(LoginFragment.LOGIN_SUCCESS).observe(viewLifecycleOwner, { loggedIn: Boolean ->
            if (loggedIn) {
                viewModel.loadFeeds()

                //Once the user logs in initially, a repeating work request is made to the WorkManager to fetch Starred stories every 24 hours with the following constraints.
                val constraints = Constraints.Builder().run {
                    setRequiresCharging(true)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        setRequiresDeviceIdle(true)
                    }
                    setRequiredNetworkType(NetworkType.CONNECTED)
                    setRequiresStorageNotLow(true)
                    build()
                }
                val request = PeriodicWorkRequest.Builder(FetchStarredStoriesWorker::class.java, 24, TimeUnit.HOURS).run {
                    setConstraints(constraints)
                    addTag(FetchStarredStoriesWorker.WORK_TAG)
                    build()
                }
                WorkManager.getInstance(requireContext()).enqueue(request)
            }
        })
        handle.getLiveData<EnumMap<NewFolderDialogFragment.ResultKeys, String?>>(NewFolderDialogFragment.ARG_DIALOG_RESULT)
                .observe(viewLifecycleOwner, { result: EnumMap<NewFolderDialogFragment.ResultKeys, String?>? ->
                    if (result != null) {
                        if (result.containsKey(NewFolderDialogFragment.ResultKeys.NESTED_UNDER)) {
                            viewModel.createNewFolder(result[NewFolderDialogFragment.ResultKeys.NEW_FOLDER],
                                    result[NewFolderDialogFragment.ResultKeys.NESTED_UNDER])
                        } else {
                            viewModel.createNewFolder(result[NewFolderDialogFragment.ResultKeys.NEW_FOLDER])
                        }
                    }
                })
        handle.getLiveData<EnumMap<NewFeedDialogFragment.ResultKeys, String?>>(NewFeedDialogFragment.ARG_RESULT)
                .observe(viewLifecycleOwner, { result: EnumMap<NewFeedDialogFragment.ResultKeys, String?>? ->
                    if (result != null) {
                        if (result.containsKey(NewFeedDialogFragment.ResultKeys.FOLDER)) {
                            viewModel.addNewFeed(result[NewFeedDialogFragment.ResultKeys.FEED],
                                    result[NewFeedDialogFragment.ResultKeys.FOLDER])
                        } else {
                            viewModel.addNewFeed(result[NewFeedDialogFragment.ResultKeys.FEED])
                        }
                    }
                })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.feed_list_menu, menu)
        Timber.d("Inflated options menu.")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val folders = requireNotNull(viewModel.getFeedsResponseData().value)
                .folders.keys
        when (item.itemId) {
            R.id.mi_add_feed -> {
                findNavController().navigate(FeedListFragmentDirections.actionFeedListFragmentToAddFeedDialog()
                        .setFolderNames(folders.toTypedArray()))
                return true
            }
            R.id.mi_new_folder -> {
                findNavController().navigate(FeedListFragmentDirections.actionFeedListFragmentToNewFolderDialog()
                        .setFolderNames(folders.toTypedArray()))
                return true
            }
            R.id.mi_view_all_feeds -> {
                val feeds = viewModel.getFeedsResponseData().value!!.feeds.values
                findNavController().navigate(FeedListFragmentDirections
                        .actionFeedListFragmentToRiverOfNewsFragment(feeds.toTypedArray()))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onFeedItemClick(f: Feed?) {
        val prefs = requireActivity().getSharedPreferences(getString(R.string.shared_pref_file), 0)
        if (prefs.getBoolean(getString(R.string.pref_analytics_key), false)) {
            Firebase.analytics.logEvent("userStats") {
                param(FirebaseAnalytics.Param.QUANTITY, adapter.feedCount.toLong())
                param(FirebaseAnalytics.Param.DESTINATION, requireNotNull(f?.feedAddress))
                param(FirebaseAnalytics.Param.ITEM_NAME, requireNotNull(f?.feedTitle))
            }
        }
        findNavController().navigate(FeedListFragmentDirections
                .actionFeedListFragmentToSingleFeedStoryFragment(f!!))
    }

    override fun onFolderItemClick(f: Folder?) {
        val prefs = requireActivity().getSharedPreferences(getString(R.string.shared_pref_file), 0)
        if (prefs.getBoolean(getString(R.string.pref_analytics_key), false)) {
            Firebase.analytics.logEvent("folderClick") {
                param(FirebaseAnalytics.Param.QUANTITY, adapter.feedCount.toLong())
            }
        }
        findNavController().navigate(FeedListFragmentDirections
                .actionFeedListFragmentToRiverOfNewsFragment(requireNotNull(f?.feeds).toTypedArray()))
    }
}