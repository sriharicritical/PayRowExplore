package com.payment.payrowapp.invoicerecall

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.payment.payrowapp.R
import com.payment.payrowapp.dashboard.DashboardActivity
import com.payment.payrowapp.databinding.ActivityInvoiceReceiptBinding
import com.payment.payrowapp.databinding.ActivitySoftwareProductsBinding
import com.payment.payrowapp.dataclass.SendURLRequest
import com.payment.payrowapp.newpayment.CardReceiptActivityViewModel
import com.payment.payrowapp.newpayment.CardReceiptActivityViewModelFactory
import com.payment.payrowapp.newpayment.CustomerCopySharedActivity
import com.payment.payrowapp.observeOnce
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.BaseActivity
import com.payment.payrowapp.utils.Constants
import java.net.URLEncoder


class InvoiceReceiptActivity : BaseActivity() {
    var ring: MediaPlayer? = null
    var optionSelected = "Email"
    lateinit var invoiceNumber: String
    private var payRowDigitalCharge: Float? = null
    private var payRowCharge: Float? = null

    private lateinit var binding: ActivityInvoiceReceiptBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      //  setContentView(R.layout.activity_invoice_receipt)
        binding = ActivityInvoiceReceiptBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        //setSupportActionBar(myToolbar)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        supportActionBar?.title = "Invoice Recall"

        ring = MediaPlayer.create(this, R.raw.sound_button_click)

