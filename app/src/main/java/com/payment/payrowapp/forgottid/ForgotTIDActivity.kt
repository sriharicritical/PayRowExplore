package com.payment.payrowapp.forgottid

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.payment.payrowapp.R
import com.payment.payrowapp.databinding.ActivityEnterAmountToPayCashBinding
import com.payment.payrowapp.databinding.ActivityForgotTidactivityBinding
import com.payment.payrowapp.observeOnce
import com.payment.payrowapp.sharepref.SharedPreferenceUtil

class ForgotTIDActivity : AppCompatActivity() {

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private lateinit var binding:ActivityForgotTidactivityBinding

    var ring: MediaPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_forgot_tidactivity)

        binding = ActivityForgotTidactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val myToolbar = findViewById<Toolbar>(R.id.myToolbar)
        setSupportActionBar(myToolbar)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        getSupportActionBar()?.setDisplayShowHomeEnabled(true)

        getSupportActionBar()?.title = "SignIn Page"

        val bundle = intent.extras
        binding.tvForgotTID.text = bundle?.getString("heading")

        ring = MediaPlayer.create(this, R.raw.sound_button_click)

        val requestTIDVIewModel =
            ViewModelProvider(
                this,
                RequestTIDViewModelFactory(this)
            ).get(RequestTIDVIewModel::class.java)

        val sharedPreferenceUtil = SharedPreferenceUtil(this)

        binding.btnSubmit.setOnClickListener {
           ring?.start()
            val mobileNum = "971${binding.etEnterAmount.text}"
            if (validation()) {

                requestTIDVIewModel.requestTIDResponse(
                    binding.etEmail.text.toString().trim(),
                    mobileNum.trim(),sharedPreferenceUtil
                )
                requestTIDVIewModel.getData().observeOnce(this@ForgotTIDActivity) {
                    startActivity(Intent(baseContext, ForgotTIDSuccessfullActivity::class.java))
                    finish()
                }
            }
        }

        binding.buttonCancel.setOnClickListener {
            ring?.start()
            finish()
        }
    }

    private fun validation(): Boolean {
        var status = true
        if (binding.etEmail.text.toString().isNullOrEmpty()) {
            showToast("Please enter Email")
            status = false
        } else if (binding.etEnterAmount.text.toString().isNullOrEmpty()) {
            showToast("Please enter Your Mobile Number")
            status = false
        }

        return status
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}