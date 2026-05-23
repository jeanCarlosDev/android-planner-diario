package com.example.plannerdiario

import android.os.Bundle
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
import com.example.plannerdiario.data.LanguagePreferences
import com.example.plannerdiario.data.ThemePreferences
import com.example.plannerdiario.ui.HomeScreen
import com.example.plannerdiario.ui.PlannerViewModel
import com.example.plannerdiario.ui.PlannerViewModelFactory
import com.example.plannerdiario.ui.theme.PlannerDiarioTheme
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var langPrefs: LanguagePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        langPrefs = LanguagePreferences(applicationContext)

        // Restaura o idioma salvo em versões anteriores ao Android 13
        val savedTag = langPrefs.getCurrentTag()
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(savedTag))

        super.onCreate(savedInstanceState)

        val app       = application as PlannerApplication
        val themePrefs = ThemePreferences(applicationContext)

        setContent {
            val isDark by themePrefs.isDarkFlow.collectAsStateWithLifecycle(initialValue = false)
            val scope  = rememberCoroutineScope()

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
                        }
                    )
                }
            }
        }
    }
}
