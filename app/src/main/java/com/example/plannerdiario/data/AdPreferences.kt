package com.example.plannerdiario.data

import android.content.Context

/**
 * Persiste localmente se o usuário já removeu os anúncios via compra.
 * A verificação autoritativa vem do Google Play Billing (BillingManager).
 */
class AdPreferences(context: Context) {

    private val prefs = context.getSharedPreferences("ad_prefs", Context.MODE_PRIVATE)

    fun isAdsRemoved(): Boolean = prefs.getBoolean("ads_removed", false)

    fun setAdsRemoved(removed: Boolean) {
        prefs.edit().putBoolean("ads_removed", removed).apply()
    }
}

