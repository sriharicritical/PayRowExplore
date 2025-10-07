package com.payment.payrowapp.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Window
import android.widget.Button
import com.payment.payrowapp.R


class QRCodeSessionDialog(
    context: Context,
    private val qrCodeSessionListener: QRCodeSessionListener
) :
    Dialog(context) {
    var ring: MediaPlayer? = null


    init {
        setCancelable(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.layout_qrcode_session)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val btnSubmit: Button = findViewById(R.id.btnSubmit)
        val btnCancel: Button = findViewById(R.id.buttonCancel)

        ring = MediaPlayer.create(context, R.raw.sound_button_click)

        btnSubmit.setOnClickListener() {
            ring?.start()
            qrCodeSessionListener.closeSession()
            dismiss()
        }

        btnCancel.setOnClickListener() {
            ring?.start()
            dismiss()
        }

    }
}