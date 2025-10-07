package com.payment.payrowapp.contactpayrow

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannedString
import android.view.WindowManager
import android.text.Annotation
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import com.payment.payrowapp.R
import com.payment.payrowapp.databinding.ActivityContactUsBinding
import com.payment.payrowapp.databinding.ActivityHardwareProductsBinding
import com.payment.payrowapp.utils.BaseActivity
import com.payment.payrowapp.utils.CustomTypefaceSpan

class HardwareProductsActivity : BaseActivity() {
    private lateinit var hardwareProductsBinding: ActivityHardwareProductsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      //  setContentView(R.layout.activity_hardware_products)

        hardwareProductsBinding = ActivityHardwareProductsBinding.inflate(layoutInflater)
        setContentView(hardwareProductsBinding.root)

        val myToolbar = hardwareProductsBinding.root.findViewById<Toolbar>(R.id.myToolbar)
        setSupportActionBar(myToolbar)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        getSupportActionBar()?.setDisplayShowHomeEnabled(true)

        getSupportActionBar()?.title = "About Us"


        val bundle = intent.extras
        if (bundle?.getString("Product") == "SoftPOS") {
            // ivHardwareProducts.setImageResource(R.drawable.ic_soft_pos)
            hardwareProductsBinding.tvSoftPOSMobile.text = "Soft POS"
            val titleText = getText(R.string.soft_pos_text) as SpannedString
            hardwareProductsBinding.tvSoftPOS.text = getFont(titleText)
        } else if (bundle?.getString("Product") == "CashInvoice") {
            // ivSoftPOSMobile.setImageResource(R.drawable.ic_cash_received_machine)
            hardwareProductsBinding.tvSoftPOSMobile.text = "Kiosk Machine"
            hardwareProductsBinding.tvSoftPOS.text = getText(R.string.cash_received_machine_txt)
            hardwareProductsBinding.tvSoftPOSTwo.text = getText(R.string.cash_received_machine_txt_two)
            // tvAuthPartner.text = getString(R.string.cash_received_machine)
        } else if (bundle?.getString("Product") == "POS") {
            // ivSoftPOSMobile.setImageResource(R.drawable.ic_pos_retail_machine)
            hardwareProductsBinding.tvSoftPOSMobile.text = "POS & Retail Machine"
            hardwareProductsBinding.tvPayRowNetProvides.text = "PayRow Provides"
            // tvPOSMachine.visibility = View.VISIBLE
            hardwareProductsBinding.tvSoftPOS.text = getText(R.string.pos_retail_machine_txt)
            hardwareProductsBinding.tvSoftPOSTwo.text = getText(R.string.pos_retail_machine_txt_two)
        } else if (bundle?.getString("Product") == "Payment Gateway"
        ) {
            // ivHardwareProducts.setImageResource(R.drawable.ic_soft_pos)
            //  ivSoftPOSMobile.setImageResource(R.drawable.ic_payment_gateway)
            hardwareProductsBinding.tvSoftPOSMobile.text = bundle?.getString("Product")
            val titleText1 = getText(R.string.payment_gaeway_text) as SpannedString
            hardwareProductsBinding.tvSoftPOS.text = getFont(titleText1)
            // val titleText = getText(R.string.payment_gateway_text_two) as SpannedString
            hardwareProductsBinding.tvSoftPOSTwo.text = getText(R.string.payment_gateway_text_two)
        } else if (bundle?.getString("Product") == "Wallet") {
            // ivHardwareProducts.setImageResource(R.drawable.ic_soft_pos)
            // ivSoftPOSMobile.setImageResource(R.drawable.ic_wallet)
            hardwareProductsBinding.tvSoftPOSMobile.text = "Wallet"
            hardwareProductsBinding.tvPayRowNetProvides.text = "PayRow Provides"
        } else if (bundle?.getString("Product") == "QR Code") {
            // ivHardwareProducts.setImageResource(R.drawable.ic_soft_pos)
            // ivSoftPOSMobile.setImageResource(R.drawable.ic_paybyqrcode)
            hardwareProductsBinding.tvSoftPOSMobile.text = "Pay by QR Code"
            hardwareProductsBinding.tvPayRowNetProvides.text = "PayRow Provides"
            val titleText = getText(R.string.pay_by_qr_code_text) as SpannedString
            hardwareProductsBinding.tvSoftPOS.text = getFont(titleText)
            hardwareProductsBinding.tvSoftPOSTwo.text = getText(R.string.pay_by_link_text_two)

        } else if (bundle?.getString("Product") == "Pay by Link") {
            // ivHardwareProducts.setImageResource(R.drawable.ic_soft_pos)
            // ivSoftPOSMobile.setImageResource(R.drawable.ic_paybylink)
            hardwareProductsBinding.tvSoftPOSMobile.text = bundle?.getString("Product")
            hardwareProductsBinding.tvPayRowNetProvides.text = "PayRow Provides"

            val titleText = getText(R.string.pay_by_link_text) as SpannedString
            hardwareProductsBinding.tvSoftPOS.text = getFont(titleText)
            //  tvSoftPOS.text = getText(R.string.pay_by_link_text)
            hardwareProductsBinding.tvSoftPOSTwo.text = getText(R.string.pay_by_link_text_two)

        } else if (bundle?.getString("Product") == "VAT") {
            // ivHardwareProducts.setImageResource(R.drawable.ic_soft_pos)
            hardwareProductsBinding.tvSoftPOSMobile.text = "Value-Added Tax(VAT)"
            // ivSoftPOSMobile.setImageResource(R.drawable.ic_vat)
            hardwareProductsBinding.tvPayRowNetProvides.text = "PayRow Provides"
            val titleText1 = getText(R.string.vat_text) as SpannedString
            hardwareProductsBinding.tvSoftPOS.text = getFont(titleText1)
            hardwareProductsBinding.tvSoftPOSTwo.text = getText(R.string.vat_text_two)

        } else if (bundle?.getString("Product") == "WPS") {
            // ivHardwareProducts.setImageResource(R.drawable.ic_soft_pos)
            hardwareProductsBinding.tvSoftPOSMobile.text = "Wage Protection Scheme"
            //   ivSoftPOSMobile.setImageResource(R.drawable.ic_wat)
            val titleText1 = getText(R.string.wps_text) as SpannedString
            hardwareProductsBinding.tvSoftPOS.text = getFont(titleText1)
            // val titleText = getText(R.string.wps_text_two) as SpannedString
            hardwareProductsBinding.tvSoftPOSTwo.text = getText(R.string.wps_text_two)
        }
    }

    private fun getFont(titleText: SpannedString): CharSequence? {
        // get all the annotation spans from the text
        val annotations = titleText.getSpans(0, titleText.length, Annotation::class.java)
// create a copy of the title text as a SpannableString
// so we can add and remove spans
        val spannableString = SpannableString(titleText)
// iterate through all the annotation spans
        for (annotation in annotations) {
            // look for the span with the key "font"
            if (annotation.key == "font") {
                val fontName = annotation.value
                // check the value associated with the annotation key
                if (fontName == "roboto_medium") {
                    // create the typeface
                    val typeface = ResourcesCompat.getFont(baseContext, R.font.roboto_medium)
                    // set the span to the same indices as the annotation
                    spannableString.setSpan(
                        CustomTypefaceSpan(typeface),
                        titleText.getSpanStart(annotation),
                        titleText.getSpanEnd(annotation),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        }
        return spannableString
    }
}