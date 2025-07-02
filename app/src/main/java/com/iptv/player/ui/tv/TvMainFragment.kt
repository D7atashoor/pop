package com.iptv.player.ui.tv

import android.os.Bundle
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import com.iptv.player.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TvMainFragment : BrowseSupportFragment() {
    
    private lateinit var rowsAdapter: ArrayObjectAdapter
    
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        
        setupUI()
        setupRowAdapter()
        setupEventListeners()
    }
    
    private fun setupUI() {
        title = getString(R.string.app_name)
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true
        
        // Set brand color for search icon
        brandColor = resources.getColor(R.color.purple_500, null)
        
        // Set search icon color
        searchAffordanceColor = resources.getColor(R.color.teal_200, null)
    }
    
    private fun setupRowAdapter() {
        rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        adapter = rowsAdapter
        
        createRows()
    }
    
    private fun createRows() {
        val sourceHeader = HeaderItem(0, "المصادر")
        val sourcePresenter = CardPresenter()
        val sourceAdapter = ArrayObjectAdapter(sourcePresenter)
        
        // Add sample data or load from repository
        val sourceRow = ListRow(sourceHeader, sourceAdapter)
        rowsAdapter.add(sourceRow)
        
        val channelHeader = HeaderItem(1, "القنوات")
        val channelPresenter = CardPresenter()
        val channelAdapter = ArrayObjectAdapter(channelPresenter)
        
        val channelRow = ListRow(channelHeader, channelAdapter)
        rowsAdapter.add(channelRow)
    }
    
    private fun setupEventListeners() {
        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            // Handle item clicks
        }
    }
}