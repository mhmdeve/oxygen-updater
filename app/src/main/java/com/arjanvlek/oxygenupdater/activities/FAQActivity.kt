package com.arjanvlek.oxygenupdater.activities

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import com.arjanvlek.oxygenupdater.BuildConfig
import com.arjanvlek.oxygenupdater.OxygenUpdater
import com.arjanvlek.oxygenupdater.R
import com.arjanvlek.oxygenupdater.internal.WebViewClient
import com.arjanvlek.oxygenupdater.utils.ThemeUtils
import kotlinx.android.synthetic.main.activity_faq.*

class FAQActivity : SupportActionBarActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) = super.onCreate(savedInstanceState).also {
        setContentView(R.layout.activity_faq)

        swipeRefreshLayout.apply {
            setOnRefreshListener { loadFaqPage() }
            setColorSchemeResources(R.color.colorPrimary)
            // needs to be done as a workaround to WebView not being able to scroll up if it's not a direct child of a SwipeRefreshLayout
            setOnChildScrollUpCallback { _, _ ->
                // allow scrolling up (and thus, disable the swipe-to-refresh gesture) only if:
                // 1. currently displayed view is a WebView,
                // 2. and this WebView is not at the topmost Y position
                webView.isVisible && webView.scrollY != 0
            }
        }

        loadFaqPage()
    }

    override fun onBackPressed() = finish()

    /**
     * Respond to the action bar's Up/Home button.
     * Delegate to [onBackPressed] if [android.R.id.home] is clicked, otherwise call `super`
     */
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> onBackPressed().let { true }
        else -> super.onOptionsItemSelected(item)
    }

    /**
     * Loads the FAQ page, or displays a No Network connection screen if there is no network connection
     */
    @SuppressLint("SetJavaScriptEnabled") // JavaScript is required to toggle the FAQ Item boxes.
    private fun loadFaqPage() {
        webView.apply {
            // must be done to avoid the white background in dark themes
            setBackgroundColor(Color.TRANSPARENT)

            // since we can't edit CSS in WebViews,
            // append 'Light' or 'Dark' to faqServerUrl to get the corresponding themed version
            // backend handles CSS according to material spec
            val faqServerUrl = BuildConfig.FAQ_SERVER_URL + "/" + if (ThemeUtils.isNightModeActive(context)) "Dark" else "Light"

            settings.javaScriptEnabled = true
            settings.userAgentString = OxygenUpdater.APP_USER_AGENT
            clearCache(true)
            loadUrl(faqServerUrl).also { swipeRefreshLayout.isRefreshing = true }

            // disable loading state once page is completely loaded
            webViewClient = WebViewClient(context) { error ->
                // hide progress bar since the page has been loaded
                swipeRefreshLayout.isRefreshing = false

                if (error == null) {
                    // Show WebView
                    webView.isVisible = true
                    // Hide error layout
                    errorLayout.isVisible = false
                } else {
                    // Hide WebView
                    webView.isVisible = false
                    // Show error layout
                    errorLayout.isVisible = true

                    errorTitle.text = error.errorCodeString
                }
            }
        }
    }

    /**
     * Handler for the "Retry" button
     *
     * @param v View
     */
    @Suppress("UNUSED_PARAMETER", "unused")
    fun onRetryButtonClick(v: View?) = loadFaqPage()
}
