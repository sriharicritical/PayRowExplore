package com.payment.payrowapp.dialogs

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.TextView
import com.payment.payrowapp.R
import com.payment.payrowapp.dashboard.DashboardActivity

class ReversalDialog(context: Context) :
    Dialog(context) {
    var ring: MediaPlayer? = null

    init {
        setCancelable(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.layout_reverse_dialog)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val btnSubmit: Button = findViewById(R.id.btnSubmit)
        val tvAlertMessage: TextView = findViewById(R.id.tvAlertMessage)

        // tvAlertMessage.setText("Are you sure you want to logout?")
        ring = MediaPlayer.create(context, R.raw.sound_button_click)

        btnSubmit.setOnClickListener() {
            ring?.start()
            val intent = Intent(context, DashboardActivity::class.java)
            context.startActivity(intent)
            //  EnterTIDRepository.getLogoutMutableLiveData(context, deviceLogoutRequest)
            dismiss()
        }

    }
}