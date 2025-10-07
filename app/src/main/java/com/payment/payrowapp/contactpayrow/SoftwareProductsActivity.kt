package com.payment.payrowapp.contactpayrow

import android.os.Bundle
import android.text.Annotation
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannedString
import android.view.View
import android.view.WindowManager
import androidx.core.content.res.ResourcesCompat
import com.payment.payrowapp.R
import com.payment.payrowapp.databinding.ActivityRaiseComplaintsBinding
import com.payment.payrowapp.databinding.ActivitySoftwareProductsBinding
import com.payment.payrowapp.utils.BaseActivity
import com.payment.payrowapp.utils.CustomTypefaceSpan



class SoftwareProductsActivity : BaseActivity() {
    private lateinit var binding: ActivitySoftwareProductsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
     //   setContentView(R.layout.activity_software_products)
     //   setSupportActionBar(myToolbar)
        binding = ActivitySoftwareProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        getSupportActionBar()?.setDisplayShowHomeEnabled(true)

        getSupportActionBar()?.title = "About Us"

        val bundle = intent.extras
         if (bundle?.getString("Product") == "EKYC") {
            binding.tvSoftPOSMobile.text = "E - KYC"
          //  ivSoftPOSMobile.setImageResource(R.drawable.ic_e_kyc)
            val titleText = getText(R.string.ekyc_text) as SpannedString
             binding.tvBNPL.text = getFont(titleText)//getText(R.string.ekyc_text)
             binding.tvBNPLTwo.text = getText(R.string.ekyc_text_two)
        } else if (bundle?.getString("Product") == "BNPL") {
             binding.tvSoftPOSMobile.text = "Buy Now & Pay Later(BNPL)"
           // ivSoftPOSMobile.setImageResource(R.drawable.ic_bnpl)
            val titleText = getText(R.string.bnpl_text) as SpannedString
             binding.tvBNPL.text = getFont(titleText)
            //tvBNPL.text = getText(R.string.bnpl_text)
             binding.tvBNPLTwo.text = getText(R.string.bnpl_text_two)
            /* val titleText = getText(R.string.text_bnpl) as SpannedString
             tvDigitalEKYC.text = getFont(titleText)*/
        } else if (bundle?.getString("Product") == "Prepaid") {
             binding.tvSoftPOSMobile.text = "PayRow Prepaid Cards"
             binding.ivSoftPOSMobile.visibility = View.GONE
             binding.ivPrepaidCard.visibility = View.VISIBLE
           // ivPrepaidCard.setImageResource(R.drawable.ic_prepaid_card)
          //  tvBNPL.text = getText(R.string.prepaid_card_text)
            val titleText = getText(R.string.prepaid_card_text) as SpannedString
             binding.tvBNPL.text = getFont(titleText)
             binding.tvBNPLTwo.text = getText(R.string.prepaid_card_text_two)
            /* val titleText = getText(R.string.text_prepaidcard) as SpannedString
             tvDigitalEKYC.text = getFont(titleText)*/
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