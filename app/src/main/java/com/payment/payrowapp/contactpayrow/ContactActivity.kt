package com.payment.payrowapp.contactpayrow

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.widget.Toolbar
import com.payment.payrowapp.R
import com.payment.payrowapp.databinding.ActivityContactBinding
import com.payment.payrowapp.databinding.ActivityContactPayrowBinding
import com.payment.payrowapp.introduction.EnterTIDActivity
import com.payment.payrowapp.utils.BaseActivity

class ContactActivity :  BaseActivity() {
    var ring: MediaPlayer? = null
    private lateinit var actvityContactBinding: ActivityContactBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_contact)
        actvityContactBinding = ActivityContactBinding.inflate(layoutInflater)
        setContentView(actvityContactBinding.root)

        val myToolbar = actvityContactBinding.root.findViewById<Toolbar>(R.id.myToolbar)
        setSupportActionBar(myToolbar)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        supportActionBar?.title = "Contact PayRow"

        ring = MediaPlayer.create(this, R.raw.sound_button_click)

        actvityContactBinding.btnContactUs.setOnClickListener {
            //            ContactUsDialog(this).show()
            ring?.start()
            startActivity(Intent(this, ContactUsActivity::class.java))
        }

        actvityContactBinding.btnAboutPayrow.setOnClickListener {
            ring?.start()
            startActivity(Intent(this, AboutPayRowActivity::class.java))
        }

        actvityContactBinding.btnSupport.setOnClickListener {
            ring?.start()
            startActivity(Intent(this, SupportActivity::class.java))
        }

        actvityContactBinding.btnChangeTID.setOnClickListener {
            ring?.start()
            val intent = Intent(baseContext, EnterTIDActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}