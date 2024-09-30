package com.example.playlistmaker

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : AppCompatActivity() {
    private lateinit var placeholderImage: ImageView
    private lateinit var placeholderText: TextView
    private lateinit var placeholderButton: Button
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var historyAdapter: SearchAdapter
    private lateinit var trackHistoryLayout: LinearLayout
    private lateinit var clearHistoryButton: Button
    private lateinit var searchHistoryTitle: TextView

    private var trackList = mutableListOf<Track>()
    private var historyList = mutableListOf<Track>()
    private var lastSearchQuery: String? = null
    private lateinit var searchHistory: SearchHistory

    private val iTunesBaseUrl = "https://itunes.apple.com/"
    private val retrofit = Retrofit.Builder()
        .baseUrl(iTunesBaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val iTunesSearchAPIInterface = retrofit.create(ITunesSearchAPI::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        searchHistory = SearchHistory(getSharedPreferences(PREFS, MODE_PRIVATE))

        val backButton = findViewById<ImageView>(R.id.ic_back_button_searchAct)
        backButton.setOnClickListener { finish() }

        val inputEditText = findViewById<EditText>(R.id.inputEditText)
        val clearButton = findViewById<ImageView>(R.id.clear_button)

        trackHistoryLayout = findViewById(R.id.trackHistoryLayout)
        clearHistoryButton = findViewById<Button>(R.id.clearHistoryButton)
        searchHistoryTitle = findViewById<TextView>(R.id.searchHistoryTitle)

        placeholderImage = findViewById(R.id.placeholderImage)
        placeholderText = findViewById(R.id.placeholderText)
        placeholderButton = findViewById(R.id.placeholderButton)

        val recyclerViewSearch = findViewById<RecyclerView>(R.id.recyclerViewSearch)
        searchAdapter = SearchAdapter(trackList) { track -> onTrackClick(track) }
        recyclerViewSearch.adapter = searchAdapter
        recyclerViewSearch.layoutManager = LinearLayoutManager(this)

        val recyclerViewHistory = findViewById<RecyclerView>(R.id.recyclerViewHistory)
        historyAdapter = SearchAdapter(historyList) { track -> onTrackClick(track) }
        recyclerViewHistory.adapter = historyAdapter
        recyclerViewHistory.layoutManager = LinearLayoutManager(this)

        loadHistory()

        clearButton.setOnClickListener {
            inputEditText.setText("")
            trackList.clear()
            searchAdapter.updateTracks(trackList)
            recyclerViewSearch.visibility = View.GONE
            hidePlaceholder()
            inputEditText.clearFocus()
            hideKeyboard(inputEditText)
            loadHistory()
        }

        inputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                trackHistoryLayout.visibility = View.VISIBLE
                clearHistoryButton.visibility = View.VISIBLE
                trackHistoryLayout.visibility = View.VISIBLE
                searchHistoryTitle.visibility = View.VISIBLE
                recyclerViewHistory.visibility = View.VISIBLE
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE

                if (s.isNullOrEmpty()) {
                    loadHistory()
                    trackHistoryLayout.visibility = View.VISIBLE
                } else {
                    trackHistoryLayout.visibility = View.GONE
                    clearHistoryButton.visibility = View.GONE
                    trackHistoryLayout.visibility = View.GONE
                    searchHistoryTitle.visibility = View.GONE
                    recyclerViewHistory.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        inputEditText.setOnEditorActionListener { _, actionId, event ->
            val isDoneAction = actionId == EditorInfo.IME_ACTION_DONE ||
                    (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)

            if (isDoneAction) {
                val query = inputEditText.text.toString().trim()
                if (query.isNotEmpty()) {
                    trackSearch(query)
                    lastSearchQuery = query
                    trackHistoryLayout.visibility = View.GONE
                }
                true
            } else {
                false
            }
        }

        placeholderButton.setOnClickListener {
            lastSearchQuery?.let { query -> trackSearch(query) }
        }

        clearHistoryButton.setOnClickListener {
            searchHistory.clearHistory()
            loadHistory()
        }
    }

    private fun trackSearch(query: String) {
        iTunesSearchAPIInterface.search(query).enqueue(object : Callback<TrackResponse> {
            override fun onResponse(call: Call<TrackResponse>, response: Response<TrackResponse>) {
                val results = response.body()?.results
                if (response.isSuccessful && results?.isNotEmpty() == true) {
                    trackList.clear()
                    trackList.addAll(results)
                    searchAdapter.updateTracks(trackList)
                    findViewById<RecyclerView>(R.id.recyclerViewSearch).visibility = View.VISIBLE
                    hidePlaceholder()
                } else {
                    trackList.clear()
                    searchAdapter.updateTracks(trackList)
                    showPlaceholderNothingWasFound()
                }
            }

            override fun onFailure(call: Call<TrackResponse>, t: Throwable) {
                findViewById<RecyclerView>(R.id.recyclerViewSearch).visibility = View.GONE
                trackList.clear()
                searchAdapter.updateTracks(trackList)
                showPlaceholderInternetProblems()
            }
        })
    }

    private fun loadHistory() {
        historyList.clear()
        historyList.addAll(searchHistory.getHistory())
        if (historyList.isNotEmpty()) {
            historyAdapter.updateTracks(historyList)
            trackHistoryLayout.isVisible = true
        } else {
            trackHistoryLayout.isVisible = false
        }
    }

    private fun hidePlaceholder() {
        placeholderText.visibility = View.GONE
        placeholderImage.visibility = View.GONE
        placeholderButton.visibility = View.GONE
    }

    private fun showPlaceholderNothingWasFound() {
        placeholderImage.setImageResource(R.drawable.ic_search_fail)
        placeholderText.text = getString(R.string.nothing_was_found)
        placeholderButton.visibility = View.GONE
        placeholderImage.visibility = View.VISIBLE
        placeholderText.visibility = View.VISIBLE
    }

    private fun showPlaceholderInternetProblems() {
        placeholderImage.setImageResource(R.drawable.ic_internet_error)
        placeholderText.text = getString(R.string.internet_problems)
        placeholderButton.visibility = View.VISIBLE
        placeholderImage.visibility = View.VISIBLE
        placeholderText.visibility = View.VISIBLE
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun onTrackClick(track: Track) {
        searchHistory.addTrackToHistory(track)
        loadHistory()
    }

    companion object {
        private const val PREFS = "PREFS"
    }
}