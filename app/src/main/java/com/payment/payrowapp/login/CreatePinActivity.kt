package com.payment.payrowapp.login

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.view.WindowManager
import com.payment.payrowapp.R
import com.payment.payrowapp.databinding.ActivityAuthenticationBinding
import com.payment.payrowapp.databinding.ActivityCreatePinBinding
import com.payment.payrowapp.utils.BaseActivity


class CreatePinActivity : BaseActivity() {

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private lateinit var binding:ActivityCreatePinBinding
    var ring: MediaPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_create_pin)
        binding = ActivityCreatePinBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()
        //  setSupportActionBar(myToolbar)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        getSupportActionBar()?.setDisplayShowHomeEnabled(true)
        getSupportActionBar()?.title = "Get Started"

        ring = MediaPlayer.create(this, R.raw.sound_button_click)

        // timer running
        val timer = object : CountDownTimer(60 * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                try {
                    val secsLeft: Int = (millisUntilFinished / 1000).toInt()
                    binding.btnResendCode.text = "00:$secsLeft"
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFinish() {
//                showToast("Timeout! Please try again")
                cancel()
                finish()
            }
        }
        timer.start()

        binding.btnSetPin.setOnClickListener {
            ring?.start()
            if (validateLogin()) {
                val intent =
                    Intent(baseContext, LoginActivity::class.java).putExtra("TYPE", "Create PIN")
                        .putExtra("PIN", binding.pinview.value.toString())
                startActivity(intent)
                finish()
            } else {
                showToast("Please enter 4 digit PIN to proceed!")
            }
        }
    }

    private fun validateLogin(): Boolean {
        if (binding.pinview.value.toString().isNotEmpty() && binding.pinview.value.length == 4) {
            return true
        }
        return false
    }
}