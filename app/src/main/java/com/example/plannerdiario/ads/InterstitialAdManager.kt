package com.example.plannerdiario.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Gerencia o anúncio intersticial (vídeo) exibido a cada [INTERACTIONS_PER_AD] interações.
 *
 * 🔧 SUBSTITUA [INTERSTITIAL_AD_UNIT_ID] pelo seu Ad Unit ID real antes de publicar:
 *    admob.google.com → [seu app] → Unidades de anúncios → + Nova unidade → Intersticial
 *
 * O ID atual é o ID de TESTE do Google (não gera receita real).
 */
private const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712" // ← teste
private const val INTERACTIONS_PER_AD = 10

class InterstitialAdManager(private val context: Context) {

    private var loadedAd: InterstitialAd? = null
    private var isLoading = false
    private var interactionCount = 0

    init {
        preloadAd()
    }

    // ── Pré-carrega o anúncio em segundo plano ────────────────────────────────

    private fun preloadAd() {
        if (isLoading || loadedAd != null) return
        isLoading = true

        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    loadedAd = ad
                    isLoading = false

                    // Configura callbacks para após a exibição
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            // Usuário fechou → já recarrega o próximo
                            loadedAd = null
                            preloadAd()
                        }
                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            loadedAd = null
                            preloadAd()
                        }
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    loadedAd = null
                    isLoading = false
                    // Não torna a tentativa agressiva; o próximo trackInteraction tentará novamente
                }
            }
        )
    }

    // ── Registra uma interação e exibe o anúncio se necessário ────────────────

    /**
     * Chame este método a cada ação relevante do usuário (criar tarefa, concluir, etc.).
     * A cada [INTERACTIONS_PER_AD] chamadas, o anúncio intersticial é exibido.
     *
     * @param activity Activity atual necessária para exibir o anúncio fullscreen.
     */
    fun trackInteraction(activity: Activity) {
        interactionCount++

        if (interactionCount % INTERACTIONS_PER_AD == 0) {
            showAd(activity)
        } else if (loadedAd == null) {
            // Garante que sempre há um anúncio sendo carregado em segundo plano
            preloadAd()
        }
    }

    private fun showAd(activity: Activity) {
        val ad = loadedAd
        if (ad != null) {
            ad.show(activity)
        } else {
            // Anúncio não disponível ainda → carrega para a próxima oportunidade
            preloadAd()
        }
    }
}


