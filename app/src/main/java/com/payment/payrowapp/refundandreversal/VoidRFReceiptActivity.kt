package com.payment.payrowapp.refundandreversal

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.payment.payrowapp.R
import com.payment.payrowapp.dashboard.DashboardActivity
import com.payment.payrowapp.databinding.ActivityRefundBinding
import com.payment.payrowapp.databinding.ActivityVoidRfreceiptBinding
import com.payment.payrowapp.dataclass.SendURLRequest
import com.payment.payrowapp.invoicerecall.QRCodeReceiptActivity
import com.payment.payrowapp.newpayment.CardReceiptActivity
import com.payment.payrowapp.newpayment.CardReceiptActivityViewModel
import com.payment.payrowapp.newpayment.CardReceiptActivityViewModelFactory
import com.payment.payrowapp.newpayment.CustomerCopySharedActivity
import com.payment.payrowapp.observeOnce
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.BaseActivity
import com.payment.payrowapp.utils.Constants
import com.payment.payrowapp.utils.ContextUtils
import com.payment.payrowapp.utils.UtilityClass
import java.text.SimpleDateFormat
import java.util.*

class VoidRFReceiptActivity : BaseActivity() {
    var stringFile: String? = null
    var currentDate: String? = null
    var currentTime: String? = null
    var optionSelected = "Email"
    var cardReceiptActivityViewModel: CardReceiptActivityViewModel? = null
    val type = "INVOICE"
    var cardNo: String? = ""
    var invoiceNo: String? = ""
    var vasRefNO: String? = ""
    var hostRefNO: String? = ""
    private lateinit var binding:ActivityVoidRfreceiptBinding
    var ring: MediaPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_void_rfreceipt)
        binding = ActivityVoidRfreceiptBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tvDate: TextView = findViewById(R.id.tvDate)
        val tvTime: TextView = findViewById(R.id.tvTime)

        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val sdfTime = SimpleDateFormat("hh:mm:ss")
        currentDate = sdf.format(Date())
        currentTime = sdfTime.format(Date())

        ring = MediaPlayer.create(this, R.raw.sound_button_click)

        val bundle = intent.extras
        if (bundle?.getString("type") != null && bundle?.getString("type")
                .equals("invoiceRecall")
        ) {
            binding.toolbar.myToolbar.visibility = View.VISIBLE
            setupToolbar()
           // setSupportActionBar(myToolbar)
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)

            supportActionBar?.title = "Invoice Recall"
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


        if (bundle != null) {

            if (bundle?.getString("cardType") != null) {
                binding.tvVisaLabel.text = bundle.getString("cardType")
            }
            if (bundle?.getString("authCode") != null) {
                binding.tvApprovalCodeLabel.visibility = View.VISIBLE
                binding.tvApprovalCodeLabel.text = "APPROVAL CODE " + bundle.getString("authCode")
            }

            if (bundle?.getString("hostRefNO") != null) {
                hostRefNO = bundle.getString("hostRefNO")
                binding.tvVATAmount.text = hostRefNO
            }

            if (bundle.getString("panSequenceNo") != null) {
                binding.tvVATNo.text = bundle.getString("panSequenceNo")
            }
            // tvVATNo.text = sharedPreferenceUtil.getAppPanSequenceNo()
            cardNo = bundle.getString("CARDNO")
            invoiceNo = bundle.getString("INVOICENO")

            vasRefNO = bundle.getString("vasRefNO")
            val status = bundle?.getString("status")
            if (bundle.getString("mode") != null) {
                if (bundle.getString("mode") == "Refund" || bundle.getString("mode") == "Refund Order" ) {
                    binding.tvTransTypeLabel.text = "REFUND"
                    binding.tvDeclineLabel.text = "REFUND"
                } else {
                    binding.tvTransTypeLabel.text = "VOID PURCHASE"
                    binding.tvDeclineLabel.text = "VOID"
                }
            }

            if (status.equals(
                    "REFUNDED",
                    ignoreCase = true
                ) ||status.equals(
                    "VOIDED",
                    ignoreCase = true
                )
            ) {
                binding.tvStatus.text = getString(R.string.transaction_successful)
            }else /*if (status == "NOT REFUNDED" || status == "NOT VOIDED")*/ {
                binding.ivDecline.setImageResource(R.drawable.ic_frame_decline)
                if (bundle.containsKey("responseMessage") && bundle.getInt("responseMessage") != null) {
                    binding.tvStatus.text = getString(bundle.getInt("responseMessage"))
                } else {
                    binding.tvStatus.text = "Transaction Failed"
                }

                if (bundle.getString("mode") != null) {
                    if (bundle.getString("mode") == "Refund" || bundle.getString("mode") == "Refund Order") {
                        binding.tvDeclineLabel.text = "Refund Declined"
                    } else {
                        binding.tvDeclineLabel.text = "Void Declined"
                    }
                }
            }


            try {
                binding.tvVisaNo.text = "**** **** **** " + cardNo?.substring(12, 16)
                binding.tvInvoiceNum.text = invoiceNo
                binding.tvVASRefID.text = vasRefNO
            } catch (e: Exception) {
                e.printStackTrace()
            }

            binding.tvTerminalNo.text =
                sharedPreferenceUtil.getTerminalID()//preference!!.getString(Constants.TERMINAL_ID, "")!!
            binding.tvNameOfTheBusiness.text =
                sharedPreferenceUtil.getMerchantName() + sharedPreferenceUtil.getMerchantLastName()//sharedPreferenceUtil.getBusinessType()
            binding.tvLocationDetails.text = sharedPreferenceUtil.getCountry()


            if (bundle.getString("totalAmount") != null) {

                val amount = bundle.getString("totalAmount")
                if (amount!!.isNotEmpty()) {
                    val amountVal = amount.toFloat()
                    binding.tvTOTAmount.text = "AED " + ContextUtils.formatWithCommas(amountVal.toDouble())
                   /* val splitTOTAmount = amountVal.toString().split(".").toTypedArray()
                    if (splitTOTAmount[1].length > 2) {
                        binding.tvTOTAmount.text =
                            "AED " + splitTOTAmount[0] + "." + splitTOTAmount[1].substring(0, 2)
                    } else {
                        binding.tvTOTAmount.text = "AED " + splitTOTAmount[0] + "." + splitTOTAmount[1]
                    }*/
                }
            }
        }

        //Add Order to PayRow database
        cardReceiptActivityViewModel =
            ViewModelProvider(
                this,
                CardReceiptActivityViewModelFactory(this)
            ).get(CardReceiptActivityViewModel::class.java)

        binding.tvMerchantNo.text = sharedPreferenceUtil.getMerchantID()

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
                if (validateInputFields()) {
                    if (optionSelected == "Email") {

                        if (etEmail.text?.isNotEmpty() == true) {

                            val finalURL =
                                CardReceiptActivity.url + ContextUtils.getBase64String(invoiceNo!!)

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
                                ?.observeOnce(this@VoidRFReceiptActivity) {
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
                val finalURL = CardReceiptActivity.url + ContextUtils.getBase64String(invoiceNo!!)
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

    private fun validateInputFields(): Boolean {
        if (optionSelected.isNotEmpty()
        ) {
            return true
        }
        return false
    }
}