package com.payment.payrowapp.invoicerecall

import android.app.Activity
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.payment.payrowapp.R
import com.payment.payrowapp.databinding.ActivityInvoiceReceiptBinding
import com.payment.payrowapp.databinding.ActivitySelectOptionBinding
import com.payment.payrowapp.dataclass.EnquiryRequestClass
import com.payment.payrowapp.dataclass.PaymentInvoiceRequest
import com.payment.payrowapp.generateqrcode.ECommVOIDRFActivity
import com.payment.payrowapp.generateqrcode.GenerateQRCodeReceiptActivity
import com.payment.payrowapp.generateqrcode.GenerateQRCodeViewModel
import com.payment.payrowapp.generateqrcode.GenerateQRCodeViewModelFactory
import com.payment.payrowapp.newpayment.CardReceiptActivity
import com.payment.payrowapp.newpayment.PaymentSuccessfulActivity
import com.payment.payrowapp.observeOnce
import com.payment.payrowapp.refundandreversal.VoidRFReceiptActivity
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.qrcodescan.OrderQRScanActivity
import com.payment.payrowapp.sunmipay.LoadingDialog
import com.payment.payrowapp.utils.*
import java.text.SimpleDateFormat
import java.util.*

class SelectOptionForInvoiceRecallActivity : BaseActivity(), LoaderCallback {
    var ring: MediaPlayer? = null
    var type = 0
    val months = arrayOf(
        "Jan",
        "Feb",
        "Mar",
        "Apr",
        "May",
        "Jun",
        "Jul",
        "Aug",
        "Sep",
        "Oct",
        "Nov",
        "Dec"
    )
    lateinit var formatedFromDate: String
    lateinit var formatedToDate: String
    private var loadDialog: LoadingDialog? = null
    private lateinit var generateQRCodeViewModel: GenerateQRCodeViewModel

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private lateinit var binding: ActivitySelectOptionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //  setContentView(R.layout.activity_select_option)
        binding = ActivitySelectOptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        // setSupportActionBar(myToolbar)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        supportActionBar?.title = "Payment Status"

        ring = MediaPlayer.create(this, R.raw.sound_button_click)

        val imgSelect: Drawable = baseContext.resources.getDrawable(R.drawable.ic_vector_2x, null)
        val imgDeSelect: Drawable =
            baseContext.resources.getDrawable(R.drawable.ic_ellipse_2x, null)

        generateQRCodeViewModel =
            ViewModelProvider(
                this,
                GenerateQRCodeViewModelFactory(this)
            ).get(GenerateQRCodeViewModel::class.java)

