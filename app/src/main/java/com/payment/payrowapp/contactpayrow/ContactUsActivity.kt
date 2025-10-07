package com.payment.payrowapp.contactpayrow

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.payment.payrowapp.R
import com.payment.payrowapp.databinding.ActivityContactBinding
import com.payment.payrowapp.databinding.ActivityContactUsBinding
import com.payment.payrowapp.dataclass.ContactUsRequest
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.BaseActivity

class ContactUsActivity : BaseActivity() {

    lateinit var preference: SharedPreferences
    lateinit var sharedPreferenceUtil: SharedPreferenceUtil
    private var did: String? = null
    private lateinit var activityContactUsBinding: ActivityContactUsBinding
    var ring: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      //  setContentView(R.layout.activity_contact_us)
        activityContactUsBinding = ActivityContactUsBinding.inflate(layoutInflater)
        setContentView(activityContactUsBinding.root)

        val myToolbar = activityContactUsBinding.root.findViewById<Toolbar>(R.id.myToolbar)
        setSupportActionBar(myToolbar)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        getSupportActionBar()?.setDisplayShowHomeEnabled(true)
        //supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_west_24);

        getSupportActionBar()?.title = "Contact Us"
        //  myToolbar.setTitleTextColor(Color.WHITE)

        ring = MediaPlayer.create(this, R.raw.sound_button_click)
        preference =
            getSharedPreferences(resources.getString(R.string.app_name), Context.MODE_PRIVATE)
        sharedPreferenceUtil = SharedPreferenceUtil(this)

        val contactUsViewModel =
            ViewModelProvider(
                this,
                ContactUsViewModelFactory(this)
            ).get(ContactUsViewModel::class.java)

        activityContactUsBinding.btnSubmit.setOnClickListener {
          ring?.start()
            if (validation()) {
                if (sharedPreferenceUtil.getDistributorID().isNotEmpty()) {
                    did = sharedPreferenceUtil.getDistributorID()
                }
                val contactUsRequest = ContactUsRequest(
                    activityContactUsBinding.etName.text.toString(), activityContactUsBinding.etEmail.text.toString(),
                    activityContactUsBinding.etComplaintDesc.text.toString(), "971" + activityContactUsBinding.etMobileNum.text.toString(),
                    did
                )
                contactUsViewModel.postContactDetails(contactUsRequest)
                contactUsViewModel.getData().observe(this) {
                    // showToast("Your details submitted successfully")
                    finish()
                }
            }

        }
    }

    private fun validation(): Boolean {
        var status = true
        if (activityContactUsBinding.etName.text.toString().isNullOrEmpty()) {
            showToast("Please enter Your Name")
            status = false
        } else if (activityContactUsBinding.etEmail.text.toString().isNullOrEmpty()) {
            showToast("Please enter Your Email")
            status = false
        } else if (activityContactUsBinding.etComplaintDesc.text.toString().isNullOrEmpty()) {
            showToast("Please enter Message")
            status = false
        } else if (activityContactUsBinding.etMobileNum.text.toString().isNullOrEmpty()) {
            showToast("Please enter Mobile Number")
            status = false
        }
        return status
    }
}