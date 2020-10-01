package com.mowdowndevelopments.blurb.ui.feedList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mowdowndevelopments.blurb.R
import com.mowdowndevelopments.blurb.database.entities.Feed
import com.mowdowndevelopments.blurb.databinding.FolderListItemBinding
import com.mowdowndevelopments.blurb.network.responseModels.GetFeedsResponse
import com.mowdowndevelopments.blurb.ui.feedList.FolderInnerFeedListAdapter.FeedOnClickListener
import timber.log.Timber
import java.util.*

class FeedListAdapter(private val listener: ItemOnClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var feedListItems: ArrayList<FeedListItem>

    interface ItemOnClickListener {
        fun onFeedItemClick(f: Feed?)
        fun onFolderItemClick(f: Folder?)
    }

    override fun getItemViewType(position: Int): Int {
        val item = feedListItems[position]
        if (item is Feed) {
            return HOLDER_TYPE_ORPHAN_FEED
        } else if (item is Folder) {
            return HOLDER_TYPE_FOLDER
        }
        return super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View
        if (viewType == HOLDER_TYPE_ORPHAN_FEED) {
            view = inflater.inflate(R.layout.feed_list_item, parent, false)
            return OrphanFeedViewHolder(view)
        } else if (viewType == HOLDER_TYPE_FOLDER) {
            view = inflater.inflate(R.layout.folder_list_item, parent, false)
            return FolderViewHolder(view)
        }
        throw UnsupportedOperationException("Tried to create an unsupported ViewHolder type.")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is FolderViewHolder) {
            holder.bind(feedListItems[position] as Folder?)
        } else if (holder is OrphanFeedViewHolder) {
            holder.bind((feedListItems[position] as Feed?)!!)
        }
    }

    override fun getItemCount(): Int = if (!::feedListItems.isInitialized) {
        feedListItems.size
    } else 0

    val feedCount: Int
        get() {
            if (!::feedListItems.isInitialized) return 0
            var count = 0
            for (item in feedListItems) {
                if (item is Folder) {
                    count += item.feeds.size
                } else {
                    ++count
                }
            }
            return count
        }

    fun setData(response: GetFeedsResponse) {
        feedListItems = ArrayList()
        val feedMap = response.feeds
        response.folders.forEach { (folderName: String, feedIDs: Array<Int>) ->
            val feeds = ArrayList<Feed>()
            for (i in feedIDs) {
                val feed = feedMap[i.toString()]
                feeds.add(requireNotNull(feed))
            }
            if (folderName.trim { it <= ' ' }.isNotEmpty()) {
                feedListItems.add(Folder(folderName, feeds))
            } else { //Empty folder name means not sorted into folder
                feedListItems.addAll(0, feeds)
            }
        }
        notifyDataSetChanged()
        Timber.d("New data received for adapter. Refreshing.")
    }

    private inner class OrphanFeedViewHolder(itemView: View) : BaseFeedViewHolder(itemView) {
        override fun onClick(view: View) {
            if (!::feedListItems.isInitialized) {
                listener.onFeedItemClick(feedListItems[adapterPosition] as Feed?)
            }
        }
    }

    private inner class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, FeedOnClickListener {
        private val binding: FolderListItemBinding = FolderListItemBinding.bind(itemView)
        private var areFeedsHidden = false
        private val adapter: FolderInnerFeedListAdapter
        fun bind(folder: Folder?) {
            areFeedsHidden = false
            binding.rvFolderFeeds.visibility = View.VISIBLE
            binding.folderHeader.ibExpandContractFolder.setImageResource(R.drawable.ic_baseline_unfold_less)
            binding.folderHeader.ibExpandContractFolder.contentDescription = itemView.context.getString(R.string.desc_collapse)
            binding.folderHeader.tvFolderName.text = folder!!.name
            adapter.setInnerFeeds(folder.feeds)
        }

        override fun onClick(view: View) {
            if (!::feedListItems.isInitialized) {
                listener.onFolderItemClick(feedListItems[adapterPosition] as Folder?)
            }
        }

        override fun onInnerItemClick(feed: Feed) {
            listener.onFeedItemClick(feed)
        }

        init {
            binding.folderHeader.ibExpandContractFolder.setOnClickListener {
                if (areFeedsHidden) {
                    binding.rvFolderFeeds.visibility = View.VISIBLE
                    binding.folderHeader.ibExpandContractFolder.setImageResource(R.drawable.ic_baseline_unfold_less)
                    binding.folderHeader.ibExpandContractFolder.contentDescription = itemView.context.getString(R.string.desc_collapse)
                    areFeedsHidden = false
                } else {
                    binding.rvFolderFeeds.visibility = View.GONE
                    binding.folderHeader.ibExpandContractFolder.setImageResource(R.drawable.ic_baseline_unfold_more)
                    binding.folderHeader.ibExpandContractFolder.contentDescription = itemView.context.getString(R.string.desc_expand)
                    areFeedsHidden = true
                }
            }
            binding.folderHeader.ivFolderIcon.setOnClickListener(this)
            binding.folderHeader.tvFolderName.setOnClickListener(this)
            adapter = FolderInnerFeedListAdapter(this)
            binding.rvFolderFeeds.setHasFixedSize(true)
            binding.rvFolderFeeds.adapter = adapter
            binding.rvFolderFeeds.layoutManager = LinearLayoutManager(itemView.context)
        }
    }

    companion object {
        private const val HOLDER_TYPE_FOLDER = 771
        private const val HOLDER_TYPE_ORPHAN_FEED = 367
    }
}