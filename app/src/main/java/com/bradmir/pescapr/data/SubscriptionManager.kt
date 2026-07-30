package com.bradmir.pescapr.data

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SubscriptionManager(
    private val context: Context,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance("pescapr")
) : PurchasesUpdatedListener {

    private val TAG = "SubscriptionManager"

    private val _isProUser = MutableStateFlow(false)
    val isProUser: StateFlow<Boolean> = _isProUser.asStateFlow()

    private var billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    fun startConnection(onConnected: () -> Unit = {}) {
        if (billingClient.isReady) {
            onConnected()
            return
        }

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing service connected successfully")
                    onConnected()
                } else {
                    Log.w(TAG, "Billing setup failed with code: ${billingResult.responseCode}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected")
            }
        })
    }

    suspend fun checkSubscriptionStatus(userId: String) = withContext(Dispatchers.IO) {
        if (!billingClient.isReady) {
            startConnection {
                CoroutineScope(Dispatchers.IO).launch {
                    queryActiveSubscriptions(userId)
                }
            }
        } else {
            queryActiveSubscriptions(userId)
        }
    }

    private suspend fun queryActiveSubscriptions(userId: String) = withContext(Dispatchers.IO) {
        try {
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()

            billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val hasActiveSub = purchases.any { purchase ->
                        purchase.purchaseState == Purchase.PurchaseState.PURCHASED && purchase.isAcknowledged
                    }

                    _isProUser.value = hasActiveSub

                    if (userId.isNotBlank()) {
                        firestore.collection("users")
                            .document(userId)
                            .set(mapOf("isPro" to hasActiveSub), SetOptions.merge())
                    }
                } else {
                    Log.e(TAG, "Failed to query purchases: ${billingResult.debugMessage}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking subscription status", e)
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.i(TAG, "User canceled purchase flow")
        } else {
            Log.e(TAG, "Purchase update error: ${billingResult.responseCode}")
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        _isProUser.value = true
                    }
                }
            } else {
                _isProUser.value = true
            }
        }
    }

    suspend fun queryProductDetails(productId: String, onResult: (ProductDetails?) -> Unit) = withContext(Dispatchers.IO) {
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(productId)
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(product))
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                onResult(productDetailsList.first())
            } else {
                Log.e(TAG, "Error fetching product details: ${billingResult.debugMessage}")
                onResult(null)
            }
        }
    }

    fun launchBillingFlow(activity: Activity, productDetails: ProductDetails): BillingResult {
        val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: ""
        
        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .setOfferToken(offerToken)
            .build()

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        return billingClient.launchBillingFlow(activity, billingFlowParams)
    }
}
