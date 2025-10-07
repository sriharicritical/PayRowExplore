package com.payment.payrowapp.introduction

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProvider
import com.payment.payrowapp.R
import com.payment.payrowapp.contactpayrow.ContactUsActivity
import com.payment.payrowapp.crypto.HeaderSignatureUtil
import com.payment.payrowapp.databinding.ActivityEnterTidactivityBinding
import com.payment.payrowapp.databinding.ActivityForgotTidsuccessfullBinding
import com.payment.payrowapp.dataclass.VerifyDeviceRequest
import com.payment.payrowapp.forgottid.ForgotTIDActivity
import com.payment.payrowapp.login.AuthenticationActivity
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.BaseActivity


class EnterTIDActivity : BaseActivity() {
    private val userType =
        arrayOf("Business Owner", "Admin", "Branch Manager", "Terminal Activation")

    lateinit var sharedPreferenceUtil: SharedPreferenceUtil
    private lateinit var binding:ActivityEnterTidactivityBinding
    var ring: MediaPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_enter_tidactivity)
        binding = ActivityEnterTidactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ring = MediaPlayer.create(this, R.raw.sound_button_click)

        val preference =
            getSharedPreferences(resources.getString(R.string.app_name), Context.MODE_PRIVATE)
        sharedPreferenceUtil = SharedPreferenceUtil(this)
       // setSupportActionBar(myToolbar)
        setupToolbar()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        getSupportActionBar()?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_west_24)
        if (sharedPreferenceUtil.getISLogin()) { //preference.getBoolean(Constants.IS_LOGIN, false)
            getSupportActionBar()?.title = "Change TID"
            binding.toolbar.myToolbar.setTitleTextColor(Color.WHITE)
            binding.ivHello.visibility = View.GONE
        } else {
            getSupportActionBar()?.title = "Get Started"
            binding.toolbar.myToolbar.setTitleTextColor(Color.WHITE)
        }

        val adapter: ArrayAdapter<*> =
            ArrayAdapter<Any?>(this, R.layout.custom_spinner, R.id.tvSelectType, userType)
        adapter.setDropDownViewResource(R.layout.custom_spinner)
        binding.spinner.adapter = adapter

        binding.btnRequestTID.setOnClickListener {
            ring?.start()
            val intent = Intent(baseContext, ForgotTIDActivity::class.java).putExtra(
                "heading",
                "Request TID"
            )
            startActivity(intent)
        }

        binding.btnForgotTID.setOnClickListener {
            ring?.start()
            val intent = Intent(baseContext, ContactUsActivity::class.java)
            startActivity(intent)
        }

        val enterTIDViewModelClass =
            ViewModelProvider(
                this,
                EnterTIDFactory(this)
            ).get(EnterTIDViewModelClass::class.java)

        enterTIDViewModelClass.initKeyResponse(sharedPreferenceUtil)
        enterTIDViewModelClass.getInitData().observe(this) {
        }

        binding.ivProceed.setOnClickListener {
            ring?.start()
            val mobileNum = "971${binding.etMobileNum.text}"
            if (validation()) {
                val verifyDeviceRequest = VerifyDeviceRequest("cloudCommerce",
                    binding.etEnterTID.text.toString(),
                    mobileNum.toLong(),
                    HeaderSignatureUtil.getDeviceSN())
                /*etEnterTID.setText("")
                etMobileNum.setText("")*/
                enterTIDViewModelClass.verifyDeviceResponse(sharedPreferenceUtil,verifyDeviceRequest)
                enterTIDViewModelClass.getData().observe(this) {
                    if (it != null) {
                        val intent =
                            Intent(baseContext, AuthenticationActivity::class.java).putExtra(
                                "AUTH",
                                "TID"
                            ).putExtra("TID", binding.etEnterTID.text.toString())
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }

    private fun validation(): Boolean {
        var status = true
        if (binding.etEnterTID.text.toString().isNullOrEmpty()) {
            showToast("Please enter TID Number")
            status = false
        } else if (binding.etMobileNum.text.toString().isNullOrEmpty()) {
            showToast("Please enter Your Mobile Number")
            status = false
        }
        return status
    }

    /*fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }*/
}