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
import com.payment.payrowapp.R

class PartialDialog(context: Context, private val intent: Intent) :
    Dialog(context) {
    var ring: MediaPlayer? = null

    init {
        setCancelable(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.layout_partial_dialog)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val btnSubmit: Button = findViewById(R.id.btnSubmit)
        val btnCancelOrder: Button = findViewById(R.id.btnCancelOrder)

        // tvAlertMessage.setText("Are you sure you want to logout?")
        ring = MediaPlayer.create(context, R.raw.sound_button_click)

        btnSubmit.setOnClickListener() {
            ring?.start()
            context.startActivity(intent)
            dismiss()
        }

        btnCancelOrder.setOnClickListener {
            ring?.start()
            dismiss()
        }

    }
}