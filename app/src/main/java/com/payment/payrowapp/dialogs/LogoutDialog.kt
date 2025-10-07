package com.payment.payrowapp.dialogs

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.TextView
import com.payment.payrowapp.R
import com.payment.payrowapp.dataclass.DeviceLogoutRequest
import com.payment.payrowapp.login.AuthenticationActivity

class LogoutDialog(context: Context, private val deviceLogoutRequest: DeviceLogoutRequest) :
    Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
    var ring: MediaPlayer? = null

    init {
        setCancelable(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.layout_logout_dialog)
       // window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val buttonCancel: Button = findViewById(R.id.buttonCancel)
        val btnSubmit: Button = findViewById(R.id.btnSubmit)
        val tvAlertMessage: TextView = findViewById(R.id.tvAlertMessage)

        // tvAlertMessage.setText("Are you sure you want to logout?")
        ring = MediaPlayer.create(context, R.raw.sound_button_click)

        btnSubmit.setOnClickListener() {
            ring?.start()
            val intent = Intent(context, AuthenticationActivity::class.java).putExtra(
                "TYPE",
                "Logout"
            ).putExtra("TID",deviceLogoutRequest.tid)
            context.startActivity(intent)
          //  EnterTIDRepository.getLogoutMutableLiveData(context, deviceLogoutRequest)
            dismiss()
        }
        buttonCancel.setOnClickListener() {
            ring?.start()
            dismiss()
        }

     /*   btnYes.setOnClickListener() {
            dismiss()
            ring?.start()
            Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
            context.startActivity(
                Intent(
                    context,
                    LoginActivity::class.java
                ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
        }*/


    }
}