package com.payment.payrowapp.newpayment

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.payment.payrowapp.R
import com.payment.payrowapp.dashboard.DashboardActivity
import com.payment.payrowapp.databinding.ActivityCardReceiptBinding
import com.payment.payrowapp.databinding.ActivityEnterAmountToPayCashBinding
import com.payment.payrowapp.dataclass.SendURLRequest
import com.payment.payrowapp.invoicerecall.QRCodeReceiptActivity
import com.payment.payrowapp.observeOnce
import com.payment.payrowapp.retrofit.ApiClient
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.BaseActivity
import com.payment.payrowapp.utils.Constants
import com.payment.payrowapp.utils.ContextUtils
import com.payment.payrowapp.utils.UtilityClass
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

/*
* This class is used to show the receipt of the card transaction
* */
class CardReceiptActivity : BaseActivity() {
    var stringFile: String? = null
    var currentDate: String? = null
    var currentTime: String? = null
    var optionSelected = "Email"
    var cardReceiptActivityViewModel: CardReceiptActivityViewModel? = null
    val type = "INVOICE"
    var randomNumber: Int? = 0
    var cardNo: String? = ""
    var invoiceNo: String? = ""
    var vasRefNO: String? = ""
    var hostRefNO: String? = ""
    private var payRowDigitalCharge: Float? = null
    private var payRowCharge: Float? = null

    private var payRowVATAmount: Float = 0.0F
    var payRowVATStatus = false

    companion object {
        const val url = ApiClient.BASE_URL_SOFT_POS + ApiClient.GENERATE_INVOICE
    }

    private lateinit var binding: ActivityCardReceiptBinding

    var ring: MediaPlayer? = null
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        // setContentView(R.layout.activity_card_receipt)
        binding = ActivityCardReceiptBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ring = MediaPlayer.create(this, R.raw.sound_button_click)

        val tvDate: TextView = findViewById(R.id.tvDate)
        val tvTime: TextView = findViewById(R.id.tvTime)
        val tvAuthCode: TextView = findViewById(R.id.tvAuthCode)

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val sdfTime = SimpleDateFormat("hh:mm:ss", Locale.getDefault())
        currentDate = sdf.format(Date())
        currentTime = sdfTime.format(Date())
        val bundle = intent.extras
        if (bundle?.getString("type") != null && bundle.getString("type")
                .equals("invoiceRecall")
        ) {
            binding.toolbar.myToolbar.visibility = View.VISIBLE
            setupToolbar()
            //setSupportActionBar(myToolbar)
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)

