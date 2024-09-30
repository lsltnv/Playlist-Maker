package com.example.playlistmaker

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import java.util.Locale

class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val artworkUrl100IdImageView: ImageView = itemView.findViewById(R.id.artworkUrl100)
    private val trackNameIdTextView: TextView = itemView.findViewById(R.id.trackName)
    private val artistNameIdTextView: TextView = itemView.findViewById(R.id.artistName)
    private val trackTimeIdTextView: TextView = itemView.findViewById(R.id.trackTime)

    private val cornerRadiusPx = dpToPx(8f, itemView.context)

    fun bind(track: Track) {
        trackNameIdTextView.text = track.trackName
        artistNameIdTextView.text = track.artistName
        trackTimeIdTextView.text = formatTrackTime(track.trackTimeMillis.toInt())

        Glide.with(itemView.context)
            .load(track.artworkUrl100)
            .placeholder(R.drawable.ic_placeholder)
            .transform(RoundedCorners(cornerRadiusPx))
            .into(artworkUrl100IdImageView)
    }

    private fun formatTrackTime(trackTimeMillis: Int): String {
        val seconds = (trackTimeMillis / 1000) % 60
        val minutes = (trackTimeMillis / (1000 * 60)) % 60
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
    }

    private fun dpToPx(dp: Float, context: Context): Int {
        val metrics = context.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics).toInt()
    }
}
