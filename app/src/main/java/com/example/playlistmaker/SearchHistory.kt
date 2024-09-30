package com.example.playlistmaker

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistory(private val sharedPreferences: SharedPreferences) {

    companion object {
        private const val LIST_SIZE = 10
        private const val HISTORY_KEY = "search_history"

    }

    private val gson = Gson()

    fun getHistory(): List<Track> {
        val historyJson = sharedPreferences.getString(HISTORY_KEY, null)
        return if (historyJson != null) {
            val type = object : TypeToken<List<Track>>() {}.type
            gson.fromJson(historyJson, type)
        } else {
            emptyList()
        }
    }


    fun addTrackToHistory(track: Track) {
        val history = getHistory().toMutableList()
        history.removeAll { it.trackId == track.trackId }
        history.add(0, track)

        if (history.size > LIST_SIZE) {
            history.removeAt(history.lastIndex)
        }

        saveHistory(history)
    }

    private fun saveHistory(history: List<Track>) {
        val historyJson = gson.toJson(history)
        sharedPreferences.edit().putString(HISTORY_KEY, historyJson).apply()
    }

    fun clearHistory() {
        saveHistory(emptyList())
    }
}