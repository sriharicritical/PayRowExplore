package com.payment.payrowapp.paymenthistory

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import com.payment.payrowapp.R
import com.payment.payrowapp.databinding.ActivityDailyReportBinding
import com.payment.payrowapp.databinding.ActivityLoginNewBinding
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.BaseActivity

class DailyReportActivity : BaseActivity() {
    var preference: SharedPreferences? = null

    private lateinit var binding: ActivityDailyReportBinding
    var ring: MediaPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_daily_report)
        binding = ActivityDailyReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

      //  setSupportActionBar(myToolbar)
        ring = MediaPlayer.create(this, R.raw.sound_button_click)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        preference =
            getSharedPreferences(resources.getString(R.string.app_name), Context.MODE_PRIVATE)

        val sharedPreferenceUtil = SharedPreferenceUtil(this)
        val merchantID = sharedPreferenceUtil.getMerchantID()//preference!!.getString(Constants.MERCHANT_ID, "")
        binding.btnInvoiceNumber.text = "MID: $merchantID"
        binding.tvNameOfTheBusiness.text = sharedPreferenceUtil.getMerchantName() + sharedPreferenceUtil.getMerchantLastName()//sharedPreferenceUtil.getBusinessType()

        val bundle = intent.extras

        if (bundle?.getString("FROM").equals("DAILY")) {
            supportActionBar?.title = "Summary Report"
        } else {
            binding.btnConsolidated.visibility = View.GONE
            supportActionBar?.title = "Monthly Report"
        }
        val myBundle = Bundle()


        binding.btnConsolidated.setOnClickListener {
            ring?.start()
            myBundle.putString("FROM", "Consolidated Report")
            startActivity(
                Intent(
                    this,
                    SummaryDailySelectionActivity
                    ::class.java
                ).putExtras(myBundle)
            )
        }

        binding.btnTapToPayReport.setOnClickListener {
            ring?.start()
            myBundle.putString("FROM", "Tap To Pay")
            if (bundle?.getString("FROM").equals("DAILY")) {
                startActivity(
                    Intent(
                        this,
                        SummaryDailySelectionActivity
                        ::class.java
                    ).putExtras(myBundle)
                )
            } else {
                startActivity(
                    Intent(this, MonthlyReportDetailActivity::class.java).putExtras(
                        myBundle
                    )
                )
            }
        }
        binding.btnCashInvoiceReport.setOnClickListener {
            ring?.start()
            myBundle.putString("FROM", "Cash Invoice")
            if (bundle?.getString("FROM").equals("DAILY")) {
                startActivity(
                    Intent(
                        this,
                        SummaryDailySelectionActivity::class.java
                    ).putExtras(myBundle)
                )
            } else {
                startActivity(
                    Intent(this, MonthlyReportDetailActivity::class.java).putExtras(
                        myBundle
                    )
                )
            }
        }
        binding.btnPayByLinkReport.setOnClickListener {
            ring?.start()
            myBundle.putString("FROM", "Pay By Link")
            if (bundle?.getString("FROM").equals("DAILY")) {
                startActivity(
                    Intent(
                        this,
                        SummaryDailySelectionActivity::class.java
                    ).putExtras(myBundle)
                )
            } else {
                startActivity(
                    Intent(this, MonthlyReportDetailActivity::class.java).putExtras(
                        myBundle
                    )
                )
            }
        }
        binding.btnPayBySms.setOnClickListener {
            ring?.start()
            myBundle.putString("FROM", "Pay By QR Code")
            if (bundle?.getString("FROM").equals("DAILY")) {
                startActivity(
                    Intent(
                        this,
                        SummaryDailySelectionActivity::class.java
                    ).putExtras(myBundle)
                )
            } else {
                startActivity(
                    Intent(this, MonthlyReportDetailActivity::class.java).putExtras(
                        myBundle
                    )
                )
            }
        }
    }
}