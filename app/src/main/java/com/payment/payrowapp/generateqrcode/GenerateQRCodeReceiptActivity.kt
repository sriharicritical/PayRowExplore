package com.payment.payrowapp.generateqrcode

import android.content.Intent
import android.graphics.Bitmap
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
import com.payment.payrowapp.databinding.ActivityForgotTidsuccessfullBinding
import com.payment.payrowapp.databinding.ActivityQrcodeReceipt2Binding
import com.payment.payrowapp.dataclass.SendURLRequest
import com.payment.payrowapp.invoicerecall.QRCodeReceiptActivity
import com.payment.payrowapp.newpayment.CustomerCopySharedActivity
import com.payment.payrowapp.newpayment.PaymentSuccessfulActivityViewModel
import com.payment.payrowapp.newpayment.PaymentSuccessfulActivityViewModelFactory
import com.payment.payrowapp.observeOnce
import com.payment.payrowapp.retrofit.ApiClient
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.*
import java.text.SimpleDateFormat
import java.util.*

class GenerateQRCodeReceiptActivity : BaseActivity() {
    var invoiceNo: String? = null
    var optionSelected = "Email"
    var paymentSuccessfulActivityViewModel: PaymentSuccessfulActivityViewModel? = null
    private var payRowVATAmount: Float = 0.0F
    var payRowVATStatus = false

    companion object {
        const val url = ApiClient.BASE_URL_SOFT_POS + ApiClient.GENERATE_INVOICE
    }

    var ring: MediaPlayer? = null

