package juan.lazy.testapp

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    lateinit private var billingClient: BillingClient
    private val skuList = listOf("test1")
    private lateinit var layout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        layout = findViewById(R.id.ll_billing);

        btnStartBilling.setOnClickListener {
            startBilling()
        }
    }

    private fun startBilling() {
        billingClient = BillingClient.newBuilder(applicationContext)
            .setListener { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    for (purchase in purchases) {
                        handlePurchase(purchase)
                    }
                } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                    Log.v("Billing", "Cancelled")
                } else {
                    Log.v("Billing", "Other error")
                }
            }
            .enablePendingPurchases()
            .build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    querySkuDetails()
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.v("Billing", "Billing service disconnected.")
            }
        })
    }

    fun querySkuDetails() {
        val skuList = ArrayList<String>()
        skuList.add("test1")
        skuList.add("subscription2")
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS)
        billingClient.querySkuDetailsAsync(params.build()) { billingResult, skuDetails ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetails != null) {
                for (skuItem in skuDetails) {
                    val sku = skuItem.sku
                    val price = skuItem.price

                    val btn = Button(this)
                    btn.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    btn.text = "Buy $sku $price"
                    btn.setOnClickListener {
                        val flowParams = BillingFlowParams.newBuilder()
                            .setSkuDetails(skuItem)
                            .build()
                        val responseCode =
                            billingClient.launchBillingFlow(this@MainActivity, flowParams)
                        Log.v("Billing", "Billing")
                    }
                    layout.addView(btn)
                }
                if (skuDetails.isEmpty()) {
                    tvList.text = getString(R.string.empty_product)
                }
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val consumeParams = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                val consumeResponseListener =
                    ConsumeResponseListener { billingResult, purchaseToken ->
                        Log.v("Billing", "Billing")
                    }
                billingClient.consumeAsync(consumeParams, consumeResponseListener)
                Log.v("Billing", "Billing")
            }
        }
    }

}
