package com.payment.payrowapp.newpayment

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.payment.payrowapp.R
import com.payment.payrowapp.dashboard.DashboardActivity
import com.payment.payrowapp.databinding.ActivityCustomerCopySharedBinding


class CustomerCopySharedActivity : AppCompatActivity() {
    private var orderNumber: String? = null
    private var amount: String? = null

    private lateinit var binding: ActivityCustomerCopySharedBinding
    var ring: MediaPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_customer_copy_shared)
        binding = ActivityCustomerCopySharedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ring = MediaPlayer.create(this, R.raw.sound_button_click)

        val intent = intent.extras
        if (intent?.getString("Payment Type") != null) {
            orderNumber = intent.getString("orderNumber")
            amount = intent.getString("amount")
            binding.tvPaymentSuccessful.text = getString(R.string.link_sent_sucessfully)
            binding.tvYouHaveMadePayment.visibility = View.GONE

        }

        binding.btnHome.setOnClickListener {
        ring?.start()
            startActivity(
                Intent(
                    this,
                    DashboardActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
        }
    }
}