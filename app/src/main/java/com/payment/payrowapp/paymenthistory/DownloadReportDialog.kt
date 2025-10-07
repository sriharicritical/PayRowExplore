package com.payment.payrowapp.paymenthistory

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.payment.payrowapp.R
import com.payment.payrowapp.dataclass.SendTIDReportReq
import com.payment.payrowapp.invoicerecall.SendReceiptRepository
import com.payment.payrowapp.utils.ContextUtils

class DownloadReportDialog(
    private val sendTIDReportReq: SendTIDReportReq?,
    context: Context
) : Dialog(context) {
    var ring: MediaPlayer? = null

    init {
        setCancelable(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_download_report_layout)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val btnYes: Button = findViewById(R.id.btnYes)
        val btnNo: Button = findViewById(R.id.btnNo)
        val etEmail: EditText = findViewById(R.id.etEmail)

        ring = MediaPlayer.create(context, R.raw.sound_button_click)

        btnNo.setOnClickListener {
            ring?.start()
            dismiss()
        }

        btnYes.setOnClickListener() {
            ring?.start()
            if (etEmail.text?.isNotEmpty() == true) {
                if (ContextUtils.isNetworkConnected(context)) {
                    sendTIDReportReq!!.email = etEmail.text.toString()
                    SendReceiptRepository.sendTIDReport(context,sendTIDReportReq)
                    /*  val sendURLRequest =
                          SendURLRequest("PayRow Report", etEmail.text.toString(), reportPath, null)
                      SendReceiptRepository.sendURLTOMail(context, sendURLRequest)*/
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.internetnotavailable),
                        Toast.LENGTH_LONG
                    ).show()
                }
                dismiss()
            } else {
                Toast.makeText(
                    context,
                    "Please enter email id to proceed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}