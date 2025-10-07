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
import com.payment.payrowapp.adapters.ComplaintsAdapter
import com.payment.payrowapp.contactpayrow.RegisteredComplaintsActivity
import com.payment.payrowapp.contactpayrow.RegisteredComplaintsViewModel
import com.payment.payrowapp.dataclass.ComplaintStatus
import com.payment.payrowapp.observeOnce

class ComplaintsTypeDialog(
    private val complaintsAdapter: ComplaintsAdapter,
    context: Context,
    private var registeredComplaintsViewModel: RegisteredComplaintsViewModel,
    private val complaintId: String,
    private val complaintStatus: ComplaintStatus,
    private val position: Int, var complaintsActivity: RegisteredComplaintsActivity
) : Dialog(context) {
    var ring: MediaPlayer? = null

    init {
        setCancelable(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_delete_staff)
        val btnYes: Button = findViewById(R.id.btnYes)
        val btnNo: Button = findViewById(R.id.btnNo)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        ring = MediaPlayer.create(context, R.raw.sound_button_click)

        btnNo.setOnClickListener {
            ring?.start()
            dismiss()
        }

        btnYes.setOnClickListener() {
            ring?.start()
            registeredComplaintsViewModel.updateComplaintStatusResponse(
                context,
                complaintId,
                complaintStatus
            )
            registeredComplaintsViewModel.getComplaintStatusResponseData()
                .observeOnce(complaintsActivity) {
                    complaintsAdapter.removeListener(position)
                }
            dismiss()
        }
    }

}
