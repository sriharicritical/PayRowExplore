package com.payment.payrowapp.contactpayrow

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import com.payment.payrowapp.R
import com.payment.payrowapp.introduction.EnterTIDActivity
import com.payment.payrowapp.newpayment.CardRepo
import com.payment.payrowapp.refundandreversal.RefundActivity
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.BaseActivity
import com.payment.payrowapp.utils.ContextUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import com.payment.payrowapp.databinding.ActivityAboutPayrowBinding
import com.payment.payrowapp.databinding.ActivityContactPayrowBinding
import com.payment.payrowapp.dataclass.ItemsCountRequest
import com.payment.payrowapp.newpayment.StoreItemsRepository
import com.payment.payrowapp.observeOnce
import com.payment.payrowapp.otp.EnterOTPActivity
import org.json.JSONObject

class ContactPayRowActivity : BaseActivity() {
    var ring: MediaPlayer? = null
    private lateinit var sharedPreferenceUtil: SharedPreferenceUtil
    private var toggleSelection = ""
    private lateinit var activityContactPayrowBinding: ActivityContactPayrowBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityContactPayrowBinding = ActivityContactPayrowBinding.inflate(layoutInflater)
        setContentView(activityContactPayrowBinding.root)
        //setContentView(R.layout.activity_contact_payrow)

        val myToolbar = activityContactPayrowBinding.root.findViewById<Toolbar>(R.id.myToolbar)
        setSupportActionBar(myToolbar)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        supportActionBar?.title = "Home"

        ring = MediaPlayer.create(this, R.raw.sound_button_click)

        sharedPreferenceUtil = SharedPreferenceUtil(applicationContext)

        activityContactPayrowBinding.btnContactUs.setOnClickListener {
            ring?.start()
            startActivity(Intent(this, ContactUsActivity::class.java))
        }

        activityContactPayrowBinding.btnAboutPayrow.setOnClickListener {
            ring?.start()
            startActivity(Intent(this, ContactActivity::class.java))
        }

        activityContactPayrowBinding.btnSupport.setOnClickListener {
            ring?.start()
            startActivity(Intent(this, SupportActivity::class.java))
        }
        activityContactPayrowBinding.btnVoid.setOnClickListener {
            ring?.start()
            startActivity(
                Intent(this, RefundActivity::class.java).putExtra(
                    "mode", "Void"
                )
            )
        }
        activityContactPayrowBinding.btnReFund.setOnClickListener {
            ring?.start()
            startActivity(
                Intent(this, RefundActivity::class.java).putExtra(
                    "mode", "Refund"
                )
            )
        }

        activityContactPayrowBinding.btnChangeTID.setOnClickListener {
            ring?.start()
            val intent = Intent(baseContext, EnterTIDActivity::class.java)
            startActivity(intent)
            finish()
        }


        activityContactPayrowBinding.btnServiceActivation.setOnClickListener {
            ring?.start()
            val intent = Intent(baseContext, ServiceActivationActivity::class.java)
            startActivity(intent)
            // finish()
        }
        // set vat calculator
        if (sharedPreferenceUtil.getVATCalculator()) {
            activityContactPayrowBinding.ivMultiON.setImageResource(R.drawable.ic_multi_user_enable)
        } else {
            activityContactPayrowBinding.ivMultiON.setImageResource(R.drawable.ic_multi_enable_off)
        }

        activityContactPayrowBinding.ivMultiON.setOnClickListener {
            toggleSelection = "vat calculator"
            val intent = Intent(this, EnterOTPActivity::class.java)
            intent.putExtra("channel", "toggle")
            resultLauncher.launch(intent)
            /*if (sharedPreferenceUtil.getVATCalculator()) {
                sharedPreferenceUtil.setVATCalculator(false)
                ivMultiON.setImageResource(R.drawable.ic_multi_enable_off)
            } else {
                sharedPreferenceUtil.setVATCalculator(true)
                ivMultiON.setImageResource(R.drawable.ic_multi_user_enable)
            }*/
        }

        // set amount from service catalog
        if (sharedPreferenceUtil.getCataLogAmount()) {
            activityContactPayrowBinding.ivCatalogON.setImageResource(R.drawable.ic_multi_user_enable)
        } else {
            activityContactPayrowBinding.ivCatalogON.setImageResource(R.drawable.ic_multi_enable_off)
        }

        activityContactPayrowBinding.ivCatalogON.setOnClickListener {

            toggleSelection = "service catalog"
            if (sharedPreferenceUtil.getCataLogAmount()) {
                val intent = Intent(this, EnterOTPActivity::class.java)
                intent.putExtra("channel", "toggle")
                resultLauncher.launch(intent)
            } else {
                val itemsCountRequest = ItemsCountRequest(
                    sharedPreferenceUtil.getGatewayMerchantID(),
                    sharedPreferenceUtil.getMerchantID()
                )
                StoreItemsRepository.getItemCountMutableLiveData(
                    this,
                    itemsCountRequest,
                    sharedPreferenceUtil
                )
                StoreItemsRepository.getItemCountLiveData()
                    .observeOnce(this@ContactPayRowActivity) {
                        if (it?.data != null) {
                            val dataObject = JSONObject(it.data)
                            val gateWayItemCount = dataObject.getInt("gatewayServCount")
                            val posItemCount = dataObject.getInt("posServCount")
                            if (gateWayItemCount > 1 || posItemCount > 1) {
                                val intent = Intent(this, EnterOTPActivity::class.java)
                                intent.putExtra("channel", "toggle")
                                resultLauncher.launch(intent)
                            } else {
                                Toast.makeText(
                                    this,
                                    getString(R.string.you_are_unable_to_enable_this_triger),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
            }
        }

        // load terminal data
        if (ContextUtils.isNetworkConnected(this)) {
            CardRepo.getTerminalData(this)
        } else {
            Toast.makeText(
                this,
                getString(R.string.internetnotavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (toggleSelection == "service catalog") {
                if (sharedPreferenceUtil.getCataLogAmount()) {
                    sharedPreferenceUtil.setCataLogAmount(false)
                    activityContactPayrowBinding.ivCatalogON.setImageResource(R.drawable.ic_multi_enable_off)
                } else {
                    sharedPreferenceUtil.setCataLogAmount(true)
                    activityContactPayrowBinding.ivCatalogON.setImageResource(R.drawable.ic_multi_user_enable)
                }
            } else if (toggleSelection == "vat calculator") {
                if (sharedPreferenceUtil.getVATCalculator()) {
                    sharedPreferenceUtil.setVATCalculator(false)
                    activityContactPayrowBinding.ivMultiON.setImageResource(R.drawable.ic_multi_enable_off)
                } else {
                    sharedPreferenceUtil.setVATCalculator(true)
                    activityContactPayrowBinding.ivMultiON.setImageResource(R.drawable.ic_multi_user_enable)
                }
            }
        }
    }
}