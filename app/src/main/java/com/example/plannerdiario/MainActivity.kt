package com.jsjstudios.dailyplanner

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jsjstudios.dailyplanner.ads.InterstitialAdManager
import com.jsjstudios.dailyplanner.billing.BillingManager
import com.jsjstudios.dailyplanner.data.AdPreferences
import com.jsjstudios.dailyplanner.data.LanguagePreferences
import com.jsjstudios.dailyplanner.data.ThemePreferences
import com.jsjstudios.dailyplanner.ui.HomeScreen
import com.jsjstudios.dailyplanner.ui.PlannerViewModel
import com.jsjstudios.dailyplanner.ui.PlannerViewModelFactory
import com.jsjstudios.dailyplanner.ui.theme.PlannerDiarioTheme
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var langPrefs: LanguagePreferences
    private lateinit var billingManager: BillingManager
    private lateinit var interstitialAdManager: InterstitialAdManager
    private val activityScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        langPrefs = LanguagePreferences(applicationContext)

        // Restaura o idioma salvo em versões anteriores ao Android 13
        val savedTag = langPrefs.getCurrentTag()
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(savedTag))

        super.onCreate(savedInstanceState)

        val app        = application as PlannerApplication
        val themePrefs = ThemePreferences(applicationContext)
        val adPrefs    = AdPreferences(applicationContext)

        // Inicializa o AdMob de forma assíncrona (não bloqueia a UI)
        MobileAds.initialize(this)

        // Inicializa o gerenciador de compras
        billingManager = BillingManager(applicationContext, adPrefs, activityScope)

        // Pré-carrega o anúncio intersticial em segundo plano
        interstitialAdManager = InterstitialAdManager(applicationContext)

        setContent {
            val isDark     by themePrefs.isDarkFlow.collectAsStateWithLifecycle(initialValue = false)
            val adsRemoved by billingManager.adsRemoved.collectAsStateWithLifecycle()
            val purchasePrice by billingManager.formattedPrice.collectAsStateWithLifecycle()
            val scope      = rememberCoroutineScope()

            var currentLanguageTag by remember { mutableStateOf(langPrefs.getCurrentTag()) }

            PlannerDiarioTheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val viewModel: PlannerViewModel = viewModel(
                        factory = PlannerViewModelFactory(app.repository)
                    )
                    HomeScreen(
                        viewModel          = viewModel,
                        currentLanguageTag = currentLanguageTag,
                        onToggleDark       = {
                            scope.launch { themePrefs.setDarkMode(!isDark) }
                        },
                        onChangeLanguage   = { tag ->
                            if (tag != currentLanguageTag) {
                                langPrefs.setTag(tag)
                                currentLanguageTag = tag
                                AppCompatDelegate.setApplicationLocales(
                                    LocaleListCompat.forLanguageTags(tag)
                                )
                            }
                        },
                        adsRemoved    = adsRemoved,
                        purchasePrice = purchasePrice,
                        onRemoveAds   = {
                            billingManager.launchPurchaseFlow(this@MainActivity) { errorMsg ->
                                Toast.makeText(this@MainActivity, errorMsg, Toast.LENGTH_LONG).show()
                            }
                        },
                        // Só rastreia interações se o usuário não removeu os anúncios
                        onInteraction = {
                            if (!adsRemoved) {
                                interstitialAdManager.trackInteraction(this@MainActivity)
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        billingManager.destroy()
    }
}
