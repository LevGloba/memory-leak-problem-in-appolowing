package com.example.demoapploving

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxError
import com.applovin.mediation.MaxReward
import com.applovin.mediation.MaxRewardedAdListener
import com.applovin.mediation.ads.MaxRewardedAd
import com.applovin.sdk.AppLovinMediationProvider
import com.applovin.sdk.AppLovinPrivacySettings
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkInitializationConfiguration
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit
import kotlin.math.pow

class MainActivity : AppCompatActivity(), MaxRewardedAdListener {

    private var rewardedAd: MaxRewardedAd? = null
    private var retryAttempt = .0

    private lateinit var textViewClickToLoadAd: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        iniApplovin()

        textViewClickToLoadAd = findViewById(R.id.text_view_click_to_load_ad)
        progressBar = findViewById(R.id.progress_bar)

        textViewClickToLoadAd.setOnClickListener {
            Log.i("ad", "setOnClickListener")
            progressBar.visibility = View.VISIBLE
            textViewClickToLoadAd.isVisible = false

            if (rewardedAd == null)
                rewardedAd = MaxRewardedAd.getInstance("1d62dd7832dd1574", this).apply {

                    setListener(WeakReference(this@MainActivity).get())
                }

            rewardedAd?.loadAd()
        }
    }

    private fun iniApplovin() {
        val initConfig = AppLovinSdkInitializationConfiguration.builder(
            "3B9PbJXIwBhOpKNJsw4gmg0buI0GLLOslfGNdNA7zb8OnBRk4mBr96iUTG4aFtfWmRdaNDgfLGIi_Gn9vInc8k",
            this
        )
            .setMediationProvider(AppLovinMediationProvider.MAX)
            //.testDeviceAdvertisingIds = ""
            .build()

        // Configure the SDK settings if needed before or after SDK initialization.
        val settings = AppLovinSdk.getInstance(this).settings
        settings.run {
            termsAndPrivacyPolicyFlowSettings.apply {
                isEnabled = true
                privacyPolicyUri = Uri.parse("«https://your_company_name.com/privacy_policy»")

                // Terms of Service URL is optional
                termsOfServiceUri = Uri.parse("«https://your_company_name.com/terms_of_service»")
            }
        }

        AppLovinPrivacySettings.setHasUserConsent( true, this )

        // Initialize the SDK with the configuration
        AppLovinSdk.getInstance(this).initialize(initConfig) {}
    }

    override fun onAdLoaded(p0: MaxAd) {
        Log.i("ad", "onAdLoaded")
        progressBar.visibility = View.GONE

        rewardedAd?.showAd(this)
    }

    override fun onAdDisplayed(p0: MaxAd) {
        Log.i("ad", "onAdDisplayed")
    }

    override fun onAdHidden(p0: MaxAd) {
        Log.i("ad", "onAdHidden")
        destroyRewarded()
        textViewClickToLoadAd.isVisible = true
    }

    override fun onAdClicked(p0: MaxAd) {
        Log.i("ad", "onAdClicked")
    }

    override fun onAdLoadFailed(p0: String, p1: MaxError) {
        Log.i("ad", "onAdLoadFailed")
        repeatBackgroundWork()
    }

    override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
        Log.i("ad", "onAdDisplayFailed")
        repeatBackgroundWork()
    }

    override fun onUserRewarded(p0: MaxAd, p1: MaxReward) {
        Log.i("ad", "onUserRewarded")
    }

    private fun destroyRewarded() {

        rewardedAd?.destroy()

        rewardedAd = null
    }

    private fun repeatBackgroundWork() {
        if (retryAttempt < 3) {
            runBlocking {
                delay(TimeUnit.SECONDS.toMillis(2.0.pow(6.0.coerceAtMost(retryAttempt)).toLong()))
            }
            retryAttempt++
            rewardedAd?.loadAd()
        } else {
            retryAttempt = .0
            destroyRewarded()
            textViewClickToLoadAd.isVisible = true
            progressBar.visibility = View.VISIBLE
        }
    }
}