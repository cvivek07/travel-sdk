package com.ixigo.sdk.webview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ixigo.sdk.flights.databinding.WebActivityBinding

class WebActivity: AppCompatActivity(), WebViewFragmentDelegate {

    private lateinit var binding: WebActivityBinding
    private lateinit var webViewFragment: WebViewFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webViewFragment = WebViewFragment()
        webViewFragment.arguments = intent.extras
        webViewFragment.delegate = this

        binding = WebActivityBinding.inflate(layoutInflater)

        supportFragmentManager.beginTransaction()
            .add(binding.fragmentContainerView.id, webViewFragment)
            .commit()
        setContentView(binding.root)

        supportFragmentManager.executePendingTransactions()
    }

    override fun quit() {
        finish()
    }
}
