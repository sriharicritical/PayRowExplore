package com.payment.payrowapp.newpayment

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
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
import com.payment.payrowapp.databinding.ActivityEnterAmountToPayCardBinding
import com.payment.payrowapp.databinding.ActivityPaymentSuccessfulBinding
import com.payment.payrowapp.dataclass.SendURLRequest
import com.payment.payrowapp.invoicerecall.QRCodeReceiptActivity
import com.payment.payrowapp.observeOnce
import com.payment.payrowapp.retrofit.ApiClient
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.*
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*


class PaymentSuccessfulActivity : BaseActivity() {
    private var ringPay: MediaPlayer? = null
    var optionSelected = "Email"
    private var paymentSuccessfulActivityViewModel: PaymentSuccessfulActivityViewModel? = null
    var invoiceNo: String? = null
    var payRowVATStatus = false
    var payRowVATAmount = 0.0F

    companion object {
        const val url = ApiClient.BASE_URL_SOFT_POS + ApiClient.GENERATE_INVOICE
    }

    private lateinit var binding: ActivityPaymentSuccessfulBinding
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
      //  setContentView(R.layout.activity_payment_successful)
        binding = ActivityPaymentSuccessfulBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent.extras

        if (bundle != null) {
            if (bundle.getString("type") != null && bundle.getString("type")
                    .equals("invoiceRecall")
            ) {
                binding.toolbar.myToolbar.visibility = View.VISIBLE
                setupToolbar()
                //setSupportActionBar(myToolbar)
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                supportActionBar?.setDisplayShowHomeEnabled(true)

                supportActionBar?.title = "Invoice Recall"
                binding.tvDate.text = bundle.getString(Constants.DATE)
                binding.tvTime.text = bundle.getString("Time")
                UtilityClass.setMargins(this, binding.ivMashreq, 0, 1, 0, 0)
            } else {
                //set the current data and time
                val calender = Calendar.getInstance().time
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val timeFormat = SimpleDateFormat("hh:mm:ss", Locale.getDefault())

                binding.tvDate.text = dateFormat.format(calender)
                binding.tvTime.text = timeFormat.format(calender)
                binding.toolbar.myToolbar.visibility = View.GONE
                UtilityClass.setMargins(this, binding.ivMashreq, 0, 35, 0, 0)
            }

            invoiceNo = bundle.getString("InvoiceNo")
            if (bundle.getString("InvoiceNo") != null) {
                binding.tvInvoiceNum.text =  bundle.getString("InvoiceNo")
            }

        //    tvInvoiceNum.text = invoiceNo

            //set amount and vat
            val amount = bundle.getString("Amount")
            binding.tvBatchNo.text = ContextUtils.formatWithCommas(amount!!.toDouble())//bundle.getString("Amount")

            // val amount = bundle?.getString("Amount")!!.toFloat()
            payRowVATAmount = bundle.getFloat("payRowVATAmount")
            payRowVATStatus = bundle.getBoolean("payRowVATStatus")

            val totalAmount = bundle.getString("TotalAmount")

            if (payRowVATStatus) {
                // tvVATLabel.visibility = View.VISIBLE
                // tvVATCharges.visibility = View.VISIBLE
                binding.tvAmountLabel.text = getString(R.string.total_inc_vat)

                /*  val splitString = payRowVATAmount.toString().split(".").toTypedArray()
                  if (splitString[1].length > 2) {
                      tvVATCharges.text =
                          splitString[0] + "." + splitString[1].substring(0, 2)
                  } else {
                      tvVATCharges.text =
                          splitString[0] + "." + splitString[1]
                  }*/
            }
            // tvVATCharges.text = vatAmount.toString()

            binding.tvAmount.text = "AED " + ContextUtils.formatWithCommas(totalAmount!!.toDouble())
            /*val splitTOTAmount = totalAmount.toString().split(".").toTypedArray()
            if (splitTOTAmount[1].length > 2) {
                binding.tvAmount.text = "AED " + splitTOTAmount[0] + "." + splitTOTAmount[1].substring(0, 2)
            } else {
                binding.tvAmount.text = "AED " + splitTOTAmount[0] + "." + splitTOTAmount[1]
            }*/

            // tvAmount.text = "AED $totalAmount"
            val cashReceived = bundle.getString("CashReceived")
            binding.tvVATNo.text =  ContextUtils.formatWithCommas(cashReceived!!.toDouble())//bundle.getString("CashReceived")
            val balance = bundle.getString("Balance")
            binding.tvBalAmount.text = ContextUtils.formatWithCommas(balance!!.toDouble())

            /*if (balance!!.contains(".")) {
                val splitBalAmount = balance.toString().split(".").toTypedArray()
                if (splitBalAmount[1].length > 2) {
                    binding.tvBalAmount.text = splitBalAmount[0] + "." + splitBalAmount[1].substring(0, 2)
                } else {
                    binding.tvBalAmount.text = splitBalAmount[0] + "." + splitBalAmount[1]
                }
            } else {
                binding.tvBalAmount.text = balance
            }*/
        }