        binding.btnByTransId.setOnClickListener {
            ring?.start()
            type = 0
            binding.clTransactionId.visibility = View.VISIBLE
            binding.cardFromToDate.visibility = View.GONE

            binding.btnByDate.setCompoundDrawablesWithIntrinsicBounds(null, null, imgDeSelect, null)
            binding.btnByTransId.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                imgSelect,
                null
            )
            // startActivity(Intent(this, EnterTransactionIdActivity::class.java))
        }
        binding.btnByDate.setOnClickListener {
            ring?.start()
            type = 1
            binding.btnSearchTransId.setBackgroundResource(R.drawable.button_round_dark_gray_bg_fill)
            binding.btnSearchTransId.isEnabled = true
            binding.clTransactionId.visibility = View.GONE
            binding.cardFromToDate.visibility = View.VISIBLE

            binding.btnByDate.setCompoundDrawablesWithIntrinsicBounds(null, null, imgSelect, null)
            binding.btnByTransId.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                imgDeSelect,
                null
            )
            //  startActivity(Intent(this, PaymentInvoiceRecallActivity::class.java))
        }

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH) + 1
        val day = c.get(Calendar.DAY_OF_MONTH)
        var currentMonth = c.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
        var currentDay =
            c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault())

        formatedFromDate =
            ContextUtils.formatDatetoformat(year, month - 1, day)
        formatedToDate =
            ContextUtils.formatDatetoformat(year, month - 1, day)

        binding.tvFromDate.text = "$day $currentMonth"
        binding.tvToDate.text = "$day $currentMonth"

        // set current day
        val calendar = Calendar.getInstance().time
        val sdf =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val currentDate = sdf.format(calendar)
        val date = sdf.parse(currentDate)
        val dayOfTheWeek = DateFormat.format("EEEE", date) as String // Thursday


        binding.tvFromDay.text = dayOfTheWeek
        binding.tvToDay.text = dayOfTheWeek
        var fromDate = "$year-$month-$day"
        var toDate = "$year-$month-$day"
        var fromDateClicked = false
        var toDateClicked = false

        val dpd = DatePickerDialog(
            this, { _, year, monthOfYear, dayOfMonth ->
                val sdfDate =
                    SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val simpleDateFormat = SimpleDateFormat("EEEE")
                if (fromDateClicked) {
                    formatedFromDate =
                        ContextUtils.formatDatetoformat(year, monthOfYear, dayOfMonth)
                    binding.tvFromDate.text = "" + dayOfMonth + " " + months[monthOfYear]
                    val month = monthOfYear + 1
                    fromDate = "$year-$month-$dayOfMonth"
                    val fromSelDate = sdfDate.parse("$dayOfMonth-$month-$year")
                    binding.tvFromDay.text = simpleDateFormat.format(fromSelDate)
                } else if (toDateClicked) {
                    formatedToDate = ContextUtils.formatDatetoformat(year, monthOfYear, dayOfMonth)
                    val month = monthOfYear + 1
                    binding.tvToDate.text = "" + dayOfMonth + " " + months[monthOfYear]
                    toDate = "$year-$month-$dayOfMonth"
                    val toSelDate = sdfDate.parse("$dayOfMonth-$month-$year")
                    binding.tvToDay.text = simpleDateFormat.format(toSelDate)
                }
            },
            year,
            c.get(Calendar.MONTH),
            day
        )

        binding.clFromDate.setOnClickListener {
            ring?.start()

            c.set(year, month - 4, day)
            fromDateClicked = true
            toDateClicked = false
            dpd.datePicker.minDate = c.timeInMillis
            dpd.datePicker.maxDate = System.currentTimeMillis()
            dpd.show()
        }
        binding.clToDate.setOnClickListener {
            ring?.start()
            c.set(year, month - 4, day)
            toDateClicked = true
            fromDateClicked = false
            dpd.datePicker.minDate = c.timeInMillis
            dpd.datePicker.maxDate = System.currentTimeMillis()
            dpd.show()
        }

        // enter transaction flow
        var enterTransactionIdViewModel =
            ViewModelProvider(
                this,
                EnterTransactionIdViewModelFactory(this)
            ).get(EnterTransactionIdViewModel::class.java)

        binding.qrOrderImg.setOnClickListener {
            val intent = Intent(this, OrderQRScanActivity::class.java)
            launchQROrderScan.launch(intent)
        }

        binding.etEnterTransactionNo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if (binding.etEnterTransactionNo.text.isNotEmpty()) {
                    binding.btnSearchTransId.setBackgroundResource(R.drawable.button_round_dark_gray_bg_fill)
                    binding.btnSearchTransId.isEnabled = true
                } else {
                    binding.btnSearchTransId.setBackgroundResource(R.drawable.button_round_gray_bg_fill)
                    binding.btnSearchTransId.isEnabled = false
                }

            }
        })

        binding.btnSearchTransId.setOnClickListener {
            ring?.start()

            if (type == 1) {
                val inputFormatter1 = SimpleDateFormat("dd/MM/yyyy")
                val date1 = inputFormatter1.parse(formatedFromDate)
                val date2 = inputFormatter1.parse(formatedToDate)
                val daysDifference: String =
                    UtilityClass.CompareTwoDatesCount(
                        "dd/MM/yyyy",
                        formatedFromDate,
                        formatedToDate
                    )

                if (date1 == date2 || date2.after(date1)) {
                    if (daysDifference.toInt() > 7) {
                        showToast("Date difference within 7 days")
                    } else {
                        val bundle = Bundle()
                        bundle.putString("Type", "InvoiceRecall")
                        bundle.putString(Constants.FROM, fromDate)
                        bundle.putString(Constants.TO, toDate)
                        bundle.putString(Constants.FROM_DATE, binding.tvFromDate.text.toString())
                        bundle.putString(Constants.TO_DATE, binding.tvToDate.text.toString())
                        startActivity(
                            Intent(
                                this,
                                InvoicesListActivity::class.java
                            ).putExtras(bundle)
                        )
                    }
                } else {
                    showToast("Start date should not exceed End date")
                }
            } else if (type == 0) {
                //enter transaction flow
                val progressDialog = ProgressDialog(this)
                progressDialog.setMessage(getString(R.string.please_wait))
                progressDialog.show()
                if (validateInputFields()) {
                    ring?.start()

                    val paymentInvoiceRequest = PaymentInvoiceRequest(
                        "",
                        "",
                        "",
                        binding.etEnterTransactionNo.text.toString()
                    ) // 73054
                    enterTransactionIdViewModel.getInvoiceRecall(paymentInvoiceRequest,this)
                    enterTransactionIdViewModel.getData()
                        .observeOnce(this@SelectOptionForInvoiceRecallActivity) {
                            progressDialog.cancel()
                            if (it.data.isNotEmpty()) {
                                when (it.data[0].channel) {
                                    "Paybylink", "generateQR" -> {

                                        val sendBundle = Bundle()
                                        sendBundle.putString(
                                            Constants.DATE,
                                            it.data.get(0).paymentDate.substring(0, 10)
                                        )
                                        sendBundle.putString(
                                            "Time",
                                            it.data.get(0).paymentDate.substring(11, 19)
                                        )
                                        sendBundle.putString("Amount", it.data.get(0).totalAmount)
                                        sendBundle.putString(
                                            "orderNumber",
                                            it.data.get(0).orderNumber
                                        )
                                        sendBundle.putString(
                                            "status",
                                            it.data.get(0).checkoutStatus
                                        )
                                        sendBundle.putString(
                                            "cardNumber",
                                            it.data.get(0).cardNumber
                                        )
                                        sendBundle.putString("cardBrand", it.data.get(0).cardBrand)
                                        sendBundle.putString("totalAmount", it.data.get(0).amount)
                                        sendBundle.putString("type", "invoiceRecall")
                                        sendBundle.putString("channel", it.data[0].channel)
                                        sendBundle.putString("auth", it.data[0].auth)

                                        it.data[0].vatAmount?.let { it1 ->
                                            sendBundle.putFloat(
                                                "payRowVATAmount",
                                                it1
                                            )
                                        }
                                        it.data[0].vatStatus?.let { it1 ->
                                            sendBundle.putBoolean(
                                                "payRowVATStatus",
                                                it1
                                            )
                                        }

                                        if (it.data.get(0).recordType == "Refund Order") {
                                            sendBundle.putString("mode", "Refund")
                                            startActivity(
                                                Intent(
                                                    this,
                                                    ECommVOIDRFActivity::class.java
                                                ).putExtras(sendBundle)
                                            )
                                        } else if (it.data.get(0).recordType == "Voided") {
                                            sendBundle.putString("mode", "Void")
                                            startActivity(
                                                Intent(
                                                    this,
                                                    ECommVOIDRFActivity::class.java
                                                ).putExtras(sendBundle)
                                            )
                                        } else {
                                            if (it.data.get(0).inquiryStatus != true && (it.data.get(
                                                    0
                                                ).checkoutStatus.equals(
                                                    "NOT APPROVED",
                                                    ignoreCase = true
                                                ) || it.data.get(0).checkoutStatus.equals(
                                                    "PRESENTED",
                                                    ignoreCase = true
                                                ) || it.data.get(0).checkoutStatus.equals(
                                                    "DENIED BY RISK",
                                                    ignoreCase = true
                                                ) || it.data.get(0).checkoutStatus.equals(
                                                    "HOST TIMEOUT",
                                                    ignoreCase = true
                                                ) || it.data.get(0).checkoutStatus.equals(
                                                    "CLOSED",
                                                    ignoreCase = true
                                                ) || it.data.get(0).checkoutStatus.equals(
                                                    "CANCELED",
                                                    ignoreCase = true
                                                ) || it.data.get(0).checkoutStatus.equals(
                                                    "NOT CAPTURED",
                                                    ignoreCase = true
                                                ))
                                            ) {
                                                getEnquiryStatus(it.data.get(0).orderNumber)
                                            } else {
                                                startActivity(
                                                    Intent(
                                                        this,
                                                        GenerateQRCodeReceiptActivity::class.java
                                                    ).putExtras(sendBundle)
                                                )
                                            }
                                        }
                                    }
                                    Constants.CASH -> {

                                        val sendBundle = Bundle()
                                        sendBundle.putString(
                                            Constants.DATE,
                                            it.data.get(0).paymentDate.substring(0, 10)
                                        )
                                        sendBundle.putString(
                                            "Time",
                                            it.data.get(0).paymentDate.substring(11, 19)
                                        )
                                        sendBundle.putString(
                                            "status",
                                            it.data.get(0).checkoutStatus
                                        )
                                        sendBundle.putString(
                                            "CashReceived",
                                            it.data.get(0).cashReceived
                                        )
                                        sendBundle.putString("Balance", it.data.get(0).balance)
                                        sendBundle.putString("Amount", it.data.get(0).totalAmount)
                                        //  sendBundle.putString(Constants.DATE, itemsList.paymentDate.substring(0, 10))
                                        sendBundle.putString(
                                            "InvoiceNo",
                                            it.data.get(0).orderNumber
                                        )

                                        sendBundle.putString(
                                            "TotalAmount",
                                            ContextUtils.splitDecimal(it.data.get(0).amount.toFloat())
                                        )

                                        it.data[0].vatAmount?.let { it1 ->
                                            sendBundle.putFloat("payRowVATAmount", it1)
                                        }
                                        it.data[0].vatStatus?.let { it1 ->
                                            sendBundle.putBoolean("payRowVATStatus", it1)
                                        }
                                        sendBundle.putString("type", "invoiceRecall")

                                        sendBundle.putString("receiptNo", it.data.get(0).receiptNo)
                                        startActivity(
                                            Intent(
                                                this,
                                                PaymentSuccessfulActivity::class.java
                                            ).putExtras(sendBundle)
                                        )
                                    }
                                    Constants.CARD -> {

                                        val sharedPreferenceUtil = SharedPreferenceUtil(this)
                                        val bundle = Bundle()
                                        bundle.putString("INVOICENO", it.data.get(0).orderNumber)

                                        bundle.putString(
                                            Constants.DATE,
                                            it.data.get(0).paymentDate.substring(0, 10)
                                        )
                                        bundle.putString(
                                            "Time",
                                            it.data.get(0).paymentDate.substring(11, 19)
                                        )
                                        //  bundle.putString(Constants.TRANSACTION_TYPE, itemsList.channel)
                                        bundle.putString("CARDNO", it.data.get(0).cardNumber)
                                        bundle.putString("hostRefNO", it.data.get(0).hostReference)
                                        bundle.putString("status", it.data.get(0).checkoutStatus)
                                        // bundle.putString("Amount", itemsList.totalAmount)
                                        bundle.putString("authCode", it.data.get(0).authorizationId)
                                        bundle.putString("type", "invoiceRecall")
                                        bundle.putString("cardType", it.data.get(0).cardType)
                                        bundle.putString("CardBrand", it.data.get(0).cardBrand)

                                        bundle.putString(
                                            "payRowDigitFee",
                                            it.data.get(0).secondaryCharges
                                        )

                                        if (it.data[0].PartialApprovedAmount != null) {
                                            bundle.putString(
                                                "totalAmount",
                                                it.data.get(0).PartialApprovedAmount
                                            )
                                            //   sharedPreferenceUtil.setAmount(it.data.get(0).PartialApprovedAmount)
                                        } else {
                                            bundle.putString("totalAmount", it.data.get(0).amount)
                                            //   sharedPreferenceUtil.setAmount(it.data.get(0).amount)
                                        }

                                        it.data[0].vatAmount?.let { it1 ->
                                            bundle.putFloat("payRowVATAmount", it1)
                                        }
                                        it.data[0].vatStatus?.let { it1 ->
                                            bundle.putBoolean("payRowVATStatus", it1)
                                        }

                                        bundle.putString(
                                            "panSequenceNo",
                                            it.data[0].cardsequencenumber
                                        )

                                        if (it.data.get(0).recordType == "Refund Order" || it.data.get(
                                                0
                                            ).recordType == "Refund" || it.data.get(0).recordType == "Void"
                                        ) {
                                            bundle.putString("mode", it.data.get(0).recordType)

                                            if (it.data.get(0).checkoutStatus == "NOT REFUNDED" || it.data.get(
                                                    0
                                                ).checkoutStatus == "NOT VOIDED"
                                            ) {
                                                val responseMessage =
                                                    ContextUtils.responseMessage(it.data.get(0).responseCode)
                                                bundle.putInt("responseMessage", responseMessage!!)
                                            }
                                            startActivity(
                                                Intent(
                                                    this,
                                                    VoidRFReceiptActivity::class.java
                                                ).putExtras(bundle)
                                            )
                                        } else {
                                            sharedPreferenceUtil.setAID(it.data.get(0).AID)
                                            sharedPreferenceUtil.setAC(it.data.get(0).AC)
                                            sharedPreferenceUtil.setACInfo(it.data.get(0).AC_INFO)
                                            sharedPreferenceUtil.setTVR(it.data.get(0).TVR)
                                            sharedPreferenceUtil.setTransactionType(it.data.get(0).TRANSACTION_TYPE)

                                            if (it.data[0].vatAmount != null) {
                                                val serviceCharges =
                                                    it.data.get(0).amount.toFloat() - it.data.get(0).totalServicesAmount?.toFloat()!! - it.data.get(
                                                        0
                                                    ).secondaryCharges?.toFloat()!! - it.data[0].vatAmount!!
                                                bundle.putString(
                                                    "surcharges",
                                                    ContextUtils.splitDecimal(serviceCharges)
                                                )
                                            } else {
                                                val serviceCharges =
                                                    it.data.get(0).amount.toFloat() - it.data.get(0).totalServicesAmount?.toFloat()!! - it.data.get(
                                                        0
                                                    ).secondaryCharges?.toFloat()!!
                                                bundle.putString(
                                                    "surcharges",
                                                    ContextUtils.splitDecimal(serviceCharges)
                                                )
                                            }


                                            bundle.putBoolean(
                                                "SignatureStatus",
                                                it.data.get(0).SignatureStatus
                                            )
                                            bundle.putBoolean(
                                                "isPinBlock",
                                                it.data.get(0).PinBlockStatus
                                            )
                                            if (it.data.get(0).responseCode != null && it.data.get(0).checkoutStatus == "NOT CAPTURED") {
                                                val responseMessage =
                                                    ContextUtils.responseMessage(it.data.get(0).responseCode)
                                                bundle.putInt("responseMessage", responseMessage!!)
                                            }

                                            startActivity(
                                                Intent(
                                                    this,
                                                    CardReceiptActivity::class.java
                                                ).putExtras(
                                                    bundle
                                                )
                                            )
                                        }
                                    }
                                }
                            } else {
                                showToast(getString(R.string.data_not_available))
                            }
                        }
                } else {
                    progressDialog.cancel()
                    Toast.makeText(
                        this,
                        getString(R.string.please_transaction_id),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private val launchQROrderScan = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            binding.etEnterTransactionNo.setText(result.data?.getStringExtra("orderNumber"))
        }
    }

    private fun validateInputFields(): Boolean {
        if (binding.etEnterTransactionNo.text.isNotEmpty()) {
            return true
        }
        return false
    }

    private fun showLoadingDialog(msg: String) {
        if (loadDialog == null) {
            loadDialog = LoadingDialog(this, msg)
        } else {
            loadDialog?.setMessage(msg)
        }
        if (!(isFinishing || isDestroyed) && loadDialog?.isShowing == false) {
            loadDialog?.show()
        }
    }

    private fun dismissLoadingDialog() {
        if (!(isFinishing || isDestroyed)) {
            loadDialog?.takeIf { it.isShowing }?.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        loadDialog?.dismiss()
        loadDialog = null
    }

    private fun getEnquiryStatus(orderNumber: String) {
        runOnUiThread {
            showLoadingDialog("Please wait..")
            val sharedPreferenceUtil =
                SharedPreferenceUtil(this)
            val enquiryRequestClass =
                EnquiryRequestClass(orderNumber, sharedPreferenceUtil.getGatewayMerchantID())
            generateQRCodeViewModel.getEnquiryResponse(enquiryRequestClass) { result ->
                dismissLoadingDialog()
                showToast(getString(R.string.something_went_wrong_try))
            }
            generateQRCodeViewModel.getEnquiryData()
                .observeOnce(this@SelectOptionForInvoiceRecallActivity) {
                    dismissLoadingDialog()
                    if (it.data != null && !it.data?.checkoutStatus.isNullOrEmpty()) {
                        val intent =
                            Intent(
                                this@SelectOptionForInvoiceRecallActivity,
                                GenerateQRCodeReceiptActivity::class.java
                            )

                        intent.putExtra(Constants.DATE, it.data?.paymentDate?.substring(0, 10))
                        intent.putExtra("Time", it.data?.paymentDate?.substring(11, 19))

                        intent.putExtra("orderNumber", orderNumber)
                        intent.putExtra("status", it.data?.checkoutStatus)
                        intent.putExtra("Amount", it.data?.totalAmount)

                        if (!it.data?.cardNumber.isNullOrEmpty()) {
                            intent.putExtra("cardNumber", it.data?.cardNumber)
                        }

                        if (!it.data?.cardBrand.isNullOrEmpty()) {
                            intent.putExtra("cardBrand", it.data?.cardBrand)
                        }

                        it.data?.vatStatus?.let { it1 ->
                            intent.putExtra("payRowVATStatus", it1)
                        }

                        it.data?.vatAmount?.let { vatAmount ->
                            intent.putExtra("payRowVATAmount", vatAmount)
                        }
                        if (it.data?.amount != null) {
                            intent.putExtra("totalAmount", it.data?.amount.toString())
                        }
                        intent.putExtra("channel", it.data?.channel)

                        if (!it.data?.auth.isNullOrEmpty()) {
                            intent.putExtra("auth", it.data?.auth)
                        }

                        startActivity(intent)
                    } else {
                        showToast(getString(R.string.data_not_available))
                    }
                }
        }
    }

    override fun closeLoader() {
        runOnUiThread {
            dismissLoadingDialog()
            finish()
        }
    }
}