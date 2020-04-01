package juan.lazy.testapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.SkuDetailsParams
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    lateinit private var billingClient: BillingClient
    private val skuList = listOf("test1")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnStartBilling.setOnClickListener {
            startBilling()
        }
    }

    private fun startBilling() {
        billingClient = BillingClient.newBuilder(applicationContext)
            .setListener { p0, p1 -> Log.v("Billing", "Purchase updates") }
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
        skuList.add("product1")
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
        billingClient.querySkuDetailsAsync(params.build()) { billingResult, skuDetails ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetails != null) {
                for (skuItem in skuDetails) {
                    val sku = skuItem.sku
                    val price = skuItem.price
                    Log.v("Billing", "$sku $price")
                    tvList.text = String.format("%s\n%s %s", tvList.text.toString(), sku, price)
                }
                if (skuDetails.isEmpty()) {
                    tvList.text = getString(R.string.empty_product)
                }
            }
        }
    }

}
