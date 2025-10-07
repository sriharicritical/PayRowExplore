package com.payment.payrowapp.dashboard

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.WindowManager
import com.payment.payrowapp.R
import com.payment.payrowapp.cashinvoice.EnterAmountToPayCashActivity
import com.payment.payrowapp.contactpayrow.ContactPayRowActivity
import com.payment.payrowapp.databinding.ActivityDashboardBinding
import com.payment.payrowapp.databinding.ActivityProductDetailsBinding
import com.payment.payrowapp.invoicerecall.SelectOptionForInvoiceRecallActivity
import com.payment.payrowapp.newpayment.EnterAmountToPayCardActivity
import com.payment.payrowapp.paymenthistory.PaymentHistoryActivity
import com.payment.payrowapp.paymentlink.PaymentLinkActivity
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.BaseActivity

class DashboardActivity : BaseActivity() {
    var ring: MediaPlayer? = null
    private lateinit var  sharedPreferenceUtil: SharedPreferenceUtil

    private lateinit var binding: ActivityDashboardBinding
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_dashboard)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        //setSupportActionBar(myToolbar)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Logout"

        ring = MediaPlayer.create(this, R.raw.sound_button_click)
        sharedPreferenceUtil = SharedPreferenceUtil(this)

        /*val tinyDB = TinyDB(this)
        val listItem: ArrayList<ContactlessConfiguration> = tinyDB.getListConfig(
            Constants.CONTACTLESS_CONFIGURATION,
            ContactlessConfiguration::class.java
        )

        Log.v("Contactless", listItem.toString())*/
        binding.btnNewPayment.setOnClickListener {
            ring?.start()
            val bundle = Bundle()
            bundle.putString("TYPE", "TAPTOPAY")
            startActivity(
                Intent(this, EnterAmountToPayCardActivity::class.java).putExtras(
                    bundle
                )
            )
        }

        binding.btnCashInvoice.setOnClickListener {
            ring?.start()
            val bundle1 = Bundle()
            bundle1.putString("TYPE", "CASHINVOICE")
            startActivity(
                Intent(this, EnterAmountToPayCashActivity::class.java).putExtras(
                    bundle1
                )
            )
        }
        binding.btnQRCode.setOnClickListener {
            ring?.start()
            val bundle1 = Bundle()
            bundle1.putString("TYPE", "QRCODE")
            startActivity(Intent(this, PaymentLinkActivity::class.java).putExtras(bundle1))
        }

        binding.btnPaymentLink.setOnClickListener {
            ring?.start()
            val bundle1 = Bundle()
            bundle1.putString("TYPE", "PAYBYLINK")
            startActivity(Intent(this, PaymentLinkActivity::class.java).putExtras(bundle1))
        }

        binding.btnPayBySms.setOnClickListener {
            ring?.start()
            val bundle1 = Bundle()
            bundle1.putString("TYPE", "PAYBYSMS")
            startActivity(Intent(this, PaymentLinkActivity::class.java).putExtras(bundle1))
        }


        binding.btnContactPayRow.setOnClickListener {
            ring?.start()
            startActivity(Intent(this, ContactPayRowActivity::class.java))
        }

        binding.btnPaymentHistory.setOnClickListener {
            ring?.start()
            startActivity(Intent(this, PaymentHistoryActivity::class.java))
        }

        binding.ivWatermarkTop.setOnClickListener {
            ring?.start()
            startActivity(Intent(this, ContactPayRowActivity::class.java))
        }

        binding.btnInvoiceRecall.setOnClickListener {
            ring?.start()
            startActivity(Intent(this, SelectOptionForInvoiceRecallActivity::class.java))
        }


         /*  val dashboardViewModel =
              ViewModelProvider(
                  this,
                  DashBoardViewModelFactory(this)
              ).get(DashboardViewModel::class.java)*/

        //  DashBoardRepo.getConfiglData(this)

        // IntroductionRepo.getMutableLiveData2(this)
    }
}