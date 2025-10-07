package com.payment.payrowapp.contactpayrow

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.widget.Toolbar
import com.payment.payrowapp.R
import com.payment.payrowapp.databinding.ActivityAboutPayrowBinding
import com.payment.payrowapp.databinding.ActivityProductDetailsBinding
import com.payment.payrowapp.utils.BaseActivity

class AboutPayRowActivity : BaseActivity() {

    private lateinit var activityAboutPayrowBinding: ActivityAboutPayrowBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //  setContentView(R.layout.activity_about_payrow)
        activityAboutPayrowBinding = ActivityAboutPayrowBinding.inflate(layoutInflater)
        setContentView(activityAboutPayrowBinding.root)

        val myToolbar = activityAboutPayrowBinding.root.findViewById<Toolbar>(R.id.myToolbar)
        setSupportActionBar(myToolbar)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        supportActionBar?.title = "About Us"


        activityAboutPayrowBinding.btnNewPayment1.setOnClickListener {
            startActivity(
                Intent(this, HardwareProductsActivity::class.java).putExtra(
                    "Product",
                    "SoftPOS"
                )
            )
        }

        activityAboutPayrowBinding.btnCashInvoice.setOnClickListener {
            startActivity(
                Intent(this, HardwareProductsActivity::class.java).putExtra(
                    "Product",
                    "CashInvoice"
                )
            )
        }

        activityAboutPayrowBinding.btnQRCode.setOnClickListener {
            startActivity(
                Intent(this, HardwareProductsActivity::class.java).putExtra(
                    "Product",
                    "POS"
                )
            )
        }

        activityAboutPayrowBinding.btnCashInvoice1.setOnClickListener {
            startActivity(
                Intent(this, HardwareProductsActivity::class.java).putExtra(
                    "Product",
                    "Payment Gateway"
                )
            )
        }

        activityAboutPayrowBinding.btnQRCode1.setOnClickListener {
            startActivity(
                Intent(this, HardwareProductsActivity::class.java).putExtra(
                    "Product",
                    "QR Code"
                )
            )
        }

        activityAboutPayrowBinding.btnPayByLink.setOnClickListener {
            startActivity(
                Intent(this, HardwareProductsActivity::class.java).putExtra(
                    "Product",
                    "Pay by Link"
                )
            )
        }

        activityAboutPayrowBinding.btnWPS.setOnClickListener {
            startActivity(
                Intent(this, HardwareProductsActivity::class.java).putExtra(
                    "Product",
                    "WPS"
                )
            )
        }

        activityAboutPayrowBinding.btnVAT.setOnClickListener {
            startActivity(
                Intent(this, HardwareProductsActivity::class.java).putExtra(
                    "Product",
                    "VAT"
                )
            )
        }

        activityAboutPayrowBinding.btnEKYC.setOnClickListener {
            startActivity(
                Intent(this, SoftwareProductsActivity::class.java).putExtra(
                    "Product",
                    "EKYC"
                )
            )
        }

        activityAboutPayrowBinding.btnBuyNowLater.setOnClickListener {
            startActivity(
                Intent(this, SoftwareProductsActivity::class.java).putExtra(
                    "Product",
                    "BNPL"
                )
            )
        }

        activityAboutPayrowBinding.btnPrepaidCard.setOnClickListener {
            startActivity(
                Intent(this, SoftwareProductsActivity::class.java).putExtra(
                    "Product",
                    "Prepaid"
                )
            )
        }

        activityAboutPayrowBinding.btnWallet.setOnClickListener {
            startActivity(
                Intent(this, HardwareProductsActivity::class.java).putExtra(
                    "Product",
                    "Wallet"
                )
            )
        }

    }
}