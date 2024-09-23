package com.example.playlistmaker

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.net.URL

class SettingsActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)


        val backButton = findViewById<ImageView>(R.id.ic_back_button)
        backButton.setOnClickListener {
            this.finish()
        }



        val shareButton = findViewById<ImageView>(R.id.icon_share_buttone)
        shareButton.setOnClickListener {
            val share = Intent(Intent.ACTION_SEND)
            share.type = "text/plain"
            share.putExtra(Intent.EXTRA_TEXT, getString(R.string.Url_practicum))
            startActivity(share)
        }
        val supportButton = findViewById<ImageView>(R.id.support)
        supportButton.setOnClickListener {
            val support = Intent(Intent.ACTION_SENDTO)
            support.data = Uri.parse("mailto:")
            support.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.email)))
            support.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_default_message))
            support.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_title))
            startActivity(support)
        }

        val agreementButton = findViewById<ImageView>(R.id.agreement)
        agreementButton.setOnClickListener {
            val agreement = Intent(Intent.ACTION_SEND)
            agreement.type = "text/plain"
            agreement.putExtra(Intent.EXTRA_TEXT, getString(R.string.user_agreement_url))
            startActivity(agreement)

        }

    }

}