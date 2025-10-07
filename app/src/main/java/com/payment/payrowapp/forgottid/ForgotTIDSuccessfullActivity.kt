package com.payment.payrowapp.forgottid

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.payment.payrowapp.databinding.ActivityForgotTidsuccessfullBinding
import com.payment.payrowapp.introduction.EnterTIDActivity

class ForgotTIDSuccessfullActivity : AppCompatActivity() {
    private lateinit var binding:ActivityForgotTidsuccessfullBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      //  setContentView(R.layout.activity_forgot_tidsuccessfull)
        binding = ActivityForgotTidsuccessfullBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnHome.setOnClickListener {
            startActivity(Intent(baseContext, EnterTIDActivity::class.java))
            finish()
        }
    }
}