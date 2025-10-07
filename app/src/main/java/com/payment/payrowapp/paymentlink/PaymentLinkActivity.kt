package com.payment.payrowapp.paymentlink


import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.messaging.FirebaseMessaging
import com.payment.payrowapp.R
import com.payment.payrowapp.crypto.HeaderSignatureUtil
import com.payment.payrowapp.dashboard.DashboardActivity
import com.payment.payrowapp.databinding.ActivityLoginNewBinding
import com.payment.payrowapp.databinding.ActivityPaymentLinkBinding
import com.payment.payrowapp.dataclass.*
import com.payment.payrowapp.generateqrcode.GenerateQRCodeActivity
import com.payment.payrowapp.invoicerecall.QRCodeReceiptActivity
import com.payment.payrowapp.newpayment.CustomerCopySharedActivity
import com.payment.payrowapp.newpayment.StoreItemsRepository
import com.payment.payrowapp.observeOnce
import com.payment.payrowapp.qrcodescan.QRCodeScanActivity
import com.payment.payrowapp.retrofit.ApiClient
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.BaseActivity
import com.payment.payrowapp.utils.Constants
import com.payment.payrowapp.utils.ContextUtils
import com.payment.payrowapp.dataclass.ItemsCountRequest
import com.payment.payrowapp.servicecatalogue.GatewayItemsActivity
import com.payment.payrowapp.sunmipay.LoadingDialog
import com.payment.payrowapp.utils.LoaderCallback
import org.json.JSONObject

class PaymentLinkActivity : BaseActivity(), LoaderCallback {
    private var urn: String? = null
    private var merchantSiteUrl: String? = null
    var totalAmount: Float = 0.0F
    var optionSelected = "Email"
    private var itemCount = 0
    private var totalAmountQRCode = "0"
    lateinit var type: String
    private var toggleExpiration = false
    private var purchaseBreakdown: PurchaseBreakdown? = null
    private var purchaseDetails: PurchaseDetails? = null
    private var itemDetailList = ArrayList<Service>()

    private var customerBillingPostalCode: String? = null
    private var customerBillingState: String? = null
    private var customerBillingCity: String? = null
    private var customerBillingCountry: String? = null
    private var customerName: String? = null
    private var posId: String? = null
    private var mainMerchantId: String? = null
    private var customerEmail: String? = null
    private var customerPhone: String? = null
    private var posType: String? = null
    private var payrowInvoiceNo: String? = null
    private var trnNo: String? = null
    private var receiptNo: String? = null
    private var merchantEmail: String? = null
    private var userId: String? = null
    private var distributorId: String? = null
    private var storeId: String? = null
    private var bankTransferURL: String? = null
    private var orderNumber: String? = null
    private var paymentType = "Paybylink"
    private lateinit var channel: String
    private lateinit var sharedPreferenceUtil: SharedPreferenceUtil
    private var serviceList = ArrayList<Service>()
    private var bookNumber: String? = null
    private var strFCMToken: String? = null
    var payRowVATStatus = false
    var payRowVATAmount = 0.0F
    var totalAmountVAT: Float = 0.0F

