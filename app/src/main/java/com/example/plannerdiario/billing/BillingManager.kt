package com.jsjstudios.dailyplanner.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.jsjstudios.dailyplanner.data.AdPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Gerencia a compra "Remover Anúncios" via Google Play Billing.
 *
 * ⚠️  CONFIGURAÇÃO NECESSÁRIA NO GOOGLE PLAY CONSOLE:
 *  1. Acesse play.google.com/console → seu app → Monetização → Produtos in-app
 *  2. Crie um produto com ID: "remove_ads"
 *  3. Defina o preço como R$ 9,99
 *  4. Publique na trilha de testes internos antes de testar
 */
class BillingManager(
    private val context: Context,
    private val adPreferences: AdPreferences,
    private val scope: CoroutineScope
) : PurchasesUpdatedListener {

    companion object {
        const val PRODUCT_REMOVE_ADS = "remove_ads"
    }

    private val _adsRemoved = MutableStateFlow(adPreferences.isAdsRemoved())
    val adsRemoved: StateFlow<Boolean> = _adsRemoved

    /** true quando o produto foi carregado com sucesso do Play Store */
    private val _isPurchaseAvailable = MutableStateFlow(false)
    val isPurchaseAvailable: StateFlow<Boolean> = _isPurchaseAvailable

    /** Preço formatado na moeda local do usuário, ex: "R$ 9,99" / "$9.99" / "€8,99" */
    private val _formattedPrice = MutableStateFlow<String?>(null)
    val formattedPrice: StateFlow<String?> = _formattedPrice

    private var productDetails: ProductDetails? = null

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .build()

    init {
        connect()
    }

    // ── Conexão ──────────────────────────────────────────────────────────────

    fun connect() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    scope.launch(Dispatchers.IO) {
                        queryProductDetails()
                        queryExistingPurchases()
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                // A reconexão é feita na próxima chamada a launchPurchaseFlow()
            }
        })
    }

    // ── Detalhes do produto ───────────────────────────────────────────────────

    private suspend fun queryProductDetails() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_REMOVE_ADS)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            ).build()

        val result = billingClient.queryProductDetails(params)
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            productDetails = result.productDetailsList?.firstOrNull()
            _isPurchaseAvailable.value = productDetails != null
            // Extrai o preço formatado na moeda local (ex: "R$ 9,99", "$9.99", "€8,99")
            _formattedPrice.value = productDetails
                ?.oneTimePurchaseOfferDetails
                ?.formattedPrice
        } else {
            _isPurchaseAvailable.value = false
        }
    }

    // ── Verificar compras existentes (restauração automática) ─────────────────

    private suspend fun queryExistingPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        val result = billingClient.queryPurchasesAsync(params)
        result.purchasesList.forEach { purchase ->
            if (purchase.products.contains(PRODUCT_REMOVE_ADS)) {
                processPurchase(purchase)
            }
        }
    }

    // ── Iniciar fluxo de compra ───────────────────────────────────────────────

    fun launchPurchaseFlow(activity: Activity, onError: (String) -> Unit = {}) {
        if (!billingClient.isReady) {
            connect()
            onError("Serviço de compras não disponível. Tente novamente em instantes.")
            return
        }

        val details = productDetails
        if (details == null) {
            // Tenta recarregar os detalhes do produto e informa o usuário
            scope.launch(Dispatchers.IO) { queryProductDetails() }
            onError("Produto não encontrado. Verifique se o app está atualizado na Play Store.")
            return
        }

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .build()
                )
            ).build()

        billingClient.launchBillingFlow(activity, flowParams)
    }

    // ── Callback de atualizações de compra ────────────────────────────────────

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            purchases.forEach { purchase ->
                if (purchase.products.contains(PRODUCT_REMOVE_ADS)) {
                    scope.launch { processPurchase(purchase) }
                }
            }
        }
    }

    // ── Processar / confirmar compra ──────────────────────────────────────────

    private suspend fun processPurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return

        // Reconhecer a compra com o Google Play (obrigatório em até 3 dias)
        if (!purchase.isAcknowledged) {
            val ackParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(ackParams)
        }

        // Persistir e emitir o novo estado
        adPreferences.setAdsRemoved(true)
        _adsRemoved.value = true
    }

    fun destroy() {
        if (billingClient.isReady) billingClient.endConnection()
    }
}
