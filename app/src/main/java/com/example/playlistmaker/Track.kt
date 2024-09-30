package com.example.playlistmaker

import java.io.Serializable

data class Track(
    val trackName: String,
    val artistName: String,
    val trackTimeMillis: Long,
    val artworkUrl100: String,
    val trackId: Long
) : Serializable