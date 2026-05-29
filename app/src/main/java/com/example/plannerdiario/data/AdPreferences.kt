package com.jsjstudios.dailyplanner.data

import android.content.Context

/**
 * Persiste localmente se o usuário já removeu os anúncios via compra.
 * A verificação autoritativa vem do Google Play Billing (BillingManager).
 *
 * ⚠️ TESTE: altere DEBUG_SIMULATE_PURCHASE para false antes de publicar na Play Store.
 */
private const val DEBUG_SIMULATE_PURCHASE = false  // ← false para produção

class AdPreferences(context: Context) {

    private val prefs = context.getSharedPreferences("ad_prefs", Context.MODE_PRIVATE)

    fun isAdsRemoved(): Boolean = DEBUG_SIMULATE_PURCHASE || prefs.getBoolean("ads_removed", false)

    fun setAdsRemoved(removed: Boolean) {
        prefs.edit().putBoolean("ads_removed", removed).apply()
    }
}

