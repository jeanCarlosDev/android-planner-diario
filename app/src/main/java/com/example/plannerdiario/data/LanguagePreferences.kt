package com.jsjstudios.dailyplanner.data

import android.content.Context

/**
 * Preferências de idioma usando SharedPreferences para leitura síncrona
 * na inicialização da Activity (necessário antes do setContent).
 */
class LanguagePreferences(context: Context) {

    private val prefs = context.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)

    /** Tag BCP-47 do idioma atual. Padrão: "pt-BR". */
    fun getCurrentTag(): String = prefs.getString(KEY_TAG, DEFAULT_TAG) ?: DEFAULT_TAG

    /** Persiste a nova tag de idioma. */
    fun setTag(tag: String) = prefs.edit().putString(KEY_TAG, tag).apply()

    companion object {
        private const val KEY_TAG     = "language_tag"
        const val DEFAULT_TAG         = "pt-BR"
        const val TAG_PT_BR           = "pt-BR"
        const val TAG_EN_US           = "en-US"
    }
}

