package com.example.playlistmaker

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

    private var currentSearchQuery = ""
    private var trackList = arrayListOf<Track>()
    private var lastSearchQuery: String? = null

    private val iTunesBaseUrl = "https://itunes.apple.com/"
    private val retrofit = Retrofit.Builder()
        .baseUrl(iTunesBaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val iTunesSearchAPIInterface = retrofit.create(ITunesSearchAPI::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val backButton = findViewById<ImageView>(R.id.ic_back_button_searchAct)
        backButton.setOnClickListener { finish() }

        val inputEditText = findViewById<EditText>(R.id.inputEditText)
        val clearButton = findViewById<ImageView>(R.id.clear_button)

        placeholderImage = findViewById(R.id.placeholderImage)
        placeholderText = findViewById(R.id.placeholderText)
        placeholderButton = findViewById(R.id.placeholderButton)

        val recyclerViewSearch = findViewById<RecyclerView>(R.id.recyclerViewSearch)
        searchAdapter = SearchAdapter(trackList)
        recyclerViewSearch.adapter = searchAdapter

        clearButton.setOnClickListener {
            inputEditText.setText("")
            trackList.clear()
            searchAdapter.notifyDataSetChanged()
            recyclerViewSearch.visibility = View.GONE
            hidePlaceholder()
            inputEditText.clearFocus()
            hideKeyboard(inputEditText)
        }

        inputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        inputEditText.setOnEditorActionListener { _, actionId, event ->
            val isDoneAction = actionId == EditorInfo.IME_ACTION_DONE ||
                    (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)

            if (isDoneAction) {
                val query = inputEditText.text.toString().trim()
                if (query.isNotEmpty()) trackSearch(query)
                true
            } else {
                false
            }
        }

        placeholderButton.setOnClickListener {
            lastSearchQuery?.let { query -> trackSearch(query) }
        }

        if (savedInstanceState != null) {
            currentSearchQuery = savedInstanceState.getString(KEY_SAVE_SEARCH, "")
            inputEditText.setText(currentSearchQuery)

            val restoredList = savedInstanceState.getSerializable(KEY_TRACK_LIST) as? ArrayList<Track>
            if (restoredList != null) {
                trackList.clear()
                trackList.addAll(restoredList)
                searchAdapter.notifyDataSetChanged()

                if (trackList.isNotEmpty()) {
                    recyclerViewSearch.visibility = View.VISIBLE
                    hidePlaceholder()
                }
            }
        }
    }

    private fun trackSearch(query: String) {
        lastSearchQuery = query
        iTunesSearchAPIInterface.search(query).enqueue(object : Callback<TrackResponse> {
            override fun onResponse(call: Call<TrackResponse>, response: Response<TrackResponse>) {
                if (response.isSuccessful && response.code() == 200) {
                    val responseBody = response.body()
                    if (responseBody != null && responseBody.results.isNotEmpty()) {
                        trackList.clear()
                        trackList.addAll(responseBody.results)
                        searchAdapter.notifyDataSetChanged()
                        findViewById<RecyclerView>(R.id.recyclerViewSearch).visibility = View.VISIBLE
                        hidePlaceholder()
                    } else {
                        trackList.clear()
                        searchAdapter.notifyDataSetChanged()
                        showPlaceholderNothingWasFound()
                    }
                } else {
                    showPlaceholderInternetProblems()
                }
            }

            override fun onFailure(call: Call<TrackResponse>, t: Throwable) {
                findViewById<RecyclerView>(R.id.recyclerViewSearch).visibility = View.GONE
                trackList.clear()
                searchAdapter.notifyDataSetChanged()
                showPlaceholderInternetProblems()
            }
        })
    }

    private fun hidePlaceholder() {
        placeholderText.visibility = View.GONE
        placeholderImage.visibility = View.GONE
        placeholderButton.visibility = View.GONE
    }

    private fun showPlaceholderNothingWasFound() {
        findViewById<RecyclerView>(R.id.recyclerViewSearch).visibility = View.GONE
        placeholderText.text = getString(R.string.nothing_was_found)
        placeholderImage.setImageResource(R.drawable.ic_search_fail)
        placeholderText.visibility = View.VISIBLE
        placeholderImage.visibility = View.VISIBLE
        placeholderButton.visibility = View.GONE
    }

    private fun showPlaceholderInternetProblems() {
        findViewById<RecyclerView>(R.id.recyclerViewSearch).visibility = View.GONE
        placeholderImage.setImageResource(R.drawable.ic_internet_error)
        placeholderText.text = getString(R.string.internet_problems)
        placeholderImage.visibility = View.VISIBLE
        placeholderText.visibility = View.VISIBLE
        placeholderButton.visibility = View.VISIBLE
        findViewById<ImageView>(R.id.clear_button).visibility = View.GONE

    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_SAVE_SEARCH, currentSearchQuery)
        outState.putSerializable(KEY_TRACK_LIST, trackList)
    }

    companion object {
        private const val KEY_SAVE_SEARCH = "KEY_SAVE_SEARCH"
        private const val KEY_TRACK_LIST = "KEY_TRACK_LIST"
    }
}
