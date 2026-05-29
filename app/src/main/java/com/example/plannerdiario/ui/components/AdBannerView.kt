package com.jsjstudios.dailyplanner.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * Banner de anúncio AdMob (tamanho padrão 320×50 dp).
 *
 * 🔧 SUBSTITUA [BANNER_AD_UNIT_ID] pelo seu Ad Unit ID real:
 *    admob.google.com → [seu app] → Unidades de anúncios → + Nova unidade → Banner
 *
 * O ID atual é o ID de TEST do Google (não gera receita real).
 */
private const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111" // ← ID de teste

@Composable
fun AdBannerView(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = BANNER_AD_UNIT_ID
                loadAd(AdRequest.Builder().build())
            }
        },
        // Recarrega quando o composable é recomposto (ex: rotação de tela)
        update = { adView ->
            adView.loadAd(AdRequest.Builder().build())
        }
    )
}

