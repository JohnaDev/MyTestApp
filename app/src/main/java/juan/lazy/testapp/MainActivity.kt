package juan.lazy.testapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.allyants.notifyme.NotifyMe
import com.android.billingclient.api.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    lateinit private var billingClient: BillingClient
    private val skuList = listOf("test1")
    private lateinit var layout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        layout = findViewById(R.id.ll_billing)

        startService(Intent(this, TestService::class.java))

        btnStartBilling.setOnClickListener {
            runBlocking {
                Snackbar.make(
                    btnShowNotification,
                    "Runblocking",
                    Snackbar.LENGTH_LONG
                )
                    .show()
            }

            startBilling()
        }

        btnShowNotification.setOnClickListener {
            val mNotificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val id = "juan.lazy.testapp"

            val name: CharSequence = "Test App"
            val description = "This is test app channel"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val importance: Int = NotificationManager.IMPORTANCE_HIGH
                val mChannel = NotificationChannel(id, name, importance)
                mChannel.description = description

                mNotificationManager.createNotificationChannel(mChannel)
            }

            val builder = NotificationCompat.Builder(this, id)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Title")
                .setContentText("content")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_CALL)

            val incomingCallNotification = builder.build()

            with(NotificationManagerCompat.from(applicationContext)) {
                notify(NotificationID.iD, incomingCallNotification)
            }

//            val intent = Intent(applicationContext, MainActivity::class.java)
//            intent.putExtra("test", "I am a String")
//            val notifyMe: NotifyMe = NotifyMe.Builder(applicationContext)
//                .title("Title")
//                .content("Content")
//                .color(255, 0, 0, 255)
//                .led_color(255, 255, 255, 255)
//                .time(Date())
//                .addAction(intent, "Snooze", false)
//                .key("test")
//                .addAction(Intent(), "Dismiss", true, false)
//                .addAction(intent, "Done")
//                .large_icon(R.mipmap.ic_launcher)
//                .build()
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
        skuList.add("android.test.purchased")
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