        val sharedPreferenceUtil = SharedPreferenceUtil(this)

        val merchantID =
            sharedPreferenceUtil.getMerchantID()//preference!!.getString(Constants.MERCHANT_ID, "")
        binding.tvMerchantNo.text = merchantID
        binding.tvTerminalNo.text =
            sharedPreferenceUtil.getTerminalID()//preference!!.getString(Constants.TERMINAL_ID, "")!!

        binding.tvNameOfTheBusiness.text =
            sharedPreferenceUtil.getMerchantName() + sharedPreferenceUtil.getMerchantLastName()//sharedPreferenceUtil.getBusinessType()
        binding.tvLocationDetails.text = sharedPreferenceUtil.getCountry()

        ringPay = MediaPlayer.create(this, R.raw.sound_button_click)


        paymentSuccessfulActivityViewModel =
            ViewModelProvider(
                this,
                PaymentSuccessfulActivityViewModelFactory(this)
            )[PaymentSuccessfulActivityViewModel::class.java]

        binding.btnHome.setOnClickListener {
        ringPay?.start()
            startActivity(
                Intent(
                    this,
                    DashboardActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
        }

        binding.btnMerchantCopy.setOnClickListener {
            ringPay?.start()
            //   SunmiPrintHelper.getInstance().initSunmiPrinterService(this)
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


            //val bundle = intent.extras
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
                ringPay?.start()
                bottomSheetDialog.dismiss()
            }

            btnHome.setOnClickListener {
                ringPay?.start()
                startActivity(
                    Intent(
                        this,
                        DashboardActivity::class.java
                    ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
            }

            ivProceed.setOnClickListener {
                ringPay?.start()
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

                //if (read_permission == PackageManager.PERMISSION_GRANTED && write_permission == PackageManager.PERMISSION_GRANTED) {
                if (validateInputFields()) {
                    if (optionSelected == "WhatsApp") {
                        if (etWhatsApp.text?.isNotEmpty() == true) {

                            sharePDF(
                                paymentSuccessfulActivityViewModel!!,
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
                            paymentSuccessfulActivityViewModel?.sendURLDetails(
                                sendURLRequest
                            )
                            paymentSuccessfulActivityViewModel?.sendURLLiveData()
                                ?.observeOnce(this@PaymentSuccessfulActivity) {
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
                /*} else {
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
                etWhatsApp.hint = "Enter SMS Number"
                tvShareOptionTitle.text = "SMS Number"
                etWhatsApp.inputType = InputType.TYPE_CLASS_NUMBER
            }

            ivPrint.setOnClickListener {

                val rootView = this.window.decorView.rootView
                rootView.isDrawingCacheEnabled = true
                val bitmapData = Bitmap.createBitmap(rootView.drawingCache)

                // val clCardReceipt = findViewById<ConstraintLayout>(R.id.clCardReceipt)
                //val bitmap = getBitmapFromViewUsingCanvas(clCardReceipt)

                /*  SunmiPrintHelper.getInstance().printBitmap(bitmapData, 1)
                  SunmiPrintHelper.getInstance().feedPaper()*/
            }

            ivQRCode.setOnClickListener {
                ringPay?.start()
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

    private fun sharePDF(
        paymentSuccessfulActivityViewModel: PaymentSuccessfulActivityViewModel,
        to: String,
        receiptType: String,
        phoneNo: String,
        url: String
    ) {
        try {
            // var message = ""
            paymentSuccessfulActivityViewModel.getPDFLinkDetails(invoiceNo!!)
            paymentSuccessfulActivityViewModel.getPDFLinkLiveData()
                .observeOnce(this@PaymentSuccessfulActivity) {
                    // message=message.replace("""""\""","")
                    val packageManager = packageManager
                    val stringUrl =
                        "https://api.whatsapp.com/send?phone=" + phoneNo + "&text=" + URLEncoder.encode(
                            it.message,
                            "UTF-8"
                        )
                    val shareIntent = Intent(Intent.ACTION_VIEW)
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

}
