package com.payment.payrowapp.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Window
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.payment.payrowapp.R
import com.payment.payrowapp.adapters.BarItemAdapter
import com.payment.payrowapp.dataclass.SharedData

class BarCodeItemDialog(
    context: Context
) :
    Dialog(context) {
    var ring: MediaPlayer? = null


    init {
        setCancelable(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.layout_barcode_item)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val btnSubmit: Button = findViewById(R.id.btnSubmit)
        val btnNext: Button = findViewById(R.id.buttonNext)
        val recBarItems: RecyclerView = findViewById(R.id.recBarItems)

        ring = MediaPlayer.create(context, R.raw.sound_button_click)

        val productAdapter = BarItemAdapter(context, SharedData.barCodeItems)

        recBarItems.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        recBarItems.adapter = productAdapter

        btnSubmit.setOnClickListener() {
            ring?.start()
            dismiss()
        }

        btnNext.setOnClickListener() {
            ring?.start()

           // dismiss()
        }
    }
}