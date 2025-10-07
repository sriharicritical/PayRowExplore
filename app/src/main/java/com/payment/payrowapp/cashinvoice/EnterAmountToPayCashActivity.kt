package com.payment.payrowapp.cashinvoice

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.payment.payrowapp.R
import com.payment.payrowapp.crypto.HeaderSignatureUtil
import com.payment.payrowapp.databinding.ActivityEcommVoidrfactivityBinding
import com.payment.payrowapp.databinding.ActivityEnterAmountToPayCashBinding
import com.payment.payrowapp.dataclass.*
import com.payment.payrowapp.newpayment.PaymentSuccessfulActivity
import com.payment.payrowapp.newpayment.StoreItemsActivity
import com.payment.payrowapp.newpayment.StoreItemsRepository
import com.payment.payrowapp.observeOnce
import com.payment.payrowapp.qrcodescan.QRCodeScanActivity
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.BaseActivity
import com.payment.payrowapp.utils.Constants
import com.payment.payrowapp.utils.ContextUtils
import com.payment.payrowapp.dataclass.ItemsCountRequest
import com.payment.payrowapp.dataclass.SharedData
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/*
* This class is used to show the receipt of the cash invoice
* */
class EnterAmountToPayCashActivity : BaseActivity() {

    private var customerBillingPostalCode: String? = null
    private var customerBillingState: String? = null
    private var customerBillingCity: String? = null
    private var customerBillingCountry: String? = null
    private var customerName: String? = null
    private var paymentType = "Cash"
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
   // var ring: MediaPlayer? = null
    private var ringPay: MediaPlayer? = null
    var totalAmount: Float = 0.0F
    var cashReceived: Float = 0.0F
    var balance: Float = 0.0F
    private var itemCount = 0
    private var totalAmountQRCode = "0"
    private var totalAmountVAT: Float = 0.0F
    private var purchaseBreakdown: PurchaseBreakdown? = null
    private var purchaseDetails: PurchaseBreakdownDetails? = null
    private var itemDetailList = ArrayList<ItemDetail>()
    var orderNumber: String? = null
    private lateinit var channel: String
    private var checkoutId: String? = null
    private var bookNumber: String? = null
    private var vatAmount: Float = 0.0F
    private var vatStatus = false
    private lateinit var sharedPreferenceUtil: SharedPreferenceUtil
    /* override fun onStart() {
         super.onStart()
         try {
             EventBus.getDefault().register(this)
         } catch (e: Exception) {
             e.printStackTrace()
         }
     }*/

