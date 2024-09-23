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
    private val artworkUrl100IdImageView = itemView.findViewById<ImageView>(R.id.artworkUrl100)
    private val trackNameIdTextView = itemView.findViewById<TextView>(R.id.trackName)
    private val artistNameIdTextView = itemView.findViewById<TextView>(R.id.artistName)
    private val trackTimeIdTextView = itemView.findViewById<TextView>(R.id.trackTime)

    private val cornerRadiusPx = dpToPx(
        itemView.context.resources.getDimension(R.dimen.corner_radius_artworkUrl100),
        itemView.context
    )

    fun bind(track: Track) {
        Glide.with(itemView)
            .load(track.artworkUrl100)
            .placeholder(R.drawable.ic_placeholder)
            .transform(RoundedCorners(cornerRadiusPx))
            .into(artworkUrl100IdImageView)

        trackNameIdTextView.text = track.trackName
        artistNameIdTextView.text = track.artistName

        val minutes = (track.trackTimeMillis / 1000) / 60
        val seconds = (track.trackTimeMillis / 1000) % 60
        trackTimeIdTextView.text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    private fun dpToPx(dp: Float, context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        ).toInt()
    }
}
