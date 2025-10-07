package com.payment.payrowapp.invoicerecall

import android.content.Intent
import android.graphics.Point
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.Display
import android.view.WindowManager
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import com.google.zxing.WriterException
import com.payment.payrowapp.R
import com.payment.payrowapp.dashboard.DashboardActivity
import com.payment.payrowapp.databinding.ActivityCreatePinBinding
import com.payment.payrowapp.databinding.ActivityQrcodeReceiptBinding
import com.payment.payrowapp.utils.BaseActivity



class QRCodeReceiptActivity : BaseActivity() {

    private lateinit var binding:ActivityQrcodeReceiptBinding
    var ring: MediaPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
     //   setContentView(R.layout.activity_qrcode_receipt)

        binding = ActivityQrcodeReceiptBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        setupToolbar()
        //setSupportActionBar(myToolbar)

        ring = MediaPlayer.create(this, R.raw.sound_button_click)

        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        getSupportActionBar()?.setDisplayShowHomeEnabled(true)

        getSupportActionBar()?.title = "Scan Receipt"

        val bundle = intent.extras
        val url = bundle?.getString("InvoiceURL")

        val manager = getSystemService(WINDOW_SERVICE) as WindowManager

        // initializing a variable for default display.
        val display: Display = manager.defaultDisplay
        val point = Point()
        display.getSize(point)
        val width: Int = point.x
        val height: Int = point.y
        var dimen = if (width < height) width else height
        dimen = dimen * 3 / 4
        val qrgEncoder = QRGEncoder(url, null, QRGContents.Type.TEXT, dimen)
        try {
            val bitmap = qrgEncoder.encodeAsBitmap()
            binding.payQRImage.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            Log.e("Tag", e.toString())
        }

        binding.btnHome.setOnClickListener {
            ring?.start()
            startActivity(
                Intent(this, DashboardActivity::class.java).addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                )
            )
        }
    }
}