        try {
            val sharedPreferenceUtil = SharedPreferenceUtil(this)

            val bundle = intent.extras
            if (bundle != null) {
                invoiceNumber = bundle.getString(Constants.INVOICE_NUMBER)!!
                binding.tvInvoiceNum.text = bundle.getString(Constants.INVOICE_NUMBER)
                binding.tvAmount.text = "AED " + bundle.getString(Constants.TOTAL_AMOUNT)
                if (bundle.getString(Constants.ADDRESS) != null) {
                    binding.tvLocationDetails.text = bundle.getString(Constants.ADDRESS)
                } else {
                    binding.tvLocationDetails.text = sharedPreferenceUtil.getCountry()
                }

                /* if (bundle.getString(Constants.NAME_OF_THE_BUSINESS) != null) {
                     tvNameOfTheBusiness.text = bundle.getString(Constants.NAME_OF_THE_BUSINESS)
                 } else {*/
                binding.tvNameOfTheBusiness.text =
                    sharedPreferenceUtil.getMerchantName() + sharedPreferenceUtil.getMerchantLastName()//sharedPreferenceUtil.getBusinessType()
                //}

                binding.tvVATCharges.text = bundle.getString(Constants.TOTAL_TAX) + " AED"
                binding.tvMerchantNo.text = bundle.getString(Constants.MERCHANT_ID)
                binding.tvDate.text = bundle.getString(Constants.DATE)
                binding.tvHostRefNo.text = bundle.getString("HostRef")
                binding.tvVASRefID.text = bundle.getString("VASRef")
                //   tvCurrAmount.text = bundle.getString(Constants.TOTAL_AMOUNT)
                val amount = bundle.getString(Constants.TOTAL_AMOUNT)

                val status = bundle?.getString("status")
                if (status!!.equals("NOT CAPTURED", ignoreCase = true)) {
                    binding.tvPurchase.text = "Payment Declined"
                } else if (status!!.equals("Cancelled", ignoreCase = true)) {
                    binding.tvPurchase.text = "Card Declined"
                }

                if (bundle.getString(Constants.TRANSACTION_TYPE).equals("Card")) {
                    binding.tvVisaNo.text =
                        "**** **** **** " + bundle.getString(Constants.VISA_NUMBER)!!
                            .substring(12, 16)
                    if (amount!!.isNotEmpty()) {
                        binding.tvCurrAmount.text = "$amount"
                        val amountVal = amount.toFloat()//amount.substringBefore(".").toInt()
                        var payRowCharge = (amountVal / 100.0f) * 0.5 // 0.5% of the total amount

                        if (payRowCharge < 0.25) {
                            payRowCharge = 0.25
                        } else if (payRowCharge > 1) {
                            payRowCharge = 1.0
                        }
                        /* if (amountVal < 10) {
                             payRowCharge = 0.26f
                         } else if (amountVal <= 100 || amountVal == 10) {
                             payRowCharge = 0.525f
                         } else if (amountVal > 100) {
                             payRowCharge = 1.05f
                         }
                         val bankCharge = (amountVal / 100.0f) * 0.8 // 0.8% of the total amount
                         val bankChargeOnVAT = (bankCharge / 100.0f) * 5 // 5% on the bank charge
                         val bankChargeWihVAT = bankChargeOnVAT + bankCharge
                         payRowDigitalCharge = payRowCharge!! + bankChargeWihVAT.toFloat()*/

                        val splitString = payRowCharge.toString().split(".").toTypedArray()

                        if (splitString[1].length > 2) {
                            binding.tvPayRowCharges.text =
                                splitString[0] + "." + splitString[1].substring(
                                    0,
                                    2
                                )// payRowDigitalCharge.toString()
                        } else {
                            binding.tvPayRowCharges.text =
                                splitString[0] + "." + splitString[1]// payRowDigitalCharge.toString()
                        }


                        val amountVa = amountVal + payRowCharge!!

                        //  val vatAmount = (amountVa / 100.0f) * 5
                        // tvVATCharges.text = vatAmount.toString()

                        // val totalAmount = vatAmount + amountVa
                        // tvTOTAmount.text = "AED $totalAmount"

                        /*val splitVATString = vatAmount.toString().split(".").toTypedArray()
                        if (splitVATString[1].length > 2) {
                            tvVATCharges.text =
                                splitVATString[0] + "." + splitVATString[1].substring(0, 2)
                        } else {
                            tvVATCharges.text =
                                splitVATString[0] + "." + splitVATString[1]
                        }*/

                        val splitTOTAmount = amountVa.toString().split(".").toTypedArray()
                        if (splitTOTAmount[1].length > 2) {
                            binding.tvAmount.text =
                                "AED " + splitTOTAmount[0] + "." + splitTOTAmount[1].substring(0, 2)
                        } else {
                            binding.tvAmount.text = "AED " + splitTOTAmount[0] + "." + splitTOTAmount[1]
                        }
                    }
                } else {
                    binding.tvVisaLabel.visibility = View.GONE
                    binding.tvVisaNo.visibility = View.GONE
                    binding.tvSourceLabel.visibility = View.GONE
                    binding.tvExpiryLabel.visibility = View.GONE
                    binding.tvExpiry.visibility = View.GONE
                    binding.tvSource.visibility = View.GONE
                    binding.tvHostRefNo.visibility = View.GONE
                    binding.tvVASRefID.visibility = View.GONE
                    binding.tvHostRefNoLabel.visibility = View.GONE
                    binding.tvVASRefIDLabel.visibility = View.GONE
                    binding.tvCurrAmountLabel.visibility = View.GONE
                    binding.tvPayRowChargesLabel.visibility = View.GONE
                    binding.tvVATLabel.visibility = View.GONE

                    binding.tvCurrAmount.visibility = View.GONE
                    binding.tvVATCharges.visibility = View.GONE
                    binding.tvPayRowCharges.visibility = View.GONE
                }
            } else {
                binding.tvMerchantNo.text = sharedPreferenceUtil.getMerchantID()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        binding.btnHome.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    DashboardActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
        }

        binding.btnMerchantCopy.setOnClickListener {
//            startActivity(Intent(this, SendReceiptActivity::class.java))
            val bottomSheetDialog = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.share_bottom_sheet, null)
            bottomSheetDialog.setCancelable(true)
            bottomSheetDialog.setContentView(view)
            val ivProceed = view.findViewById<Button>(R.id.ivProceed)
            val tvShareOptionTitle = view.findViewById<TextView>(R.id.tvShareOptionTitle)
            val etWhatsApp = view.findViewById<EditText>(R.id.etWhatsApp)
            val ivWhatsApp = view.findViewById<ImageView>(R.id.ivWhatsApp)
            val ivEmail = view.findViewById<ImageView>(R.id.ivEmail)
            val ivSMS = view.findViewById<ImageView>(R.id.ivSMS)
            val btnHome = view.findViewById<Button>(R.id.btnHome)
            val etEmail = view.findViewById<EditText>(R.id.etEmail)
            val clWhatapp = view.findViewById<ConstraintLayout>(R.id.clWhatsapp)

            val bundle = intent.extras
            val titlesArray = resources.getStringArray(R.array.modeType)
            // Create an ArrayAdapter using a simple spinner layout and languages array
            val titlesAdapter = ArrayAdapter(
                this, android.R.layout.simple_spinner_item,
                titlesArray
            )
            // Set layout to use when the list of choices appear
            titlesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Set Adapter to Spinner

            val ivBack = view.findViewById<ImageView>(R.id.ivBackBtn)
            ivBack.setOnClickListener {
                bottomSheetDialog.dismiss()
            }

            btnHome.setOnClickListener {
                startActivity(
                    Intent(
                        this,
                        DashboardActivity::class.java
                    ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
            }

            ivProceed.setOnClickListener {
                var cardReceiptActivityViewModel =
                    ViewModelProvider(
                        this,
                        CardReceiptActivityViewModelFactory(this)
                    ).get(CardReceiptActivityViewModel::class.java)
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ),
                    0
                )
                val read_permission = ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                val write_permission = ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )

                if (read_permission == PackageManager.PERMISSION_GRANTED && write_permission == PackageManager.PERMISSION_GRANTED) {

                    if (optionSelected == "WhatsApp") {
                        if (etWhatsApp.text?.isNotEmpty() == true) {

                            sharePDF(
                                cardReceiptActivityViewModel,
                                "customer",
                                "invoice",
                                "971" + etWhatsApp.text.toString(),
                                ""
                            )

                        } else {
                            Toast.makeText(
                                this,
                                "Please enter whatsapp number to proceed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else if (optionSelected == "Email") {

                        if (etEmail.text?.isNotEmpty() == true) {
                            cardReceiptActivityViewModel.getPDFLinkDetails(invoiceNumber)
                            cardReceiptActivityViewModel.getPDFLinkLiveData()
                                .observeOnce(this@InvoiceReceiptActivity) {
                                    val sendURLRequest =
                                        SendURLRequest(
                                            "PayRow Receipt",
                                            etEmail.text.toString(),
                                            it.message, null
                                        )
                                    cardReceiptActivityViewModel.sendURLDetails(
                                        sendURLRequest
                                    )
                                    cardReceiptActivityViewModel.sendURLLiveData()
                                        .observeOnce(this@InvoiceReceiptActivity) {
                                            bottomSheetDialog.dismiss()
                                            startActivity(
                                                Intent(
                                                    this,
                                                    CustomerCopySharedActivity::class.java
                                                )
                                            )
                                        }
                                    /*ContextUtils.sendMail(
                                                                this@InvoiceReceiptActivity,
                                                                etEmail.text.toString(),
                                                                "PayRow Receipt",
                                                                it.message
                                                            )*/
                                }
                        } else {
                            Toast.makeText(
                                this,
                                "Please enter email id to proceed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else if (optionSelected == "SMS") {

                        Toast.makeText(this, "Work in progress...", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Toast.makeText(
                        this,
                        "Permission denied.Please provide permissions.",
                        Toast.LENGTH_SHORT
                    ).show()
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ),
                        0
                    )
                }
            }

            ivWhatsApp.setOnClickListener {
                val bundle1: Bundle = Bundle()
                bundle1.putString("TYPE", "")
                optionSelected = "WhatsApp"
                clWhatapp.visibility = View.VISIBLE
                etEmail.visibility = View.GONE
                ivWhatsApp.setImageDrawable(resources.getDrawable(R.drawable.ic_icon_whatsapp_active))
                ivEmail.setImageDrawable(resources.getDrawable(R.drawable.ic_icon_email_default))
                ivSMS.setImageDrawable(resources.getDrawable(R.drawable.ic_icon_email_default))
                tvShareOptionTitle.text = "WhatsApp Number"
                etWhatsApp.inputType = InputType.TYPE_CLASS_NUMBER
            }
            ivEmail.setOnClickListener {
                optionSelected = "Email"
                clWhatapp.visibility = View.GONE
                etEmail.visibility = View.VISIBLE
                ivWhatsApp.setImageDrawable(resources.getDrawable(R.drawable.ic_icon_whatsapp_default))
                ivEmail.setImageDrawable(resources.getDrawable(R.drawable.ic_icon_email_active))
                ivSMS.setImageDrawable(resources.getDrawable(R.drawable.ic_icon_email_default))
                tvShareOptionTitle.text = "Email"
                // etWhatsApp.inputType = InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS
            }
            ivSMS.setOnClickListener {
                optionSelected = "SMS"
                ivWhatsApp.setImageDrawable(resources.getDrawable(R.drawable.ic_icon_whatsapp_default))
                ivEmail.setImageDrawable(resources.getDrawable(R.drawable.ic_icon_email_default))
                ivSMS.setImageDrawable(resources.getDrawable(R.drawable.ic_icon_sms_active))
                etWhatsApp.hint = "Enter SMS Number"
                tvShareOptionTitle.text = "SMS Number"
                etWhatsApp.inputType = InputType.TYPE_CLASS_NUMBER
            }

            bottomSheetDialog.show()
        }
    }

    fun sharePDF(
        cardReceiptActivityViewModel: CardReceiptActivityViewModel,
        to: String,
        receiptType: String,
        phoneNo: String,
        url: String
    ) {
        try {
            var message = ""
            cardReceiptActivityViewModel.getPDFLinkDetails(invoiceNumber)
            cardReceiptActivityViewModel.getPDFLinkLiveData()
                .observeOnce(this@InvoiceReceiptActivity) {
                    // message=message.replace("""""\""","")

                    val packageManager = packageManager
                    val stringUrl =
                        "https://api.whatsapp.com/send?phone=" + phoneNo + "&text=" + URLEncoder.encode(
                            it.message,
                            "UTF-8"
                        )
                    val shareIntent: Intent = Intent(Intent.ACTION_VIEW)
                    shareIntent.setPackage("com.whatsapp")
                    //shareIntent.setType("text/plain")
                    shareIntent.data = Uri.parse(stringUrl)
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                    if (shareIntent.resolveActivity(packageManager) != null) {
                        Log.d("PayRow", "Activity Started")
                        startActivity(shareIntent)
                    } else {
                        Toast.makeText(
                            this,
                            "WhatsApp not installed in this device.",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