    private lateinit var binding:ActivityQrcodeReceipt2Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_qrcode_receipt2)
        binding = ActivityQrcodeReceipt2Binding.inflate(layoutInflater)
        setContentView(binding.root)

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
            binding.tvDate.text = bundle.getString(Constants.DATE)
            binding.tvTime.text = bundle.getString("Time")
            UtilityClass.setMargins(this, binding.ivMashreq, 0, 9, 0, 0)
        } else {
            binding.toolbar.myToolbar.visibility = View.GONE
            val calender = Calendar.getInstance().time
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("hh:mm:ss", Locale.getDefault())

            binding.tvDate.text = dateFormat.format(calender)
            binding.tvTime.text = timeFormat.format(calender)
            UtilityClass.setMargins(this, binding.ivMashreq, 0, 35, 0, 0)
        }

        if (bundle?.getString("auth") != null && !bundle.getString("auth").isNullOrEmpty()) {
            binding.tvApprovalCodeLabel.visibility = View.VISIBLE
            binding.tvApprovalCodeLabel.text = "APPROVAL CODE " + bundle.getString("auth")
        }

        if (bundle?.getString("cardNumber") != null) {
            binding.tvVisaLabel.visibility = View.VISIBLE
            binding.tvVisaNo.visibility = View.VISIBLE
            binding.tvVisaNo.text = bundle?.getString("cardNumber")
            binding.tvVisaLabel.text = bundle?.getString("cardBrand")

            binding.tvCardBrandLabel.visibility = View.VISIBLE
            binding.tvCardBrand.visibility = View.VISIBLE
            binding.tvCardBrand.text = bundle?.getString("cardBrand")
        }

        invoiceNo = bundle?.getString("orderNumber")
        binding.tvInvoiceNum.text = invoiceNo
        /*  tvPayRowCharges.text = bundle?.getString("PayRowCharge")
          tvOtherCharges.text = bundle?.getString("other")
          tvBankCharges.text = bundle?.getString("bankFee")*/

        if (bundle?.getString("channel").equals("Paybylink")) {
            binding.tvCVMLabel.text = getString(R.string.paybylink_payment_utilized)
        } else {
            binding.tvCVMLabel.text = getString(R.string.qrcode_payment_utilized)
        }

        val status = bundle?.getString("status")
        if (status != null) {
            if (status.equals("CAPTURED", ignoreCase = true)
            ) {
                binding.tvPurchase.text = getString(R.string.transaction_successful)
            } else if (status.equals("OPEN", ignoreCase = true) || status.equals(
                    "DENIED BY RISK",
                    ignoreCase = true
                ) || status.equals(
                    "HOST TIMEOUT",
                    ignoreCase = true
                ) || status.equals("NOT APPROVED", ignoreCase = true) || status.equals(
                    "NOT CAPTURED",
                    ignoreCase = true
                ) || status.equals("CLOSED", ignoreCase = true) || status.equals(
                    "CANCELED",
                    ignoreCase = true
                )
            ) {
                binding.tvPurchase.text = getString(R.string.payment_failed)
                binding.tvPurchase.visibility = View.GONE
                binding.tvCVMLabel.visibility = View.GONE
                binding.tvCVMStatus.visibility = View.GONE
                binding.ConLayDecline.visibility = View.VISIBLE
                binding.tvStatus.text = getString(R.string.transaction_declined)
            } else if (status.equals("Pending", ignoreCase = true)) {
                binding.tvPurchase.text = getString(R.string.payment_failed)
                binding.tvPurchase.visibility = View.GONE
                binding.tvCVMLabel.visibility = View.GONE
                binding.tvCVMStatus.visibility = View.GONE
                binding.ConLayDecline.visibility = View.VISIBLE
                binding.tvStatus.text = getString(R.string.transaction_declined)
            } else if (status.equals("INPROGRESS", ignoreCase = true)
                || status.equals("CREATED", ignoreCase = true)
            ) {
                binding.tvPurchase.text = getString(R.string.in_progress)
                binding.tvPurchase.visibility = View.VISIBLE
                binding.tvCVMLabel.visibility = View.GONE
                binding.tvCVMStatus.visibility = View.GONE
                binding.ConLayDecline.visibility = View.VISIBLE
                binding.ivDecline.setImageResource(R.drawable.inprogress_rotate_left)
                if (bundle?.getString("channel").equals("Paybylink")) {
                    binding.tvDeclineLabel.text = "PayByLink"
                } else {
                    binding.tvDeclineLabel.text = "QRCode"
                }
                binding.tvStatus.text = getString(R.string.payment_inprogress)
            } else {
                binding.tvPurchase.visibility = View.GONE
                binding.tvCVMLabel.visibility = View.GONE
                binding.tvCVMStatus.visibility = View.GONE
                binding.ConLayDecline.visibility = View.VISIBLE
                binding.tvStatus.text = getString(R.string.transaction_declined)
                binding.tvPurchase.text = getString(R.string.payment_failed)
            }
        } else {
            binding.tvPurchase.text = getString(R.string.payment_failed)
            binding.tvPurchase.visibility = View.GONE
            binding.tvCVMLabel.visibility = View.GONE
            binding.tvCVMStatus.visibility = View.GONE
            binding.ConLayDecline.visibility = View.VISIBLE
            binding.tvStatus.text = getString(R.string.transaction_declined)
        }
        //set amount and va
        binding.tvBatchNo.text = bundle?.getString("Amount")

        if (bundle!!.containsKey("payRowVATStatus")) {
            // tvAmountLabel.text = getString(R.string.total_inc_vat)
            payRowVATAmount = bundle.getFloat("payRowVATAmount")
            payRowVATStatus = bundle.getBoolean("payRowVATStatus")
        }

        if (payRowVATStatus) {
            // tvPayRowChargesLbl.visibility = View.VISIBLE
            //  tvPayRowCharges.visibility = View.VISIBLE
            binding.tvAmountLabel.text = getString(R.string.total_inc_vat)
            //  tvPayRowCharges.text = payRowVATAmount.toString()
        }

        val totalAmount = bundle?.getString("totalAmount")
        binding.tvAmount.text = "AED " + ContextUtils.formatWithCommas(totalAmount!!.toDouble())//"AED " + bundle?.getString("totalAmount")


        val sharedPreferenceUtil = SharedPreferenceUtil(this)

        val merchantID =
            sharedPreferenceUtil.getMerchantID()
        binding.tvMerchantNo.text = merchantID
        binding.tvTerminalNo.text =
            sharedPreferenceUtil.getTerminalID()

        binding.tvNameOfTheBusiness.text =
            sharedPreferenceUtil.getMerchantName() + sharedPreferenceUtil.getMerchantLastName()//sharedPreferenceUtil.getBusinessType()
        binding.tvLocationDetails.text = sharedPreferenceUtil.getCountry()

        paymentSuccessfulActivityViewModel =
            ViewModelProvider(
                this,
                PaymentSuccessfulActivityViewModelFactory(this)
            ).get(PaymentSuccessfulActivityViewModel::class.java)

        binding.btnHome.setOnClickListener {
            startActivity(
                Intent(this, DashboardActivity::class.java).addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                )
            )
        }

        binding.btnMerchantCopy.setOnClickListener {
          ring?.start()
            //  SunmiPrintHelper.getInstance().initSunmiPrinterService(this)
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
            val ivPrint = view.findViewById<ImageView>(R.id.ivPrint)
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
            }

            ivProceed.setOnClickListener() {
                /*ActivityCompat.requestPermissions(
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

                //  if (read_permission == PackageManager.PERMISSION_GRANTED && write_permission == PackageManager.PERMISSION_GRANTED) {

                ring?.start()
                if (validateInputFields()) {
                    if (optionSelected == "WhatsApp") {
                        if (etWhatsApp.text?.isNotEmpty() == true) {


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
                            paymentSuccessfulActivityViewModel?.sendURLDetails(
                                sendURLRequest
                            )
                            paymentSuccessfulActivityViewModel?.sendURLLiveData()
                                ?.observeOnce(this@GenerateQRCodeReceiptActivity) {
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
                ivWhatsApp.setImageResource(R.drawable.ic_icon_whatsapp_active)
                ivEmail.setImageResource(R.drawable.ic_icon_email_default)
                ivSMS.setImageResource(R.drawable.ic_icon_sms_default)
                tvShareOptionTitle.text = "WhatsApp Number"
                etWhatsApp.inputType = InputType.TYPE_CLASS_NUMBER
            }
            ivEmail.setOnClickListener {
                clWhatapp.visibility = View.GONE
                etEmail.visibility = View.VISIBLE
                optionSelected = "Email"
                ivWhatsApp.setImageResource(R.drawable.ic_icon_whatsapp_default)
                ivEmail.setImageResource(R.drawable.ic_icon_email_active)
                ivSMS.setImageResource(R.drawable.ic_icon_email_default)
                tvShareOptionTitle.text = "Email"
            }
            ivSMS.setOnClickListener {
                optionSelected = "SMS"
                ivWhatsApp.setImageResource(R.drawable.ic_icon_whatsapp_default)
                ivEmail.setImageResource(R.drawable.ic_icon_email_default)
                ivSMS.setImageResource(R.drawable.ic_icon_sms_active)
                etWhatsApp.setHint("Enter SMS Number")
                tvShareOptionTitle.text = "SMS Number"
                etWhatsApp.inputType = InputType.TYPE_CLASS_NUMBER
            }

            ivPrint.setOnClickListener {

                val rootView = this.window.decorView.rootView
                rootView.isDrawingCacheEnabled = true
                val bitmapData = Bitmap.createBitmap(rootView.drawingCache)

                // val clCardReceipt = findViewById<ConstraintLayout>(R.id.clCardReceipt)
                //val bitmap = getBitmapFromViewUsingCanvas(clCardReceipt)

                //  SunmiPrintHelper.getInstance().printBitmap(bitmapData, 1)
                //  SunmiPrintHelper.getInstance().feedPaper()
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
    }

    private fun validateInputFields(): Boolean {
        if (optionSelected.isNotEmpty()
        ) {
            return true
        }
        return false
    }
}