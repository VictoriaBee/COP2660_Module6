package com.raywenderlich.podplay.ui

import android.app.AlertDialog
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.raywenderlich.podplay.R
import com.raywenderlich.podplay.adapter.PodcastListAdapter
import com.raywenderlich.podplay.repository.ItunesRepo
import com.raywenderlich.podplay.repository.PodcastRepo
import com.raywenderlich.podplay.service.FeedService
import com.raywenderlich.podplay.service.ItunesService
import com.raywenderlich.podplay.viewmodel.PodcastViewModel
import com.raywenderlich.podplay.viewmodel.SearchViewModel
import kotlinx.android.synthetic.main.activity_podcast.*


class PodcastActivity : AppCompatActivity(),
        PodcastListAdapter.PodcastListAdapterListener {

    private val searchViewModel by viewModels<SearchViewModel>()
    private lateinit var podcastListAdapter: PodcastListAdapter
    private lateinit var searchMenuItem: MenuItem
    private val podcastViewModel by viewModels<PodcastViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_podcast)

        val TAG = javaClass.simpleName

        setupToolbar()
        setupViewModels()
        updateControls()
        handleIntent(intent)
        addBackStackListener()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // 1 - Inflating options menu.
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_search, menu)
        // 2 - Search action menu item is found in options menu,
        // and search view's taken from item's actionView property.
        searchMenuItem = menu?.findItem(R.id.search_item)!!
        val searchView = searchMenuItem.actionView as SearchView
        // 3 - The system SearchManager obj is loaded.
        val searchManager = getSystemService(Context.SEARCH_SERVICE)
                as SearchManager
        // 4 - SearchManager loads the search configuration and
        // assigns it to the searchView.
        searchView.setSearchableInfo(searchManager.getSearchableInfo(
            componentName))

        if (supportFragmentManager.backStackEntryCount > 0) {
            podcastRecyclerView.visibility = View.INVISIBLE
        }
        // Ensures the searchMenuItem remains hidden
        // if podcastRecyclerView is not visible.
        if (podcastRecyclerView.visibility == View.INVISIBLE) {
            searchMenuItem.isVisible = false
        }
        return true
    }

    private fun performSearch(term: String) {
        showProgressBar()
        searchViewModel.searchPodcasts(term) { results ->
            hideProgressBar()
            toolbar.title = term
            podcastListAdapter.setSearchData(results)
        }
    }

    // Takes in an Intent and checks to see if it's an ACTION_SEARCH;
    // If so, extracts search query and passes to performSearch().
    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY) ?: return
            performSearch(query)
        }
    }

    // Receives the updated Intent when a new search is performed.
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent!!)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
    }

    // Create an instance of the ItunesService
    // and uses ViewModelProviders to get instance of SearchViewModel.
    // Creates new ItunesRepo obj and assigns it to SearchViewModel.
    private fun setupViewModels() {
        val service = ItunesService.instance
        searchViewModel.iTunesRepo = ItunesRepo(service)

        // New instance of the FeedService and
        // uses it to create a new PodcastRepo obj.
        val rssService = FeedService.instance
        podcastViewModel.podcastRepo = PodcastRepo(rssService)
    }

    private fun updateControls() {
        podcastRecyclerView.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(this)
        podcastRecyclerView.layoutManager = layoutManager

        val dividerItemDecoration = DividerItemDecoration(
            podcastRecyclerView.context, layoutManager.orientation)
        podcastRecyclerView.addItemDecoration(dividerItemDecoration)

        podcastListAdapter = PodcastListAdapter(
            null, this, this)
        podcastRecyclerView.adapter = podcastListAdapter
    }

    // Methods for the progress bar.
    private fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        progressBar.visibility = View.INVISIBLE
    }

    override fun onShowDetails(podcastSummaryViewData:
         SearchViewModel.PodcastSummaryViewData) {
        // 1 - feedUrl taken from podcastSummaryViewData obj if not null;
        // otherwise, method return without doing anything.
        val feedUrl = podcastSummaryViewData.feedUrl ?: return
        // 2 - Progress bar is displayed to show the use that the app
        // is busy loading the podcast data.
        showProgressBar()
        // 3 - Called to load the podcast view data.
        podcastViewModel.getPodcast(podcastSummaryViewData) {
            // 4 - After data is returned, progress bar is hidden.
            hideProgressBar()
            if (it != null) {
                // 5 - If data is not null, showDetailsFragment() is called
                // to display the detail fragment.
                showDetailsFragment()
            } else {
                // 6 - If data is null, the error dialog is displayed.
                showError("Error loading feed $feedUrl")
            }
        }
    }

    // Either creates the details Fragment or
    // uses an existing instance if one exists.
    private fun createPodcastDetailsFragment(): PodcastDetailsFragment {
        // 1 - Checks if Fragment already exists.
        var podcastDetailsFragment = supportFragmentManager
            .findFragmentByTag(TAG_DETAILS_FRAGMENT) as
                PodcastDetailsFragment?

        // 2 - If no existing Fragment, creates new one with newInstance()
        // on Fragment's companion obj.
        if (podcastDetailsFragment == null) {
            podcastDetailsFragment = PodcastDetailsFragment.newInstance()
        }

        // Returns Fragment obj.
        return podcastDetailsFragment
    }

    // Displays the details Fragment.
    private fun showDetailsFragment() {
        // 1 - Details fragment is created or retrieved from fragment manager.
        val podcastDetailsFragment = createPodcastDetailsFragment()
        // 2 - Fragmet is added to supportFragmentManager.
        supportFragmentManager.beginTransaction().add(
            R.id.podcastDetailsContainer,
            podcastDetailsFragment, TAG_DETAILS_FRAGMENT)
            .addToBackStack("DetailsFragment").commit()
        // 3 - Main podcast RecyclerView is hidden so
        // only showing the detail Fragment.
        podcastRecyclerView.visibility = View.INVISIBLE
        // 4 - searchMenuItem is hidden so search icon is not shown on details screen.
        searchMenuItem.isVisible = false
    }

    // Lambda method that can respond to changes in Fragment back stack.
    private fun addBackStackListener() {
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                podcastRecyclerView.visibility = View.VISIBLE
            }
        }
    }

    // Helper method to display generic alert dialog with error message.
    // Will handle all error cases.
    private fun showError(message: String) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton(getString(R.string.ok_button), null)
            .create()
            .show()
    }

    // Defines a tag to uniquely identify the details Fragment
    // in the Fragment Manager.
    companion object {
        private const val TAG_DETAILS_FRAGMENT = "DetailsFragment"
    }
}