            supportActionBar?.title = "Payment Status"
            tvDate.text = bundle.getString(Constants.DATE)
            tvTime.text = bundle.getString("Time")
            UtilityClass.setMargins(this, binding.ivMashreq, 0, 1, 0, 0)
        } else {
            binding.toolbar.myToolbar.visibility = View.GONE
            tvDate.text = currentDate
            tvTime.text = currentTime
            UtilityClass.setMargins(this, binding.ivMashreq, 0, 35, 0, 0)
        }
        val sharedPreferenceUtil = SharedPreferenceUtil(this)

        // val mCardType = sharedPreferenceUtil.getTransactionType()

        /* if (mCardType == 1) {
             clEMVDetails.visibility = View.GONE
             // tvCVMStatus.visibility = View.GONE
             tvVATNoLabel.visibility = View.GONE
             tvVATNo.visibility = View.GONE
         }*/


        if (bundle?.getString("CardBrand") != null) {
            binding.tvVisaLabel.text = bundle.getString("CardBrand")
        }

        /* if (sharedPreferenceUtil.getAID() != "") {
             val aid = sharedPreferenceUtil.getAID()
             tvAID.text = aid
             val isVisa: Boolean = aid.startsWith("A000000003")
             val isMaster = (aid.startsWith("A000000004") || aid.startsWith("A000000005"))

             if (isVisa) {
                 tvAppName.text = "Visa"
                 tvVisaLabel.text = "Visa"
             } else if (isMaster) {
                 tvAppName.text = "MasterCard"
                 tvVisaLabel.text = "MasterCard"
             }
         }*/

        if (sharedPreferenceUtil.getTVR() != "") {
            binding.tvTVR.text = sharedPreferenceUtil.getTVR()
        }

        if (sharedPreferenceUtil.getACInfo() != "") {
            binding.tvACInfo.text = sharedPreferenceUtil.getACInfo()
        }

        if (sharedPreferenceUtil.getAC() != "") {
            binding.tvAC.text = sharedPreferenceUtil.getAC()
        }

        val amount = bundle?.getString("totalAmount")//sharedPreferenceUtil.getAmount()

        if (bundle != null) {

            if (bundle.getString("surcharges") != null) {
                binding.tvAmountLabel.visibility = View.VISIBLE
                binding.tvAmount.visibility = View.VISIBLE
                val bankCharge =bundle.getString("surcharges")
                binding.tvAmount.text = ContextUtils.formatWithCommas(bankCharge!!.toDouble())//bundle.getString("surcharges")
            }

            if (bundle.getString("payRowDigitFee") != null) {
                binding.tvPayRowChargesLabel.visibility = View.VISIBLE
                binding.tvPayRowCharges.visibility = View.VISIBLE
                val payRowDigitalFee = bundle.getString("payRowDigitFee")
                binding.tvPayRowCharges.text =  ContextUtils.formatWithCommas(payRowDigitalFee!!.toDouble())//bundle.getString("payRowDigitFee")
            }

            if (bundle.containsKey("payRowVATStatus")) {
                payRowVATAmount = bundle.getFloat("payRowVATAmount")
                payRowVATStatus = bundle.getBoolean("payRowVATStatus")
            }

            if (bundle.getString("authCode") != null) {
                binding.tvApprovalCodeLabel.visibility = View.VISIBLE
                binding.tvApprovalCodeLabel.text = "APPROVAL CODE " + bundle.getString("authCode")
            }

            if (bundle.getString("hostRefNO") != null) {
                hostRefNO = bundle.getString("hostRefNO")
                binding.tvVATAmountLabel.visibility = View.VISIBLE
                binding.tvVATAmount.visibility = View.VISIBLE
                binding.tvVATAmount.text = hostRefNO
            }

            if (bundle.getString("panSequenceNo") != null) {
                binding.tvVATNo.text = bundle.getString("panSequenceNo")
            }

            cardNo = bundle.getString("CARDNO")
            invoiceNo = bundle.getString("INVOICENO")

            //  vasRefNO = bundle.getString("vasRefNO")
            val status = bundle?.getString("status")
            if (status == "CAPTURED") {
                binding.tvPurchase.visibility = View.VISIBLE
                binding.tvPurchase.text = getString(R.string.transaction_successful)
            } else if (status == "PARTIAL APPROVED") {
                binding.tvPurchase.visibility = View.GONE
                binding.tvPayRowChargesLabel.visibility = View.GONE
                binding.tvPayRowCharges.visibility = View.GONE
                binding.tvPartialLabel.visibility = View.VISIBLE
                binding.tvTOTAmountLabel.text = "Approved Amount"
                binding.tvPartialLabel.text = "PARTIAL APPROVAL"
            } else if (status == "Terminal Declined") {
                binding.tvPurchase.visibility = View.GONE
                binding.clEMVDetails.visibility = View.GONE
                binding.tvCVMStatus.visibility = View.GONE
                binding.tvCVMLabel.visibility = View.GONE

                binding.ConLayDecline.visibility = View.VISIBLE
                binding.tvStatus.text = getString(R.string.transaction_declined)
            } else if (status == "NOT CAPTURED") {
                binding.tvPurchase.visibility = View.GONE
                binding.clEMVDetails.visibility = View.GONE
                binding.tvCVMStatus.visibility = View.GONE
                binding.tvCVMLabel.visibility = View.GONE

                binding.ConLayDecline.visibility = View.VISIBLE
                if (bundle.containsKey("responseMessage") && bundle.getInt("responseMessage") != null) {
                    binding.tvStatus.text = getString(bundle.getInt("responseMessage"))
                } else {
                    binding.tvStatus.text = "host timeout"
                }
                //   tvStatus.text = "Transaction Declined"
            } else if (status == "Cancelled" || status == "CLOSED" || status == "CREATED") {
                binding.tvPurchase.visibility = View.GONE
                binding.clEMVDetails.visibility = View.GONE
                binding.tvCVMStatus.visibility = View.GONE
                binding.tvCVMLabel.visibility = View.GONE
                binding.ConLayDecline.visibility = View.VISIBLE

                binding.tvVisaLabel.visibility = View.GONE
                binding.tvVisaNo.visibility = View.GONE
                binding.tvSourceLabel.visibility = View.GONE
                binding.tvSource.visibility = View.GONE
                binding.tvExpiryLabel.visibility = View.GONE
                binding.tvExpiry.visibility = View.GONE
                binding.tvVATNoLabel.visibility = View.GONE
                binding.tvVATNo.visibility = View.GONE

                binding.tvStatus.text = "CARD DECLINED"
            } else if (status == "REVERSAL") {
                binding.tvPurchase.visibility = View.GONE
                binding.clEMVDetails.visibility = View.GONE
                binding.tvCVMStatus.visibility = View.GONE
                binding.tvCVMLabel.visibility = View.GONE

                binding.ConLayDecline.visibility = View.VISIBLE
                binding.tvStatus.text = "Transaction Reversal"
                binding.tvDeclineLabel.text = "REVERSAL"
            } else {
                binding.tvPurchase.visibility = View.GONE
                binding.clEMVDetails.visibility = View.GONE
                binding.tvCVMStatus.visibility = View.GONE
                binding.tvCVMLabel.visibility = View.GONE
                binding.ConLayDecline.visibility = View.VISIBLE

                binding.tvVisaLabel.visibility = View.GONE
                binding.tvVisaNo.visibility = View.GONE
                binding.tvSourceLabel.visibility = View.GONE
                binding.tvSource.visibility = View.GONE
                binding.tvExpiryLabel.visibility = View.GONE
                binding.tvExpiry.visibility = View.GONE
                binding.tvVATNoLabel.visibility = View.GONE
                binding.tvVATNo.visibility = View.GONE

                binding.tvStatus.text = "CARD DECLINED"
            }

            val isPinBlock = bundle.getBoolean("isPinBlock")
            if (isPinBlock) {
                binding.tvCVMStatus.text = resources.getString(R.string.authorised_by_pin_entry)
            }

            val signatureStatus = bundle.getBoolean("SignatureStatus")

            if (signatureStatus) {
                binding.tvCVMStatus.text = resources.getString(R.string.dotted_line)
            }

            if (amount!!.toFloat() <= 500 && !isPinBlock && !signatureStatus) {
                binding.tvCVMStatus.text =
                    resources.getString(R.string.no_sign_required_for_contactless_txn)//"NO SIGN REQUIRED FOR CONTACTLESS TXN"
            }

            try {
                if (cardNo != null) {
                    binding.tvVisaNo.text = "**** **** **** " + cardNo?.substring(12, 16)
                } else {
                    binding.tvVisaLabel.visibility = View.GONE
                    binding.tvVisaNo.visibility = View.GONE
                    binding.tvSourceLabel.visibility = View.GONE
                    binding.tvSource.visibility = View.GONE
                    binding.tvExpiryLabel.visibility = View.GONE
                    binding.tvExpiry.visibility = View.GONE
                }
                binding.tvInvoiceNum.text = invoiceNo
            } catch (e: Exception) {
                e.printStackTrace()
            }

            binding.tvTerminalNo.text =
                sharedPreferenceUtil.getTerminalID()//preference!!.getString(Constants.TERMINAL_ID, "")!!
            binding.tvNameOfTheBusiness.text =
                sharedPreferenceUtil.getMerchantName() + sharedPreferenceUtil.getMerchantLastName()//sharedPreferenceUtil.getBusinessType()
            binding.tvLocationDetails.text = sharedPreferenceUtil.getCountry()

            if (!amount.isNullOrEmpty()) {
                // tvAmount.text = "$amount"
                val amountVal = amount.toFloat()

                if (payRowVATStatus && !status.equals("PARTIAL APPROVED")) {
                    binding.tvVATLabel.visibility = View.GONE
                    binding.tvVATCharges.visibility = View.GONE

                    binding.tvTOTAmountLabel.text = getString(R.string.total_inc_vat)
                    //  amountVal += payRowVATAmount
                    /*val splitString = payRowVATAmount.toString().split(".").toTypedArray()

                    if (splitString[1].length > 2) {
                        tvVATCharges.text =
                            splitString[0] + "." + splitString[1].substring(
                                0,
                                2
                            )// payRowDigitalCharge.toString()
                    } else {
                        tvVATCharges.text =
                            splitString[0] + "." + splitString[1]// payRowDigitalCharge.toString()
                    }*/
                }

                binding.tvTOTAmount.text = "AED " + ContextUtils.formatWithCommas(amountVal.toDouble())
                /*val splitTOTAmount = amountVal.toString().split(".").toTypedArray()
                if (splitTOTAmount[1].length > 2) {
                    binding.tvTOTAmount.text =
                        "AED " + splitTOTAmount[0] + "." + splitTOTAmount[1].substring(0, 2)
                } else {
                    if (splitTOTAmount[1] == "0") {
                        binding.tvTOTAmount.text = "AED " + splitTOTAmount[0] + ".00"
                    } else {
                        binding.tvTOTAmount.text =
                            "AED " + splitTOTAmount[0] + "." + splitTOTAmount[1]
                    }
                }*/
            }

        }

        //Add Order to PayRow database
        cardReceiptActivityViewModel =
            ViewModelProvider(
                this,
                CardReceiptActivityViewModelFactory(this)
            ).get(CardReceiptActivityViewModel::class.java)


        binding.tvMerchantNo.text = sharedPreferenceUtil.getMerchantID()
        randomNumber = (10000..99999).shuffled().last()


        binding.btnMerchantCopy.setOnClickListener {
            ring?.start()
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
            val ivQRCode = view.findViewById<ImageView>(R.id.ivQRCode)

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
                ring?.start()
                bottomSheetDialog.dismiss()
            }
            btnHome.setOnClickListener {
                ring?.start()
                startActivity(
                    Intent(
                        this,
                        DashboardActivity::class.java
                    ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
                finish()
            }

            ivProceed.setOnClickListener {
                ring?.start()
                /* ActivityCompat.requestPermissions(
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
                 )*/

                // if (read_permission == PackageManager.PERMISSION_GRANTED && write_permission == PackageManager.PERMISSION_GRANTED) {
                if (validateInputFields()) {
                    if (optionSelected == "WhatsApp") {
                        if (etWhatsApp.text?.isNotEmpty() == true) {

                            sharePDF(
                                cardReceiptActivityViewModel!!,
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

                            val finalURL = url + ContextUtils.getBase64String(invoiceNo!!)

                            val sendURLRequest =
                                SendURLRequest(
                                    "PayRow Receipt",
                                    etEmail.text.toString(),
                                    finalURL, null
                                )
                            cardReceiptActivityViewModel?.sendURLDetails(
                                sendURLRequest
                            )
                            cardReceiptActivityViewModel?.sendURLLiveData()
                                ?.observeOnce(this@CardReceiptActivity) {
                                    bottomSheetDialog.dismiss()
                                    startActivity(
                                        Intent(
                                            this,
                                            CustomerCopySharedActivity::class.java
                                        )
                                    )
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
                        "Please select an option to proceed",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
                /* } else {
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
                 }*/
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
                clWhatapp.visibility = View.GONE
                etEmail.visibility = View.VISIBLE
                optionSelected = "Email"
                ivWhatsApp.setImageDrawable(resources.getDrawable(R.drawable.ic_icon_whatsapp_default))
                ivEmail.setImageDrawable(resources.getDrawable(R.drawable.ic_icon_email_active))
                ivSMS.setImageDrawable(resources.getDrawable(R.drawable.ic_icon_email_default))
                // etWhatsApp.setHint("Enter Email")
                tvShareOptionTitle.text = "Email"
                etWhatsApp.inputType = InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS
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

            ivQRCode.setOnClickListener {
                ring?.start()
                val finalURL = url + ContextUtils.getBase64String(invoiceNo!!)
                startActivity(
                    Intent(
                        this,
                        QRCodeReceiptActivity::class.java
                    ).putExtra("InvoiceURL", finalURL)
                )
            }
            bottomSheetDialog.show()
        }

        binding.btnHome.setOnClickListener {
            ring?.start()
            startActivity(
                Intent(
                    this,
                    DashboardActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
            finish()
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
            cardReceiptActivityViewModel.getPDFLinkDetails(invoiceNo!!)
            cardReceiptActivityViewModel.getPDFLinkLiveData()
                .observeOnce(this@CardReceiptActivity) {
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


    private fun validateInputFields(): Boolean {
        if (optionSelected.isNotEmpty()
        ) {
            return true
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        Runtime.getRuntime().gc()
        if (cardNo != null) {
            cardNo = ""
        }

        if (this::binding.isInitialized) {
            binding.tvVisaNo.text = ""
            binding.tvVATAmount.text = ""
            binding.tvAmount.text = ""
            binding.tvTOTAmount.text = ""
        }
    }

}
