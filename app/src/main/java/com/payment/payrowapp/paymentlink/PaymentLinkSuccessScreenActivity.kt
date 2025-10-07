package com.payment.payrowapp.paymentlink

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.payment.payrowapp.R
import com.payment.payrowapp.dashboard.DashboardActivity
import com.payment.payrowapp.databinding.ActivityLinkSentSuccessfullyBinding
import com.payment.payrowapp.databinding.ActivitySoftwareProductsBinding
import com.payment.payrowapp.utils.BaseActivity

class PaymentLinkSuccessScreenActivity : BaseActivity() {

private lateinit var binding:ActivityLinkSentSuccessfullyBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      //  setContentView(R.layout.activity_link_sent_successfully)
        binding = ActivityLinkSentSuccessfullyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent.extras
        if (bundle != null) {
            binding.tvTransNo.visibility = View.GONE
            binding.tvAmount.visibility = View.GONE
            binding.tvYouHaveMadePayment.text = "Customer Copy has been shared"
            binding.tvPaymentSuccessful.text = "Done!"
        }

        binding.btnHome.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    DashboardActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
        }

    }
}