package com.example.magnifyingglass.magnifier.Language

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.magnifyingglass.magnifier.ui.activites.MainScreen
import com.example.magnifyingglass.magnifier.R
import com.example.magnifyingglass.magnifier.ads.loadAndShowNativeAd
import com.example.magnifyingglass.magnifier.ads.showLoadedNativeAd
import com.example.magnifyingglass.magnifier.ads.showPriorityAdmobInterstitial
import com.example.magnifyingglass.magnifier.databinding.ActivityLanguageBinding
import com.example.magnifyingglass.magnifier.ui.activites.BaseActivity
import com.example.magnifyingglass.magnifier.utils.isInternetConnected
import com.example.magnifyingglass.magnifier.utils.languagesNativeAd
import com.example.magnifyingglass.magnifier.utils.showToast


class LanguageActivity : BaseActivity(), OnItemClickListener {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var adapter:LanguageAdapter
    val binding:ActivityLanguageBinding by lazy{
        ActivityLanguageBinding.inflate(layoutInflater)
    }
    private lateinit var mainIntent: Intent
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        showNative()

        mainIntent = Intent(this, MainScreen::class.java)
        sharedPreferences = getSharedPreferences("language_prefs", Context.MODE_PRIVATE)

        if (sharedPreferences.getBoolean("language_shown", false)) {
            // Language is already selected, navigate to MainActivity
            navigateToMainActivity()
            return
        }
        val list = ArrayList<LanguageModel>()
        list.add(LanguageModel( "English (Default)"))
        list.add(LanguageModel( "Arabic"))
        list.add(LanguageModel( "Chinese"))
        list.add(LanguageModel( "Hindi"))
        list.add(LanguageModel( "Urdu"))
        list.add(LanguageModel( "Malay"))
        list.add(LanguageModel( "Japanese"))
        list.add(LanguageModel( "Spanish"))
        list.add(LanguageModel( "Turkish"))
        list.add(LanguageModel( "Bengali"))


        adapter = LanguageAdapter(list,this)
        binding.recyclerview.adapter = adapter
    }
    override fun onItemClick(position: Int) {
        val selectedLanguage = when (position) {
            0 -> "en"
            1 -> "ar"
            2 -> "ch"
            3 -> "hi"
            4 -> "ur"
            5 -> "ms"
            6 -> "ja"
            7 -> "es"
            8 -> "tr"
            9 -> "bn"
            else -> "en"
        }

        LocaleHelper.setLocale(this, selectedLanguage)

        // Store the selected language in SharedPreferences
        sharedPreferences.edit().putString("selected_language", selectedLanguage).apply()

        // Navigate to MainActivity
        navigateToMainActivity()
    }
    private fun navigateToMainActivity() {
        startActivity(mainIntent)
        finishAffinity()
    }
    private fun showNative() {
        if (isInternetConnected() && remoteConfigViewModel.getRemoteConfig(this@LanguageActivity)?.languagesNative?.value == 1){

                loadAndShowNativeAd(
                    binding.layoutNative,
                    R.layout.native_ad_layout_small,
                    getString(R.string.languagesNativeId)
                )
        }else{
            binding.layoutNative.visibility = View.GONE
        }
    }


    override fun onBackPressed() {
        navigateToMainActivity()
    }


}