    private lateinit var binding:ActivityEnterAmountToPayCashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_enter_amount_to_pay_cash)
        binding = ActivityEnterAmountToPayCashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
      //  setSupportActionBar(myToolbar)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Cash Invoice"

       // ring = MediaPlayer.create(this, R.raw.sound_button_click)
        ringPay = MediaPlayer.create(this, R.raw.sound_pay_button)

        val enterAmountToPayCashViewModel =
            ViewModelProvider(
                this,
                EnterAmountToPayCashViewModelFactory(this)
            )[EnterAmountToPayCashViewModel::class.java]


        sharedPreferenceUtil = SharedPreferenceUtil(this)

        if (sharedPreferenceUtil.getScanBarCode() && sharedPreferenceUtil.getCataLogAmount()) {
            binding.btnAddItem.visibility = View.VISIBLE
            binding.etEnterAmount.isEnabled = false

            val marginInDp = 16
            val marginInPx = (marginInDp * resources.displayMetrics.density).toInt()
            val params = binding.btnScanBarCode.layoutParams as ConstraintLayout.LayoutParams
            params.topMargin = marginInPx

            binding.btnScanBarCode.layoutParams = params
            binding.btnScanBarCode.visibility = View.VISIBLE
        } else if (sharedPreferenceUtil.getScanBarCode()) {
            binding.btnScanBarCode.visibility = View.VISIBLE
            binding.etEnterAmount.isEnabled = false
        } else if (sharedPreferenceUtil.getCataLogAmount()) {
            binding.btnAddItem.visibility = View.VISIBLE
            binding.etEnterAmount.isEnabled = false
            /*  val marginInDp = 16
              val marginInPx = (marginInDp * resources.displayMetrics.density).toInt()
              val params = btnScanBarCode.layoutParams as ConstraintLayout.LayoutParams
              params.topMargin = marginInPx

              btnScanBarCode.layoutParams = params
              btnScanBarCode.visibility = View.GONE*/
        }else {
            val itemsCountRequest = ItemsCountRequest(
                sharedPreferenceUtil.getGatewayMerchantID(),
                sharedPreferenceUtil.getMerchantID()
            )
            StoreItemsRepository.getItemCountMutableLiveData(
                this,
                itemsCountRequest,
                sharedPreferenceUtil
            )
            StoreItemsRepository.getItemCountLiveData()
                .observeOnce(this@EnterAmountToPayCashActivity) {
                    if (it?.data != null) {
                        SharedData.selectedItems = ArrayList<Product>()
                      //  itemCount = 1
                        val dataObject = JSONObject(it.data)

                        val posArray = dataObject.optJSONArray("defaultPosServ")//.getJSONObject(0)
                        val posObject = posArray?.optJSONObject(0)
                        if (posObject != null) {
                            itemCount = 1
                            val product = Product(
                                posObject.getString("serviceId"),
                                posObject.getString("shortServiceName"), 1,
                                posObject.getString("unitPrice").toDouble(), 1, 0.0,
                                posObject.getString("serviceType")
                            )
                            SharedData.selectedItems.add(product)
                        }
                    }
                }
        }

        val merchantID =
            sharedPreferenceUtil.getMerchantID()//preference!!.getString(Constants.MERCHANT_ID, "")
        binding.btnInvoiceNumber.text = "MID: $merchantID"
        binding.tvStoreName.text =
            sharedPreferenceUtil.getMerchantName() + sharedPreferenceUtil.getMerchantLastName()
        // val bundle = intent.extras

        binding.etEnterAmount.addTextChangedListener(object : TextWatcher {
            @SuppressLint("SetTextI18n")
            override fun afterTextChanged(p0: Editable?) {

                if (binding.etEnterAmount.text.toString().isNotEmpty()) {
                    totalAmount = binding.etEnterAmount.text.toString().toFloat()
                    // sharedPreferenceUtil.setAmount(etEnterAmount.text.toString())

                    if (sharedPreferenceUtil.getVATCalculator()) {
                        vatStatus = true
                        vatAmount = (totalAmount / 100.0f) * Constants.VAT_PER
                        totalAmountVAT = totalAmount + vatAmount
                        binding.clTotalAmountVAT.visibility = View.VISIBLE
                        // etVATEnterAmount.setText(totalAmountVAT.toString())

                        binding.etVATEnterAmount.setText(ContextUtils.formatWithCommas(totalAmountVAT.toDouble()))
                       /* val splitTOTAmount = totalAmountVAT.toString().split(".").toTypedArray()
                        if (splitTOTAmount[1].length > 2) {

                            binding.etVATEnterAmount.setText(
                                splitTOTAmount[0] + "." + splitTOTAmount[1].substring(0, 2)
                            )
                        } else {
                            if (splitTOTAmount[1].toInt() == 0) {
                                binding.etVATEnterAmount.setText(splitTOTAmount[0])
                            } else {
                                binding.etVATEnterAmount.setText(splitTOTAmount[0] + "." + splitTOTAmount[1])
                            }
                        }*/
                    } else {
                        totalAmountVAT = binding.etEnterAmount.text.toString().toFloat()
                    }

                    binding.btnPay.setBackgroundResource(R.drawable.button_round_dark_gray_bg_fill)
                    val img: Drawable =
                        resources.getDrawable(R.drawable.ic_icon_pay_3_lines_green, null)
                    val img2: Drawable =
                        resources.getDrawable(R.drawable.ic_baseline_arrow_forward_24, null)
                    binding.btnPay.setCompoundDrawablesWithIntrinsicBounds(img, null, img2, null)
                } else {
                    binding.clTotalAmountVAT.visibility = View.GONE
                    binding.etCashReceived.setText("")
                    binding.etCashReceived.hint = "0.0"

                    binding.btnPay.setBackgroundResource(R.drawable.button_gray_before_selection)
                    val img: Drawable =
                        resources.getDrawable(R.drawable.ic_pay_with_3_lines, null)
                    val img2: Drawable =
                        resources.getDrawable(R.drawable.ic_baseline_arrow_forward_24, null)
                    binding.btnPay.setCompoundDrawablesWithIntrinsicBounds(img, null, img2, null)
                }
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

        binding.etCashReceived.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                //To change body of created functions use File | Settings | File Templates.
                /*  if (etVATEnterAmount.text.toString().isNotEmpty()) {
                      totalAmountVAT = etVATEnterAmount.text.toString().toFloat()

                  } else {
                      totalAmountVAT = 0.00F
                  }*/
                cashReceived = if (binding.etCashReceived.text.toString().isNotEmpty()) {
                    binding.etCashReceived.text.toString().toFloat()
                } else {
                    0.00F
                }

                if (totalAmountVAT > 0.00F && cashReceived >= totalAmountVAT) {
                    balance = cashReceived - totalAmountVAT

                    binding.etBalance.setText(ContextUtils.formatWithCommas(balance.toDouble()))

                    /*val splitBalAmount = balance.toString().split(".").toTypedArray()
                    if (splitBalAmount[1].length > 2) {
                        binding.etBalance.text = splitBalAmount[0] + "." + splitBalAmount[1].substring(0, 2)
                    } else {
                        binding.etBalance.text = splitBalAmount[0] + "." + splitBalAmount[1]
                    }*/

                } else {
                    binding.etBalance.text = "0.00"
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //To change body of created functions use File | Settings | File Templates.
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //To change body of created functions use File | Settings | File Templates.
                //var formatter: NumberFormat = DecimalFormat("#,###,###.##")

            }
        })

        binding.btnAddItem.setOnClickListener {
            ringPay?.start()
            val bundle1 = Bundle()
            bundle1.putString("TYPE", "CASHINVOICE")
            startActivityForResult((Intent(this, StoreItemsActivity::class.java)), 2)
        }
        binding.btnScanBarCode.setOnClickListener {
            ringPay?.start()
            startActivityForResult((Intent(this, QRCodeScanActivity::class.java)), 3)
        }
        binding.btnPay.setOnClickListener {
            ringPay?.start()
            if (itemCount > 0) {
                if (binding.etEnterAmount.text.toString().isNotEmpty()) {
                    if (binding.etEnterAmount.text.toString().toFloat() <= 1000000) {
                        if (binding.etEnterAmount.text.toString().toFloat() > 0) {
                            /*if (!vatStatus) {
                                totalAmountVAT = etEnterAmount.text.toString().toFloat()
                            }*/
                            if (binding.etCashReceived.text.toString().isNotEmpty()) {
                                if (binding.etBalance.text.toString().toFloat() >= 0) {
                                    if (binding.etCashReceived.text.toString()
                                            .toFloat() >= totalAmountVAT
                                    ) {

                                        val progressDialog = ProgressDialog(this)
                                        progressDialog.setMessage("Please wait...")
                                        progressDialog.show()


                                        if (purchaseBreakdown != null) {
                                            purchaseDetails =
                                                PurchaseBreakdownDetails(itemDetailList)
                                        } else if (bookNumber != null) {
                                            val itemDetail = ItemDetail(
                                                "10000",
                                                "10000",
                                                "Book",
                                                "",
                                                1,
                                                0.0,
                                                0.0
                                            )
                                            itemDetailList.add(itemDetail)
                                            purchaseDetails =
                                                PurchaseBreakdownDetails(itemDetailList)
                                        } else {
                                            if (!sharedPreferenceUtil.getCataLogAmount()) {
                                                SharedData.selectedItems[0].transactionAmount =
                                                    binding.etEnterAmount.text.toString().toDouble()
                                            }
                                            for (itemDetail in SharedData.selectedItems) {
                                                val itemDetails = ItemDetail(
                                                    itemDetail.serviceId,
                                                    itemDetail.serviceId,
                                                    itemDetail.shortServiceName,
                                                    null,
                                                    itemDetail.quantity,
                                                    itemDetail.transactionAmount,
                                                    0.0
                                                )
                                                itemDetailList.add(itemDetails)
                                            }
                                            purchaseDetails =
                                                PurchaseBreakdownDetails(itemDetailList)
                                        }

                                        val calendar = Calendar.getInstance().time
                                        val sdf =
                                            SimpleDateFormat(
                                                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                                                Locale.getDefault()
                                            )
                                        val currentDate = sdf.format(calendar)


                                        if (customerName.isNullOrEmpty()) {
                                            customerName = sharedPreferenceUtil.getMerchantName()
                                        }

                                        if (customerBillingCity.isNullOrEmpty()) {
                                            customerBillingCity = sharedPreferenceUtil.getCity()
                                        }

                                        if (customerBillingState.isNullOrEmpty()) {
                                            customerBillingState = sharedPreferenceUtil.getCity()
                                        }

                                        if (customerBillingCountry.isNullOrEmpty()) {
                                            customerBillingCountry =
                                                sharedPreferenceUtil.getCountry()
                                        }

                                        if (customerBillingPostalCode.isNullOrEmpty()) {
                                            customerBillingPostalCode =
                                                sharedPreferenceUtil.getBOBox()
                                        }

                                        if (orderNumber.isNullOrEmpty()) {

                                            orderNumber = ContextUtils.randomValue()
                                                .toString() + ContextUtils.getRandomLastValue()
                                        }

                                        if (storeId.isNullOrEmpty()) {
                                            storeId = sharedPreferenceUtil.getStoreID()
                                        }

                                        if (mainMerchantId.isNullOrEmpty()) {
                                            mainMerchantId = sharedPreferenceUtil.getMerchantID()
                                        }

                                        if (receiptNo.isNullOrEmpty()) {
                                            receiptNo =  ContextUtils.randomValue().toString()//(100..999).shuffled().last().toString()
                                        }

                                        if (posType.isNullOrEmpty()) {
                                            posType = sharedPreferenceUtil.getUserRole()
                                        }

                                        if (posId.isNullOrEmpty()) {
                                            posId = sharedPreferenceUtil.getUserID()
                                        }

                                        if (distributorId.isNullOrEmpty()) {
                                            distributorId = sharedPreferenceUtil.getDistributorID()
                                        }

                                        if (userId.isNullOrEmpty()) {
                                            userId = sharedPreferenceUtil.getUserID()
                                        }

                                        if (merchantEmail.isNullOrEmpty()) {
                                            merchantEmail = sharedPreferenceUtil.getEmailID()
                                        }

                                        if (customerPhone.isNullOrEmpty()) {
                                            customerPhone =
                                                sharedPreferenceUtil.getMerchantMobileNumber()
                                        }

                                        if (customerEmail.isNullOrEmpty()) {
                                            customerEmail = sharedPreferenceUtil.getEmailID()
                                        }

                                        if (trnNo.isNullOrEmpty()) {
                                            trnNo = ContextUtils.randomValue().toString()
                                        }

                                        if (payrowInvoiceNo.isNullOrEmpty()) {
                                            payrowInvoiceNo = ContextUtils.randomValue().toString()
                                        }

                                        channel = if (paymentType == "scan qr") {
                                            "Third Party QRCode"
                                        } else {
                                            Constants.CASH
                                        }

                                        val orderRequest = OrderRequest(
                                            storeId!!,
                                            orderNumber!!,
                                            Constants.CASH,
                                            mainMerchantId!!,
                                            posId!!,
                                            posType!!,
                                            customerEmail!!,
                                            customerPhone!!,
                                            distributorId!!,
                                            purchaseDetails,
                                            userId!!,
                                            currentDate,
                                            vatAmount,
                                            binding.etEnterAmount.text.toString().toFloat(),
                                            sharedPreferenceUtil.getMerchantEmail(),
                                            sharedPreferenceUtil.getMerchantPhone(),
                                            trnNo,
                                            receiptNo,
                                            ContextUtils.randomValue().toString(),
                                            payrowInvoiceNo,
                                            balance,
                                            binding.etCashReceived.text.toString().toInt(),
                                            null,
                                            null,
                                            null,
                                            "CAPTURED",
                                            customerName,
                                            sharedPreferenceUtil.getMerchantEmail(),
                                            sharedPreferenceUtil.getMerchantPhone(),
                                            customerBillingCity,
                                            customerBillingState,
                                            customerBillingCountry,
                                            customerBillingPostalCode,
                                            checkoutId,
                                            checkoutId,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            "CashInvoice",
                                            null,
                                            null,
                                            null,
                                            null,
                                            sharedPreferenceUtil.getTerminalID(),
                                            customerEmail,
                                            customerPhone,
                                            sharedPreferenceUtil.getBussinessId(),
                                            null,
                                            null,
                                            "CAPTURED",
                                            totalAmountVAT,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            sharedPreferenceUtil.getReportID(),
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            HeaderSignatureUtil.getDeviceSN(),
                                            null,
                                            vatStatus,
                                            vatAmount, null, null
                                        )
                                        enterAmountToPayCashViewModel.addOrder(this, orderRequest)
                                        enterAmountToPayCashViewModel.getOrderData()
                                            .observeOnce(this@EnterAmountToPayCashActivity) {
                                                progressDialog.cancel()
                                                try {
                                                    //   it.data.checkoutId = checkoutId!!
                                                    if (paymentType == "scan qr") {
                                                        enterAmountToPayCashViewModel.postQR(
                                                            this,
                                                            it,
                                                            bankTransferURL!!
                                                        )
                                                    }

                                                    Toast.makeText(
                                                        this,
                                                        "Order added successfully!",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                    val gson = Gson()
                                                    Log.i("Order", "response1->" + gson.toJson(it))
                                                    val orderResponse: OrderResponse = it
                                                    /* editor.putString(
                                                         Constants.INVOICE_NUMBER,
                                                         orderResponse.data.invoiceNumber
                                                     )
                                                     editor.apply()*/
                                                    //EventBus.getDefault().postSticky(orderResponse)
                                                    val bundle = Bundle()
                                                    bundle.putString("TYPE", "CASHINVOICE")
                                                    bundle.putString("InvoiceNo", orderNumber)
                                                    bundle.putString(
                                                        "CashReceived",
                                                        binding.etCashReceived.text.toString()
                                                    )
                                                    bundle.putString(
                                                        "Amount",
                                                        binding.etEnterAmount.text.toString()
                                                    )
                                                    bundle.putString("Balance", balance.toString())
                                                    bundle.putBoolean("payRowVATStatus", vatStatus)
                                                    bundle.putFloat("payRowVATAmount", vatAmount)
                                                    bundle.putString("receiptNo",receiptNo)
                                                    bundle.putString(
                                                        "TotalAmount",
                                                        totalAmountVAT.toString()
                                                    )

                                                    itemCount = 0
                                                    binding.btnItemCountCash.visibility = View.GONE
                                                    SharedData.selectedItems = ArrayList<Product>()
                                                    startActivity(
                                                        Intent(
                                                            this,
                                                            PaymentSuccessfulActivity::class.java
                                                        ).putExtras(bundle)
                                                    )
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            }
                                    } else {
                                        Toast.makeText(
                                            this,
                                            R.string.enter_amount_should_not_be,
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                    }
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Balance amount should be greater than or equal to zero",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                            } else {
                                Toast.makeText(
                                    this,
                                    R.string.please_enter_received_amount,
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        } else {
                            showToast("Please enter amount is greater than zero")
                        }
                    } else {
                        showToast("Kindly enter lesser amount.")
                    }
                } else {
                    Toast.makeText(
                        this,
                        R.string.please_enter_the_amount_to_proceed,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            } else {
               // showToast("Please select the items to proceed")
                if (sharedPreferenceUtil.getCataLogAmount() || (sharedPreferenceUtil.getScanBarCode() && sharedPreferenceUtil.getCataLogAmount())) {
                    showToast(getString(R.string.please_select_item_to_proceed))
                } else if (sharedPreferenceUtil.getScanBarCode()) {
                    showToast(getString(R.string.kinldy_scan_the_qr_fech_the_amount))
                } else {
                    showToast(getString(R.string.default_items_not_available))
                    finish()
                }
            }

        }

    }

    /*@Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(merchantId: MerchantId) {
        Log.i("Order", "event fired Enter Amount screen->$merchantId")
    }*/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            itemCount = data.getIntExtra("itemLength", 0)
            totalAmountQRCode = data.getStringExtra("totalAmount").toString()
            paymentType = "Cash"
            if (itemCount > 0) {
                binding.btnItemCountCash.visibility = View.VISIBLE
                binding.btnItemCountCash.text =
                    "+" + data.getIntExtra("itemLength", 0).toString() + " items"
            }

            if (resultCode == 2) {
                if (itemCount > 0) {
                    //   if (sharedPreferenceUtil.getCataLogAmount()) {
                    binding.etEnterAmount.isEnabled = false
                    val tranAmount = data.getStringExtra("transactionAmount").toString()
                    binding.etEnterAmount.setText(ContextUtils.formatWithCommas(tranAmount.toDouble()))
                    //  }
                }
            } else if (resultCode == 3) {
                if (itemCount > 0) {

                    if (!sharedPreferenceUtil.getCataLogAmount()) {
                        SharedData.selectedItems = arrayListOf()
                    }

                    paymentType = "scan qr"
                    bankTransferURL = data.extras!!.getString("MerchantBankURL")
                    binding.etEnterAmount.setText(ContextUtils.formatWithCommas(totalAmountQRCode.toDouble()))
                    purchaseBreakdown =
                        data.extras!!.get("purchaseBreakDown")!! as PurchaseBreakdown
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
                    checkoutId = data.extras!!.getString("checkoutId")

                    if (purchaseBreakdown != null && purchaseBreakdown!!.service.size > 0) {
                        for (itemDetail in purchaseBreakdown!!.service) {
                            val itemDetails = ItemDetail(
                                itemDetail.serviceCode,
                                itemDetail.serviceCode,
                                itemDetail.englishName,
                                itemDetail.arabicName,
                                itemDetail.quantity,
                                itemDetail.transactionAmount.toDouble(),
                                itemDetail.totalAmount.toDouble()
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
                    btnItemCountCash.visibility = View.VISIBLE
                    btnItemCountCash.text = "+1 items"
                }*/
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.etEnterAmount.setText("")
        binding.etCashReceived.setText("")
    }
}
