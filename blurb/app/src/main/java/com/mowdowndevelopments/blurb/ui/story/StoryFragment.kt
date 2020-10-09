package com.mowdowndevelopments.blurb.ui.story

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.mowdowndevelopments.blurb.R
import com.mowdowndevelopments.blurb.database.entities.Story
import com.mowdowndevelopments.blurb.databinding.StoryFragmentBinding
import org.jsoup.Jsoup
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class StoryFragment : Fragment() {
    val viewModel: StoryViewModel by viewModels()
    lateinit var binding: StoryFragmentBinding
    private lateinit var menu: Menu

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = StoryFragmentBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val story: Story? = requireArguments().getParcelable(ARG_STORY)
        viewModel.setActiveStory(requireNotNull(story))
        binding.storyTopBar.tvStoryAuthor.text = story.authors
        binding.storyTopBar.tvStoryTitle.text = story.title

        val doc = Jsoup.parse(story.content) //Format HTML before passing to WebView
        doc.select("img").attr("width", "100%")
        doc.select("figure").attr("style", "width: 80%")
        doc.select("iframe").attr("style", "width: 100%")
        binding.wvStoryContent.loadData(doc.html(), "text/html", null)

        val instant = Instant.ofEpochSecond(story.timestamp)
        val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        binding.storyTopBar.tvStoryTime.text = dateTime
                .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))

        viewModel.enqueueMarkAsRead(story)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.story_fragment_menu, menu)
        this.menu = menu
        toggleMenuItemVisibility(viewModel.isActiveStoryStarred.value)
        viewModel.isActiveStoryStarred.observe(viewLifecycleOwner, this@StoryFragment::toggleMenuItemVisibility)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val activeStory = viewModel.activeStory
        when (item.itemId) {
            R.id.mi_view_in_browser -> {
                val toolbarColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requireContext().getColor(R.color.primaryColor)
                } else {
                    requireContext().resources.getColor(R.color.primaryColor)
                }
                val intent = CustomTabsIntent.Builder()
                        .setToolbarColor(toolbarColor)
                        .setStartAnimations(requireContext(), R.anim.slide_in_top, R.anim.fast_fade_out)
                        .setExitAnimations(requireContext(), R.anim.slide_out_top, R.anim.fast_fade_in)
                        .build()
                intent.launchUrl(requireContext(), Uri.parse(activeStory.permalink))
                return true
            }
            R.id.mi_mark_as_unread -> {
                viewModel.removeFromMarkAsReadQueue(activeStory)
                item.isVisible = false
                return true
            }
            R.id.mi_share -> {
                val shareIntent = Intent(Intent.ACTION_SEND)
                        .setType("text/plain")
                        .putExtra(Intent.EXTRA_TITLE, activeStory.title)
                        .putExtra(Intent.EXTRA_TEXT, activeStory.permalink)
                startActivity(Intent.createChooser(shareIntent, null))
                return true
            }
            R.id.mi_star -> {
                viewModel.markStoryAsStarred(activeStory)
                return true
            }
            R.id.mi_unstar -> {
                viewModel.removeStoryFromStarred(activeStory)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun toggleMenuItemVisibility(isStarred: Boolean?) {
        requireNotNull(isStarred) { return }
        require(::menu.isInitialized) { return }
        if (isStarred) {
            menu.findItem(R.id.mi_unstar).isVisible = true
            menu.findItem(R.id.mi_star).isVisible = false
        } else {
            menu.findItem(R.id.mi_unstar).isVisible = false
            menu.findItem(R.id.mi_star).isVisible = true
        }
    }

    companion object {
        const val ARG_STORY = "story_for_fragment"
        fun newInstance(story: Story): StoryFragment {
            val fragment = StoryFragment()
            val bundle = Bundle()
            bundle.putParcelable(ARG_STORY, story)
            fragment.arguments = bundle
            return fragment
        }
    }
}