    private lateinit var binding:ActivityPaymentLinkBinding
    var ring: MediaPlayer? = null
    private var loadDialog: LoadingDialog? = null
    private var checkoutId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_payment_link)
        binding = ActivityPaymentLinkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
       // setSupportActionBar(myToolbar)
        setupToolbar()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        ring = MediaPlayer.create(this, R.raw.sound_button_click)
        val bundle = intent.extras
        type = bundle?.getString("TYPE")!!
        if (type == "PAYBYLINK") {
            supportActionBar?.title = "Pay By Link"
        } else {
            binding.btnQRMultiUse.visibility = View.VISIBLE
            binding.ivMultiON.visibility = View.VISIBLE
            binding.btnScanBarCode.visibility = View.GONE
            if (type == "QRCODE") {
                supportActionBar?.title = "Pay by QR code"
            } else {
                supportActionBar?.title = "Pay by SMS"
            }
            binding.logoMashref.setImageDrawable(resources.getDrawable(R.drawable.icon_payrow))
        }

        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    strFCMToken = task.result
                }
            }
        } catch (e: Exception) {
            //   Toast.makeText(this, "crashed"+e.message, Toast.LENGTH_LONG).show()
        }

        sharedPreferenceUtil = SharedPreferenceUtil(this)

        if (sharedPreferenceUtil.getCataLogAmount() && sharedPreferenceUtil.getScanBarCode()) {

            binding.etEnterAmount.isEnabled = false
            binding.btnAddItem.visibility = View.VISIBLE

            val marginInDp = 16
            val marginInPx = (marginInDp * resources.displayMetrics.density).toInt()

            binding.btnScanBarCode.visibility = View.VISIBLE
            if (type == "PAYBYLINK") {
                val params = binding.btnScanBarCode.layoutParams as ConstraintLayout.LayoutParams
                params.topMargin = marginInPx
                binding.btnScanBarCode.layoutParams = params
            } else {
                val params3 = binding.btnScanBarCode.layoutParams as ConstraintLayout.LayoutParams
                params3.topMargin = marginInPx
                binding.btnScanBarCode.layoutParams = params3

                val params = binding.btnQRMultiUse.layoutParams as ConstraintLayout.LayoutParams
                params.topMargin = marginInPx
                binding.btnQRMultiUse.layoutParams = params

                val marginInDp1 = 32
                val marginInPx1 = (marginInDp1 * resources.displayMetrics.density).toInt()

                val params2 = binding.ivMultiON.layoutParams as ConstraintLayout.LayoutParams
                params2.topMargin = marginInPx1
                binding.ivMultiON.layoutParams = params2
            }
        } else if (sharedPreferenceUtil.getScanBarCode()) {
            binding.etEnterAmount.isEnabled = false
            binding.btnScanBarCode.visibility = View.VISIBLE
        } else if (sharedPreferenceUtil.getCataLogAmount()) {
            binding.etEnterAmount.isEnabled = false
            binding.btnAddItem.visibility = View.VISIBLE

            val marginInDp = 16
            val marginInPx = (marginInDp * resources.displayMetrics.density).toInt()

            //  btnScanBarCode.visibility = View.GONE
            /*  if (type == "PAYBYLINK") {
                  val params = btnScanBarCode.layoutParams as ConstraintLayout.LayoutParams
                  params.topMargin = marginInPx
                  btnScanBarCode.layoutParams = params
              } else {
                  val params3 = btnScanBarCode.layoutParams as ConstraintLayout.LayoutParams
                  params3.topMargin = marginInPx
                  btnScanBarCode.layoutParams = params3

                  val params = btnQRMultiUse.layoutParams as ConstraintLayout.LayoutParams
                  params.topMargin = marginInPx
                  btnQRMultiUse.layoutParams = params

                  val marginInDp1 = 32
                  val marginInPx1 = (marginInDp1 * resources.displayMetrics.density).toInt()

                  val params2 = ivMultiON.layoutParams as ConstraintLayout.LayoutParams
                  params2.topMargin = marginInPx1
                  ivMultiON.layoutParams = params2
              }*/
        } else {
            val itemsCountRequest = ItemsCountRequest(
                sharedPreferenceUtil.getGatewayMerchantID(),
                sharedPreferenceUtil.getMerchantID()
            )
            StoreItemsRepository.getItemCountMutableLiveData(
                this,
                itemsCountRequest,
                sharedPreferenceUtil
            )
            StoreItemsRepository.getItemCountLiveData().observeOnce(this@PaymentLinkActivity) {
                if (it?.data != null) {
                    SharedData.sharedArrayList = arrayListOf()
                    itemCount = 1
                    val dataObject = JSONObject(it.data)

                    val gateWayArray = dataObject.optJSONArray("defaultGWServ")//.getJSONObject(0)
                    val gatewayObject = gateWayArray?.optJSONObject(0)
                    if (gatewayObject != null) {
                        val service = Service(
                            gatewayObject.getString("serviceId"),
                            gatewayObject.getString("shortServiceName"), 1,
                            gatewayObject.getString("unitPrice").toDouble(), 0.0, 1
                        )
                        SharedData.sharedArrayList.add(service)
                    }
                }
            }
        }

        val merchantID =
            sharedPreferenceUtil.getMerchantID()//preference!!.getString(Constants.MERCHANT_ID, "")
        binding.btnInvoiceNumber.text = "MID: $merchantID"
        binding.tvStoreName.text =
            sharedPreferenceUtil.getMerchantName() + sharedPreferenceUtil.getMerchantLastName()
        val paymentLinkViewModel =
            ViewModelProvider(
                this,
                PaymentLinkViewModelFactory(this)
            )[PaymentLinkViewModel::class.java]

        binding.etEnterAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

                if (binding.etEnterAmount.text.toString().isNotEmpty()) {
                    totalAmount = binding.etEnterAmount.text.toString().toFloat()
                    // sharedPreferenceUtil.setAmount(etEnterAmount.text.toString())
                    if (sharedPreferenceUtil.getVATCalculator()) {
                        payRowVATStatus = true
                        payRowVATAmount = (totalAmount / 100.0f) * Constants.VAT_PER
                        totalAmountVAT = payRowVATAmount + totalAmount
                    } else {
                        totalAmountVAT = totalAmount
                    }
                }
                binding.btnPay.setBackgroundResource(R.drawable.button_round_dark_gray_bg_fill)
                val img: Drawable =
                    resources.getDrawable(R.drawable.ic_icon_pay_3_lines_green, null)
                val img2: Drawable =
                    resources.getDrawable(R.drawable.ic_baseline_arrow_forward_24, null)
                binding.btnPay.setCompoundDrawablesWithIntrinsicBounds(img, null, img2, null)

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //To change body of created functions use File | Settings | File Templates.
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //To change body of created functions use File | Settings | File Templates.
                try {
                    if (p0?.length == 0) {
                        binding.btnPay.setBackgroundResource(R.drawable.button_round_gray_bg_fill)
                        val img: Drawable =
                            resources.getDrawable(R.drawable.ic_pay_with_3_lines, null)
                        val img2: Drawable =
                            resources.getDrawable(R.drawable.ic_baseline_arrow_forward_24, null)
                        binding.btnPay.setCompoundDrawablesWithIntrinsicBounds(img, null, img2, null)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })


        binding.btnAddItem.setOnClickListener {
            ring?.start()
            val bundle1: Bundle = Bundle()
            bundle1.putString("TYPE", "TAPTOPAY")
            startActivityForResult((Intent(this, GatewayItemsActivity::class.java)), 2)
        }
        binding.btnScanBarCode.setOnClickListener {
            ring?.start()
            startActivityForResult((Intent(this, QRCodeScanActivity::class.java)), 3)
        }

        binding.ivMultiON.setOnClickListener {
            ring?.start()
            toggleExpiration = if (toggleExpiration) {
                binding.ivMultiON.setImageResource(R.drawable.ic_multi_enable_off)
                false
            } else {
                binding.ivMultiON.setImageResource(R.drawable.ic_multi_user_enable)
                true
            }
        }

        binding.btnPay.setOnClickListener {
            //showToast("The link has been successfully sent to the customer")
            ring?.start()
            if (itemCount > 0) {
                if (binding.etEnterAmount.text?.isNotEmpty() == true) {
                    if (binding.etEnterAmount.text.toString().toFloat() <= 1000000) {
                        if (binding.etEnterAmount.text.toString().toFloat() > 0) {
                            if (type == "PAYBYLINK") {

                                if (purchaseBreakdown != null) {
                                    purchaseDetails = PurchaseDetails(itemDetailList)
                                } else if (bookNumber != null) {
                                    /* val service =
                                         Service(
                                             "10000",
                                             1,
                                             etEnterAmount.text.toString().toFloat(),
                                             1
                                         )
                                     serviceList.add(service)
                                     purchaseDetails = PurchaseDetails(serviceList)*/
                                } else {

                                    if (sharedPreferenceUtil.getCataLogAmount()) {
                                        serviceList = SharedData.sharedArrayList
                                    } else {
                                        SharedData.sharedArrayList[0].transactionAmount =
                                            totalAmountVAT.toDouble()
                                        serviceList = SharedData.sharedArrayList
                                    }

                                    purchaseDetails = PurchaseDetails(serviceList)
                                }

                                if (orderNumber.isNullOrEmpty()) {
                                    orderNumber = ContextUtils.randomValue()
                                        .toString() + ContextUtils.getRandomLastValue()
                                }

                                channel = if (paymentType == "scan qr") {
                                    "Third Party QRCode"
                                } else {
                                    "Paybylink"
                                }

                                if (bankTransferURL.isNullOrEmpty()) {
                                    bankTransferURL = ApiClient.MERCHANT_BANK_TRANS_URL
                                }

                                if (customerName.isNullOrEmpty()) {
                                    customerName = sharedPreferenceUtil.getMerchantName()
                                }

                                if (customerEmail.isNullOrEmpty()) {
                                    customerEmail = sharedPreferenceUtil.getMailID()
                                }

                                if (customerBillingCity.isNullOrEmpty()) {
                                    customerBillingCity = sharedPreferenceUtil.getCity()
                                }

                                if (customerBillingState.isNullOrEmpty()) {
                                    customerBillingState = sharedPreferenceUtil.getAddress()
                                }

                                if (customerBillingCountry.isNullOrEmpty()) {
                                    customerBillingCountry = sharedPreferenceUtil.getCountry()
                                }

                                if (customerBillingPostalCode.isNullOrEmpty()) {
                                    customerBillingPostalCode = sharedPreferenceUtil.getBOBox()
                                }

                                if (urn.isNullOrEmpty()) {
                                    urn = sharedPreferenceUtil.getURN()//Constants.URN
                                }

                                if (merchantSiteUrl.isNullOrEmpty()) {
                                    merchantSiteUrl = ApiClient.MERCHANT_BANK_TRANS_URL
                                }

                                if (customerPhone.isNullOrEmpty()) {
                                    customerPhone =
                                        sharedPreferenceUtil.getMerchantMobileNumber()//preference!!.getString(Constants.MERCHANT_MOBILE_NUMBER, "")!!
                                }

                                val paymentMethodList =
                                    arrayOf(Constants.EDIRHAM_CARD, Constants.NON_EDIRHAM_CARD)
                                val paymentLinkRequest = PaymentLinkRequest(
                                    null,
                                    orderNumber!!,
                                    customerBillingState!!,
                                    customerBillingState!!,
                                    "EN",
                                    "Paybylink",
                                    true,
                                    true,
                                    merchantSiteUrl!!,
                                    bankTransferURL!!,
                                    paymentMethodList,
                                    Constants.SESSIONS_TIMEOUT_SEC,
                                    customerName!!,
                                    urn!!,
                                    Constants.EDIRHAM_CARD,
                                    "Pending",
                                    sharedPreferenceUtil.getMerchantEmail(),
                                    sharedPreferenceUtil.getMerchantPhone(),
                                    customerBillingCity!!,
                                    customerBillingCity!!,
                                    customerBillingCountry!!,
                                    customerBillingPostalCode!!,
                                    purchaseDetails!!,
                                    totalAmount.toString(),
                                    strFCMToken,
                                    sharedPreferenceUtil.getTerminalID(),
                                    sharedPreferenceUtil.getDistributorID(),
                                    customerEmail,
                                    customerPhone,
                                    sharedPreferenceUtil.getBussinessId(),
                                    "Pending",
                                    sharedPreferenceUtil.getMerchantEmail(),
                                    sharedPreferenceUtil.getMerchantPhone(),
                                    sharedPreferenceUtil.getReportID(),
                                    HeaderSignatureUtil.getDeviceSN(),
                                    payRowVATStatus,
                                    payRowVATAmount, sharedPreferenceUtil.getGatewayMerchantID(),
                                    checkoutId)
                                showLoadingDialog("Please wait..")
                                paymentLinkViewModel.getPaymentLinkResponse(paymentLinkRequest,this)

                                paymentLinkViewModel.getData().observeOnce(this) {
                                    // showToast("Received payment link ${it.checkoutUrl}")
                                    dismissLoadingDialog()
                                    if (it?.checkoutUrl != null) {
                                        val checkOutURL = it.checkoutUrl
                                        val bottomSheetDialog = BottomSheetDialog(this)
                                        val view =
                                            layoutInflater.inflate(
                                                R.layout.share_bottom_sheet,
                                                null
                                            )
                                        bottomSheetDialog.setCancelable(true)
                                        bottomSheetDialog.setContentView(view)

                                        val ivProceed = view.findViewById<Button>(R.id.ivProceed)
                                        val etWhatsApp =
                                            view.findViewById<EditText>(R.id.etWhatsApp)
                                        val ivWhatsApp =
                                            view.findViewById<ImageView>(R.id.ivWhatsApp)
                                        val ivEmail = view.findViewById<ImageView>(R.id.ivEmail)
                                        val ivSMS = view.findViewById<ImageView>(R.id.ivSMS)
                                        val tvShareOptionTitle =
                                            view.findViewById<TextView>(R.id.tvShareOptionTitle)
                                        val btnHome = view.findViewById<Button>(R.id.btnHome)
                                        val etEmail = view.findViewById<EditText>(R.id.etEmail)
                                        val clWhatapp =
                                            view.findViewById<ConstraintLayout>(R.id.clWhatsapp)
                                        val ivQRCode = view.findViewById<ImageView>(R.id.ivQRCode)

                                        val ivBack = view.findViewById<ImageView>(R.id.ivBackBtn)
                                        ivBack.setOnClickListener {
                                            ring?.start()
                                            bottomSheetDialog.dismiss()
                                        }

                                        ivQRCode.setOnClickListener {
                                            ring?.start()
                                            startActivity(
                                                Intent(
                                                    this,
                                                    QRCodeReceiptActivity::class.java
                                                ).putExtra("InvoiceURL", checkOutURL)
                                            )

                                        }
                                        btnHome.setOnClickListener {
                                            ring?.start()
                                            startActivity(
                                                Intent(
                                                    this,
                                                    DashboardActivity::class.java
                                                ).addFlags(
                                                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                                                )
                                            )
                                        }

                                        ivWhatsApp.setOnClickListener {
                                            val bundle1 = Bundle()
                                            bundle1.putString("TYPE", "")
                                            optionSelected = "WhatsApp"
                                            clWhatapp.visibility = View.VISIBLE
                                            etEmail.visibility = View.GONE
                                            ivWhatsApp.setImageDrawable(resources.getDrawable(R.drawable.ic_icon_whatsapp_active))
                                            ivEmail.setImageDrawable(resources.getDrawable(R.drawable.ic_icon_email_default))
                                            ivSMS.setImageDrawable(resources.getDrawable(R.drawable.ic_icon_sms_default))
                                            tvShareOptionTitle.text = "WhatsApp Number"
                                            etWhatsApp.inputType = InputType.TYPE_CLASS_NUMBER
                                        }
                                        ivEmail.setOnClickListener {
                                            optionSelected = "Email"
                                            clWhatapp.visibility = View.GONE
                                            etEmail.visibility = View.VISIBLE
                                            ivWhatsApp.setImageDrawable(resources.getDrawable(R.drawable.ic_icon_whatsapp_default))
                                            ivEmail.setImageDrawable(resources.getDrawable(R.drawable.ic_icon_email_active))
                                            ivSMS.setImageDrawable(resources.getDrawable(R.drawable.ic_icon_sms_default))
                                            tvShareOptionTitle.text = "Email"
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
                                        ivProceed.setOnClickListener {
                                            ring?.start()
                                            //  if (read_permission == PackageManager.PERMISSION_GRANTED && write_permission == PackageManager.PERMISSION_GRANTED) {

                                            if (validateInputFields()) {
                                                if (optionSelected == "WhatsApp") {
                                                    if (etWhatsApp.text?.isNotEmpty() == true) {
/*
                                                    sharePDF(
                                                        "customer",
                                                        "link",
                                                        "971" + etWhatsApp.text.toString(),
                                                        checkOutURL
                                                    )
*/

                                                    } else {

                                                        Toast.makeText(
                                                            this,
                                                            "Please enter whatsapp number to proceed",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                } else if (optionSelected == "Email") {

                                                    if (etEmail.text?.isNotEmpty() == true) {

                                                        val sendURLRequest =
                                                            SendURLRequest(
                                                                "PayRow Payment Link",
                                                                etEmail.text.toString(),
                                                                checkOutURL, null
                                                            )
                                                        paymentLinkViewModel.sendURLDetails(
                                                            sendURLRequest
                                                        )
                                                        paymentLinkViewModel.sendURLLiveData()
                                                            .observeOnce(this@PaymentLinkActivity) {
                                                                bottomSheetDialog.dismiss()
                                                                startActivity(
                                                                    Intent(
                                                                        this,
                                                                        CustomerCopySharedActivity::class.java
                                                                    ).putExtra(
                                                                        "Payment Type",
                                                                        "Pay By Link"
                                                                    ).putExtra(
                                                                        "orderNumber",
                                                                        orderNumber
                                                                    )
                                                                        .putExtra(
                                                                            "amount",
                                                                            binding.etEnterAmount.text.toString()
                                                                        )
                                                                )
                                                            }
                                                        /*sendMail(
                                                    this@PaymentLinkActivity,
                                                    etEmail.text.toString(),
                                                    "PayRow Payment Link",
                                                    checkOutURL
                                                )*/

                                                    } else {
                                                        Toast.makeText(
                                                            this,
                                                            "Please enter email id to proceed",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                } else if (optionSelected == "SMS") {

                                                    Toast.makeText(
                                                        this,
                                                        "Work in progress...",
                                                        Toast.LENGTH_SHORT
                                                    )
                                                        .show()
                                                }
                                            } else {
                                                Toast.makeText(
                                                    this,
                                                    "Please select an option to proceed",
                                                    Toast.LENGTH_SHORT
                                                )
                                                    .show()
                                            }
                                            /*  } else {
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
                                        bottomSheetDialog.show()
                                    }
                                }
                            } else {
                                itemCount = 0
                                binding.btnItemCount.visibility = View.GONE
                                val intent = Intent(this, GenerateQRCodeActivity::class.java)

                                if (purchaseBreakdown != null) {
                                    SharedData.sharedArrayList = itemDetailList
                                    intent.putExtra(
                                        "purchaseBreakDown",
                                        "purchaseBreakdown"
                                    )
                                }

                                if (customerName != null) {
                                    intent.putExtra(
                                        "customerName",
                                        customerName
                                    )
                                }

                                if (orderNumber != null) {
                                    intent.putExtra(
                                        "OrderNumber",
                                        orderNumber
                                    )
                                }

                                if (bankTransferURL != null) {
                                    intent.putExtra(
                                        "MerchantBankURL",
                                        bankTransferURL
                                    )
                                }

                                if (customerEmail != null) {
                                    intent.putExtra(
                                        "customerEmail",
                                        customerEmail
                                    )
                                }

                                if (customerPhone != null) {
                                    intent.putExtra(
                                        "customerPhone",
                                        customerPhone
                                    )
                                }


                                if (customerBillingCity != null) {
                                    intent.putExtra(
                                        "customerBillingCity",
                                        customerBillingCity
                                    )
                                }

                                if (customerBillingState != null) {
                                    intent.putExtra(
                                        "customerBillingState",
                                        customerBillingState
                                    )
                                }

                                if (customerBillingCountry != null) {
                                    intent.putExtra(
                                        "customerBillingCountry",
                                        customerBillingCountry
                                    )
                                }

                                if (customerBillingPostalCode != null) {
                                    intent.putExtra(
                                        "customerBillingPostalCode",
                                        customerBillingPostalCode
                                    )
                                }

                                if (merchantSiteUrl != null) {
                                    intent.putExtra(
                                        "merchantSiteUrl",
                                        merchantSiteUrl
                                    )
                                }


                                if (urn != null) {
                                    intent.putExtra(
                                        "urn",
                                        urn
                                    )
                                }

                                if (checkoutId != null) {
                                    intent.putExtra(
                                        "checkoutId",
                                        checkoutId
                                    )
                                }

                                startActivity(
                                    intent.putExtra(
                                        Constants.TOTAL_AMOUNT,
                                        binding.etEnterAmount.text.toString()
                                    )
                                        .putExtra("toggleExpire", toggleExpiration)
                                        .putExtra("VatStatus", payRowVATStatus)
                                        .putExtra("VatAmount", payRowVATAmount)
                                )
                                binding.etEnterAmount.setText("")
                                finish()
                               /* itemCount = 0
                                binding.btnItemCount.visibility = View.GONE
                                val intent = Intent(this, GenerateQRCodeActivity::class.java)

                                startActivity(
                                    intent.putExtra(
                                        Constants.TOTAL_AMOUNT,
                                        binding.etEnterAmount.text.toString()
                                    )
                                        .putExtra("toggleExpire", toggleExpiration)
                                        .putExtra("VatStatus", payRowVATStatus)
                                        .putExtra("VatAmount", payRowVATAmount)
                                )
                                binding.etEnterAmount.setText("")*/
                            }
                        } else {
                            showToast("Please enter amount is greater than zero")
                        }
                    } else {
                        showToast("Kindly enter lesser amount.")
                    }
                } else {
                    showToast("Please enter the amount to proceed")
                }
            } else {
                if (sharedPreferenceUtil.getCataLogAmount() || (sharedPreferenceUtil.getScanBarCode() && sharedPreferenceUtil.getCataLogAmount())) {
                    showToast(getString(R.string.please_select_item_to_proceed))
                } else if (sharedPreferenceUtil.getScanBarCode()) {
                    showToast(getString(R.string.kinldy_scan_the_qr_fech_the_amount))
                } else {
                    showToast(getString(R.string.default_items_not_available))
                    finish()
                }
              //  showToast("Please select the items to proceed")
            }

        }
    }//oncreate

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data != null) {
            //  data.getStringExtra("itemList")
            itemCount = data.getIntExtra("itemLength", 0)
            totalAmountQRCode = data.getStringExtra("totalAmount").toString()

            paymentType = "Paybylink"
            if (itemCount > 0) {
                binding.btnItemCount.visibility = View.VISIBLE
                binding.btnItemCount.text =
                    "+" + data.getIntExtra("itemLength", 0).toString() + " items"
            }

            if (resultCode == 2) {
                if (itemCount > 0) {

                    //  if (sharedPreferenceUtil.getCataLogAmount()) {
                    binding.etEnterAmount.isEnabled = false
                    val tranAmount = data.getStringExtra("transactionAmount").toString()
                    binding.etEnterAmount.setText(ContextUtils.formatWithCommas(tranAmount.toDouble()))
                    //  }
                }
            } else if (resultCode == 3) {
                if (itemCount > 0) {

                    if (!sharedPreferenceUtil.getCataLogAmount()) {
                        SharedData.sharedArrayList = arrayListOf()
                    }

                    paymentType = "scan qr"
                    binding.etEnterAmount.setText(ContextUtils.formatWithCommas(totalAmountQRCode.toDouble()))

                    bankTransferURL = data.extras!!.getString("MerchantBankURL")
                    orderNumber = data.extras!!.getString("OrderNumber")
                    storeId = data.extras!!.getString("storeId")
                    distributorId = data.extras!!.getString("distributorId")
                    userId = data.extras!!.getString("userId")
                    merchantEmail = data.extras!!.getString("merchantEmail")
                    receiptNo = data.extras!!.getString("receiptNo")
                    trnNo = data.extras!!.getString("trnNo")
                    payrowInvoiceNo = data.extras!!.getString("payrowInvoiceNo")
                    posType = data.extras!!.getString("posType")

                    customerPhone = data.extras!!.getString("customerPhone")
                    customerEmail = data.extras!!.getString("customerEmail")
                    mainMerchantId = data.extras!!.getString("mainMerchantId")
                    posId = data.extras!!.getString("posId")

                    customerName = data.extras!!.getString("customerName")
                    customerBillingCountry = data.extras!!.getString("customerBillingCountry")
                    customerBillingCity = data.extras!!.getString("customerBillingCity")
                    customerBillingState = data.extras!!.getString("customerBillingState")
                    customerBillingPostalCode = data.extras!!.getString("customerBillingPostalCode")

                    merchantSiteUrl = data.extras!!.getString("merchantSiteUrl")
                    urn = data.extras!!.getString("urn")
                    purchaseBreakdown =
                        data.extras!!.get("purchaseBreakDown")!! as PurchaseBreakdown
                    checkoutId = data.extras!!.getString("checkoutId")
                    if (purchaseBreakdown != null && purchaseBreakdown!!.service.size > 0) {
                        for (itemDetail in purchaseBreakdown!!.service) {

                            val totalAmountVAT = if (sharedPreferenceUtil.getVATCalculator()) {
                                val payRowVATAmount =
                                    itemDetail.totalAmount / 100.0f * Constants.VAT_PER
                                payRowVATAmount + itemDetail.totalAmount
                            } else {
                                itemDetail.totalAmount
                            }
                            val itemDetails = Service(
                                itemDetail.serviceCode, itemDetail.englishName,
                                itemDetail.quantity, itemDetail.unitPrice.toDouble(),
                                totalAmountVAT.toDouble(),
                                itemDetail.quantity
                            )
                            itemDetailList.add(itemDetails)
                        }
                    }
                } /*else {
                    itemCount = 1
                    val orderAmount = data.extras!!.getString("orderAmount")
                    val orderAmt = orderAmount?.split("|")
                    bookNumber = orderAmt?.get(0)
                    etEnterAmount.setText(orderAmt?.get(1))
                    btnItemCount.visibility = View.VISIBLE
                    btnItemCount.text = "+1 items"
                }*/
            }
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
        dismissLoadingDialog()
        binding.etEnterAmount.setText("")
        if (type == "PAYBYLINK") {
            SharedData.sharedArrayList = arrayListOf()
        }
    }

    private fun showLoadingDialog(msg: String) {
        if (loadDialog == null) {
            loadDialog = LoadingDialog(this, msg)
        } else {
            loadDialog?.setMessage(msg)
        }
        if (!loadDialog!!.isShowing) {
            loadDialog!!.show()
        }
    }

    private fun dismissLoadingDialog() {
        if (loadDialog != null && loadDialog!!.isShowing) {
            loadDialog!!.dismiss()
        }
    }

    override fun closeLoader() {
        runOnUiThread {
            dismissLoadingDialog()
            finish()
        }
    }
}