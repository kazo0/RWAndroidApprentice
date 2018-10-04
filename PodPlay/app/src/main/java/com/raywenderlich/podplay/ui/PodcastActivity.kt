package com.raywenderlich.podplay.ui

import android.app.SearchManager
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.firebase.jobdispatcher.*
import com.raywenderlich.podplay.R
import com.raywenderlich.podplay.repository.ItunesRepo
import com.raywenderlich.podplay.adapter.PodcastListAdapter
import com.raywenderlich.podplay.db.PodPlayDatabase
import com.raywenderlich.podplay.repository.PodcastRepo
import com.raywenderlich.podplay.service.EpisodeUpdateService
import com.raywenderlich.podplay.service.FeedService
import com.raywenderlich.podplay.service.ItunesService
import com.raywenderlich.podplay.viewmodel.PodcastViewModel
import com.raywenderlich.podplay.viewmodel.SearchViewModel
import kotlinx.android.synthetic.main.activity_podcast.*

class PodcastActivity : AppCompatActivity(),
        PodcastListAdapter.PodcastListAdapterListener,
        PodcastDetailsFragment.OnPodcastDetailsListener {

    private lateinit var searchViewModel: SearchViewModel
    private lateinit var podcastViewModel: PodcastViewModel
    private lateinit var podcastListAdapter: PodcastListAdapter
    private lateinit var searchMenuItem: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_podcast)
        setupToolbar()
        setupViewModels()
        updateControls()
        setupPodcastsListView()
        handleIntent(intent)
        addBackStackListener()
        scheduleJobs()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.let { menu ->
            menuInflater.inflate(R.menu.menu_search, menu)

            searchMenuItem = menu.findItem(R.id.search_item)
            searchMenuItem.setOnActionExpandListener(object: MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(p0: MenuItem?): Boolean {
                    return true
                }
                override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {
                    showSubscribedPodcasts()
                    return true
                }
            })
            val searchView = searchMenuItem.actionView as SearchView

            val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager

            searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))

            if (supportFragmentManager.backStackEntryCount > 0) {
                podcastRecyclerView.visibility = View.INVISIBLE
            }

        }
        if (podcastRecyclerView.visibility == View.INVISIBLE) {
            searchMenuItem.isVisible = false
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent?.let { handleIntent(it) }
    }

    override fun onShowDetails(podcastSummaryViewData: SearchViewModel.PodcastSummaryView) {
        val feedUrl = podcastSummaryViewData.feedUrl ?: return

        showProgressBar()
        podcastViewModel.getPodcast(podcastSummaryViewData) {
            hideProgressBar()
            if (it != null) {
                showPodcastDetailsFragment()
            } else {
                showError("Error loading feed: $feedUrl")
            }
        }
    }

    override fun onSubscribe() {
        podcastViewModel.saveActivePodcast()
        supportFragmentManager.popBackStack()
    }

    override fun onUnsubscribe() {
        podcastViewModel.deleteActivePodcast()
        supportFragmentManager.popBackStack()
    }

    override fun onShowEpisodePlayer(episodeViewData: PodcastViewModel.EpisodeView) {
        podcastViewModel.activeEpisodeView = episodeViewData
        showPlayerFragment()
    }

    private fun showPodcastDetailsFragment() {
        var detailsFragment = createPodcastDetailsFragment()

        supportFragmentManager.beginTransaction()
                .add(R.id.podcastDetailsContainer, detailsFragment, TAG_DETAILS_FRAGMENT)
                .addToBackStack("DetailsFragment")
                .commit()

        podcastRecyclerView.visibility = View.INVISIBLE
        searchMenuItem.isVisible = false
    }

    private fun createPodcastDetailsFragment() : PodcastDetailsFragment {
        var fragment = supportFragmentManager
                .findFragmentByTag(TAG_DETAILS_FRAGMENT) as PodcastDetailsFragment?

        if (fragment == null) {
            fragment = PodcastDetailsFragment.newInstance()
        }
        return fragment
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_SEARCH) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            performSearch(query)
        }

        val feedUrl = intent.getStringExtra(EpisodeUpdateService.EXTRA_FEED_URL)
        if (feedUrl != null) {
            podcastViewModel.setActivePodcast(feedUrl) {
                it?.let { summaryView ->
                    onShowDetails(summaryView)
                }
            }
        }
    }

    private fun performSearch(term: String) {
        showProgressBar()
        searchViewModel.searchPodcasts(term) { results ->
            hideProgressBar()
            toolbar.setTitle(R.string.search_results)
            podcastListAdapter.setSearchData(results)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
    }

    private fun setupViewModels() {
        val itunesService = ItunesService.instance
        searchViewModel = ViewModelProviders.of(this)
                .get(SearchViewModel::class.java)
        searchViewModel.itunesRepo = ItunesRepo(itunesService)

        podcastViewModel = ViewModelProviders.of(this)
                .get(PodcastViewModel::class.java)
        val podcastDb = PodPlayDatabase.getInstance(this)
        podcastViewModel.podcastRepo = PodcastRepo(FeedService.instance, podcastDb.podcastDao())
    }

    private fun updateControls() {
        podcastRecyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        podcastRecyclerView.layoutManager = layoutManager
        val dividerItemDecoration =
                android.support.v7.widget.DividerItemDecoration(
                        podcastRecyclerView.context, layoutManager.orientation)
        podcastRecyclerView.addItemDecoration(dividerItemDecoration)
        podcastListAdapter = PodcastListAdapter(null, this, this)
        podcastRecyclerView.adapter = podcastListAdapter
    }

    private fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        progressBar.visibility = View.INVISIBLE
    }

    private fun showError(message: String) {
        AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(R.string.ok_button, null)
                .create()
                .show()
    }

    private fun addBackStackListener() {
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                podcastRecyclerView.visibility = View.VISIBLE
            }
        }
    }

    private fun setupPodcastsListView() {
        podcastViewModel.getPodcasts()?.observe(this, Observer { if (it != null) showSubscribedPodcasts() })
    }

    private fun showSubscribedPodcasts() {
        val podcasts = podcastViewModel.getPodcasts()?.value

        if (podcasts != null) {
            toolbar.title = getString(R.string.subscribed_podcasts)
            podcastListAdapter.setSearchData(podcasts)
        }
    }

    private fun scheduleJobs() {
        val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(this))

        val oneHourInSeconds = 60*60
        val tenMinutesInSeconds = 60*10
        val episodeUpdateJob = dispatcher.newJobBuilder()
                .setService(EpisodeUpdateService::class.java)
                .setTag(TAG_EPISODE_UPDATE_JOB)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(oneHourInSeconds,
                        (oneHourInSeconds + tenMinutesInSeconds)))
                .setLifetime(Lifetime.FOREVER)
                .setConstraints(
                        Constraint.ON_UNMETERED_NETWORK,
                        Constraint.DEVICE_CHARGING
                )
                .build()
        dispatcher.mustSchedule(episodeUpdateJob)
    }

    private fun createEpisodePlayerFragment(): EpisodePlayerFragment {
        var episodePlayerFragment =
                supportFragmentManager.findFragmentByTag(TAG_PLAYER_FRAGMENT) as
                        EpisodePlayerFragment?
        if (episodePlayerFragment == null) {
            episodePlayerFragment = EpisodePlayerFragment.newInstance()
        }
        return episodePlayerFragment
    }

    private fun showPlayerFragment() {
        val episodePlayerFragment = createEpisodePlayerFragment()

        supportFragmentManager.beginTransaction()
                .replace(R.id.podcastDetailsContainer,
                        episodePlayerFragment,
                        TAG_PLAYER_FRAGMENT)
                .addToBackStack("PlayerFragment")
                .commit()

        podcastRecyclerView.visibility = View.INVISIBLE
        searchMenuItem.isVisible = false
    }

    companion object {
        val TAG: String = PodcastActivity::class.java.simpleName
        private val TAG_DETAILS_FRAGMENT = "DetailsFragment"
        private val TAG_EPISODE_UPDATE_JOB = "com.raywenderlich.podplay.episodes"
        private const val TAG_PLAYER_FRAGMENT = "PlayerFragment"
    }
}
