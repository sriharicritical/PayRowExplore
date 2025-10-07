package com.payment.payrowapp.sunmipay

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.payment.payrowapp.R
import com.payment.payrowapp.dashboard.DashboardActivity
import com.payment.payrowapp.databinding.ActivityMainBinding
import com.payment.payrowapp.databinding.ActivityProductDetailsBinding
import com.payment.payrowapp.dataclass.*
import com.payment.payrowapp.mastercloud.CPOCConnectActivity
import com.payment.payrowapp.newpayment.TinyDB
import com.payment.payrowapp.observeOnce
import com.payment.payrowapp.product.ProductViewModel
import com.payment.payrowapp.product.ProductViewModelFactory
import com.payment.payrowapp.retrofit.ApiClient
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.BaseActivity
import com.payment.payrowapp.utils.Constants
import com.payment.payrowapp.utils.ContextUtils
import com.payment.payrowapp.utils.ContextUtils.Companion.getSelecteItems
import java.text.SimpleDateFormat
import java.util.*

class ProductActivity : BaseActivity() {
    private var ringPay: MediaPlayer? = null
    private var surCharge: Boolean = false
    private var purchaseBreakdown: PurchaseBreakdown? = null

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
    var orderNumber: String? = null
    var paymentType: String? = null

    private var customerBillingPostalCode: String? = null
    private var customerBillingState: String? = null
    private var customerBillingCity: String? = null
    private var customerBillingCountry: String? = null
    private var customerName: String? = null
    private var checkoutId: String? = null
    private var itemDetailList = ArrayList<ItemDetail>()
    private var purchaseDetails: PurchaseBreakdownDetails? = null
    private var payRowVATAmount: Float = 0.0f
    private var payRowVATStatus = false
    private var serviceList = ArrayList<Product>()
    private var feeResponseData: FeeResponseData? = null
    private var payRowSecondaryCharges: Float = 0.0f

    private var loadDialog: LoadingDialog? = null
    private lateinit var activityProductDetailsBinding: ActivityProductDetailsBinding

    @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityProductDetailsBinding = ActivityProductDetailsBinding.inflate(layoutInflater)
        setContentView(activityProductDetailsBinding.root)
       // setContentView(R.layout.activity_product_details)

        val bundle = intent.extras
        val amount = bundle!!.getString("AMOUNT")
        val paymentType = bundle.getString("PAYMENT TYPE")

        ringPay = MediaPlayer.create(this, R.raw.sound_pay_button)

        val sharedPreferenceUtil = SharedPreferenceUtil(this)

        if (bundle.get("OrderNumber") != null) {
            orderNumber = bundle.getString("OrderNumber")
            //  sharedPreferenceUtil.setOrderNum(orderNumber)
        }

        if (bundle.get("bookNumber") != null) {
            val bookNumber = bundle.getString("bookNumber")
            // tvOrderNo.text = bookNumber
        }

        if (bundle.get("purchaseBreakDown") != null) {
            purchaseBreakdown = bundle.get("purchaseBreakDown") as PurchaseBreakdown

            // orderNumber = bundle.getString("OrderNumber")
            // tvOrderNo.text = orderNumber
            storeId = bundle.getString("storeId")
            distributorId = bundle.getString("distributorId")
            userId = bundle.getString("userId")
            merchantEmail = bundle.getString("merchantEmail")
            receiptNo = bundle.getString("receiptNo")
            trnNo = bundle.getString("trnNo")
            payrowInvoiceNo = bundle.getString("payrowInvoiceNo")
            posType = bundle.getString("posType")

            customerPhone = bundle.getString("customerPhone")
            customerEmail = bundle.getString("customerEmail")
            mainMerchantId = bundle.getString("mainMerchantId")
            posId = bundle.getString("posId")
            bankTransferURL = bundle.getString("MerchantBankURL")

            customerName = bundle.getString("customerName")
            customerBillingCountry = bundle.getString("customerBillingCountry")
            customerBillingCity = bundle.getString("customerBillingCity")
            customerBillingState = bundle.getString("customerBillingState")
            customerBillingPostalCode = bundle.getString("customerBillingPostalCode")
            checkoutId = bundle.getString("checkoutId")

            sharedPreferenceUtil.setQRStoreID(storeId)
            sharedPreferenceUtil.setQRDistID(distributorId)
            sharedPreferenceUtil.setQRUser(userId)
            sharedPreferenceUtil.setQRMerEmail(merchantEmail)
            sharedPreferenceUtil.setQRReNo(receiptNo)
            sharedPreferenceUtil.setQRTrNo(trnNo)
            sharedPreferenceUtil.setQRPInvoiceNo(payrowInvoiceNo)
            sharedPreferenceUtil.setQRPosType(posType)
            sharedPreferenceUtil.setQRTransURL(bankTransferURL)
            sharedPreferenceUtil.setQRPosID(posId)
            sharedPreferenceUtil.setQRCheckID(checkoutId)

            sharedPreferenceUtil.setQRCPhone(customerPhone)
            sharedPreferenceUtil.setQRCEmail(customerEmail)

            sharedPreferenceUtil.setQRCName(customerName)
            sharedPreferenceUtil.setQRCCountry(customerBillingCountry)
            sharedPreferenceUtil.setQRCCity(customerBillingCity)
            sharedPreferenceUtil.setQRCState(customerBillingState)
            sharedPreferenceUtil.setQRCPCode(customerBillingPostalCode)
        }

        // tvTOTAmount.text = "$amount AED"


        val amountPayRowCharge = amount!!.toFloat() //+ payRowCharge.toFloat()
        val amountVal = if (sharedPreferenceUtil.getVATCalculator()) {
            activityProductDetailsBinding.tvTOTAmountLabel.text = getString(R.string.total_inc_vat)
            payRowVATStatus = true
            payRowVATAmount = (amountPayRowCharge / 100.0f) * Constants.VAT_PER
            amountPayRowCharge + payRowVATAmount
        } else {
            payRowVATStatus = false
            amountPayRowCharge
        }

        val splitString = amountVal.toString().split(".").toTypedArray()

        var spiltString3: String? = null
        if (splitString[1].length > 2) {
            spiltString3 = splitString[0] + "." + splitString[1].substring(
                0,
                2
            )
            /*  tvTOTAmountVAT.text =
                  splitString[0] + "." + splitString[1].substring(
                      0,
                      2
                  ) + " AED"*/
        } else {

            spiltString3 = if (splitString[1].toInt() == 0) {
                splitString[0]
                /*  tvTOTAmountVAT.text =
                              splitString[0]*/
            } else {
                splitString[0] + "." + splitString[1]
                /* tvTOTAmountVAT.text =
                             splitString[0] + "." + splitString[1] + " AED"*/
            }
        }

        val merchantID =
            sharedPreferenceUtil.getMerchantID()
        activityProductDetailsBinding.tvMerchantNo.text = merchantID
        activityProductDetailsBinding.tvOrderNo.text = sharedPreferenceUtil.getTerminalID()
        //set the current data and time
        val calender = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        activityProductDetailsBinding.tvDate.text = dateFormat.format(calender)
        if (purchaseBreakdown != null) {
            if (purchaseBreakdown != null && purchaseBreakdown!!.service.size > 0) {
                for (itemDetail in purchaseBreakdown!!.service) {
                    /* val itemDetails = ItemDetail(
                         itemDetail.serviceCode,
                         itemDetail.serviceCode,
                         itemDetail.englishName,
                         itemDetail.arabicName,
                         itemDetail.quantity.toInt(),
                         itemDetail.transactionAmount.toFloat(),
                         itemDetail.totalAmount.toInt()
                     )
                     itemDetailList.add(itemDetails)*/
                    val service =
                        Product(
                            "S001",
                            itemDetail.englishName,
                            itemDetail.quantity,
                            itemDetail.unitPrice.toDouble(),
                            itemDetail.quantity, itemDetail.transactionAmount.toDouble(), null
                        )
                    serviceList.add(service)
                }
            }
            //  purchaseDetails = PurchaseBreakdownDetails(itemDetailList)

            val feeDetails = ProductDetails(serviceList)
            getFeeDetails(sharedPreferenceUtil, feeDetails)
        } else if (bundle.get("bookNumber") != null) {
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
            purchaseDetails = PurchaseBreakdownDetails(itemDetailList)
        } else {
            if (!sharedPreferenceUtil.getCataLogAmount()) {
                SharedData.selectedItems[0].transactionAmount = amount.toDouble()
                val feeDetails = ProductDetails(SharedData.selectedItems)//serviceList)
                getFeeDetails(sharedPreferenceUtil, feeDetails)
                serviceList = SharedData.selectedItems
            } else {
                val feeDetails = ProductDetails(SharedData.selectedItems)//serviceList)
                getFeeDetails(sharedPreferenceUtil, feeDetails)
                serviceList = SharedData.selectedItems
                SharedData.selectedItems = arrayListOf()
            }

        }

        activityProductDetailsBinding.countTV.text = serviceList.size.toString()
        val productAdapter = ProductAdapter(this, serviceList)

        activityProductDetailsBinding.recProductDetails.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        activityProductDetailsBinding.recProductDetails.adapter = productAdapter

        activityProductDetailsBinding.btnSubmit.setOnClickListener {
            ringPay?.start()
            val intent = Intent(this, CPOCConnectActivity::class.java)

            if (orderNumber != null) {
                intent.putExtra("OrderNumber", orderNumber)
            }

            if (bankTransferURL != null) {
                intent.putExtra("MerchantBankURL", bankTransferURL)
            }
            if (mainMerchantId != null) {
                intent.putExtra("mainMerchantId", mainMerchantId)
            }
            if (customerEmail != null) {
                intent.putExtra("customerEmail", customerEmail)
            }

            if (customerPhone != null) {
                intent.putExtra("customerPhone", customerPhone)
            }

            if (posId != null) {
                intent.putExtra("posId", posId)
            }

            if (posType != null) {
                intent.putExtra("posType", posType)
            }

            if (payrowInvoiceNo != null) {
                intent.putExtra("payrowInvoiceNo", payrowInvoiceNo)
            }

            if (trnNo != null) {
                intent.putExtra("trnNo", trnNo)
            }

            if (receiptNo != null) {
                intent.putExtra("receiptNo", receiptNo)
            }

            if (merchantEmail != null) {
                intent.putExtra("merchantEmail", merchantEmail)
            }
            if (userId != null) {
                intent.putExtra("userId", userId)
            }
            if (distributorId != null) {
                intent.putExtra("distributorId", distributorId)
            }

            if (storeId != null) {
                intent.putExtra("storeId", storeId)
            }

            if (customerName != null) {
                intent.putExtra("customerName", customerName)
            }

            if (customerEmail != null) {
                intent.putExtra("customerEmail", customerEmail)
            }
            if (customerPhone != null) {
                intent.putExtra("customerPhone", customerPhone)
            }
            if (customerBillingCity != null) {
                intent.putExtra("customerBillingCity", customerBillingCity)
            }

            if (customerBillingState != null) {
                intent.putExtra("customerBillingState", customerBillingState)
            }

            if (customerBillingCountry != null) {
                intent.putExtra("customerBillingCountry", customerBillingCountry)
            }

            if (customerBillingPostalCode != null) {
                intent.putExtra("customerBillingPostalCode", customerBillingPostalCode)
            }

            if (checkoutId != null) {
                intent.putExtra("checkoutId", checkoutId)
            }


            //  intent.putExtra("purchaseDetails", purchaseDetails as Serializable)
            if (feeResponseData != null) {
                intent.putExtra("PAYMENT TYPE", paymentType)
                intent.putExtra("AMOUNT", amount)
                intent.putExtra("AmountVAT", spiltString3)
                intent.putExtra("payRowVATAmount", payRowVATAmount)
                intent.putExtra("payRowVATStatus", payRowVATStatus)
                intent.putExtra("PayRowDigFee", payRowSecondaryCharges.toString())

                intent.putExtra("feeResponseData", feeResponseData)
                startActivity(intent)
                finish()
            } else {
                showToast("Invalid Service Details provided")
                finish()
            }

        }

        activityProductDetailsBinding.imgBackBtn.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    DashboardActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
            finish()
        }

        activityProductDetailsBinding.tvhomeLabel.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    DashboardActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
            finish()
        }

        activityProductDetailsBinding.ivAddItem.setOnClickListener {

            finish()
        }

        activityProductDetailsBinding.tvAddItemLabel.setOnClickListener {

            finish()
        }


        activityProductDetailsBinding.tvPayRowServiceLabel.setOnClickListener {
            if (surCharge) {
                val marginInDp = 95
                val marginInPx = (marginInDp * resources.displayMetrics.density).toInt()
                val params = activityProductDetailsBinding.clConstraintLayout.layoutParams as ConstraintLayout.LayoutParams
                params.height = marginInPx
                activityProductDetailsBinding.clConstraintLayout.layoutParams = params
                surCharge = false
                val img: Drawable =
                    resources.getDrawable(R.drawable.ic_icon_add_circle, null)
                activityProductDetailsBinding.imgPlus.setImageDrawable(img)
                activityProductDetailsBinding.clSurCharges.visibility = View.GONE
            } else {
                val marginInDp = 149
                val marginInPx = (marginInDp * resources.displayMetrics.density).toInt()
                val params = activityProductDetailsBinding.clConstraintLayout.layoutParams as ConstraintLayout.LayoutParams
                params.height = marginInPx
                activityProductDetailsBinding.clConstraintLayout.layoutParams = params
                surCharge = true
                val img: Drawable =
                    resources.getDrawable(R.drawable.ic_icon_remove_circle, null)
                activityProductDetailsBinding.imgPlus.setImageDrawable(img)
                activityProductDetailsBinding.clSurCharges.visibility = View.VISIBLE
            }
        }

        activityProductDetailsBinding.imgPlus.setOnClickListener {
            if (surCharge) {
                val marginInDp = 95
                val marginInPx = (marginInDp * resources.displayMetrics.density).toInt()
                val params = activityProductDetailsBinding.clConstraintLayout.layoutParams as ConstraintLayout.LayoutParams
                params.height = marginInPx
                activityProductDetailsBinding.clConstraintLayout.layoutParams = params
                surCharge = false
                val img: Drawable =
                    resources.getDrawable(R.drawable.ic_icon_add_circle, null)
                activityProductDetailsBinding.imgPlus.setImageDrawable(img)
                activityProductDetailsBinding.clSurCharges.visibility = View.GONE
            } else {
                val marginInDp = 149
                val marginInPx = (marginInDp * resources.displayMetrics.density).toInt()
                val params = activityProductDetailsBinding.clConstraintLayout.layoutParams as ConstraintLayout.LayoutParams
                params.height = marginInPx
                activityProductDetailsBinding.clConstraintLayout.layoutParams = params
                surCharge = true
                val img: Drawable =
                    resources.getDrawable(R.drawable.ic_icon_remove_circle, null)
                activityProductDetailsBinding.imgPlus.setImageDrawable(img)
                activityProductDetailsBinding.clSurCharges.visibility = View.VISIBLE
            }
        }

    }

    private fun getFeeDetails(
        sharedPreferenceUtil: SharedPreferenceUtil,
        feeDetails: ProductDetails
    ) {
        showLoadingDialog("Please wait..")
        if (orderNumber==null) {
            orderNumber = ContextUtils.randomValue().toString() + ContextUtils.getRandomLastValue()
        }


        if (bankTransferURL==null) {
            bankTransferURL = ApiClient.MERCHANT_BANK_TRANS_URL
        }

        if (customerName==null) {
            customerName = sharedPreferenceUtil.getMerchantName()
        }

        if (customerEmail==null) {
            customerEmail = sharedPreferenceUtil.getMailID()
        }

        if (customerBillingCity==null) {
            customerBillingCity = sharedPreferenceUtil.getCity()
        }

        if (customerBillingState==null) {
            customerBillingState = sharedPreferenceUtil.getAddress()
        }

        if (customerBillingCountry==null) {
            customerBillingCountry = sharedPreferenceUtil.getCountry()
        }

        if (customerBillingPostalCode==null) {
            customerBillingPostalCode = sharedPreferenceUtil.getBOBox()
        }

        /* if (urn.isNullOrEmpty()) {
             urn = sharedPreferenceUtil.getURN()//Constants.URN
         }*/

        if (bankTransferURL==null) {
            bankTransferURL = ApiClient.MERCHANT_BANK_TRANS_URL
        }

        if (customerPhone==null) {
            customerPhone =
                sharedPreferenceUtil.getMerchantMobileNumber()//preference!!.getString(Constants.MERCHANT_MOBILE_NUMBER, "")!!
        }

        if (distributorId==null) {
            distributorId = sharedPreferenceUtil.getDistributorID()
        }

        if (posId==null) {
            posId = sharedPreferenceUtil.getUserID()
        }

        if (storeId==null) {
            storeId = sharedPreferenceUtil.getStoreID()
        }

        if (posType==null) {
            posType = sharedPreferenceUtil.getUserRole()
        }

        if (receiptNo==null) {
            receiptNo = (100..999).shuffled().last().toString()
        }

        if (trnNo==null) {
            trnNo = (10000..99999).shuffled().last().toString()
        }

        if (payrowInvoiceNo==null) {
            payrowInvoiceNo = (10000..99999).shuffled().last().toString()
        }

        val paymentMethodList =
            arrayOf(Constants.EDIRHAM_CARD, Constants.NON_EDIRHAM_CARD)

        val feePrepareRequest = FeePrepareRequest(
            orderNumber!!,
            customerBillingState!!,
            customerBillingState!!,
            "EN",
            "Card",
            true,
            true,
            bankTransferURL!!,
            bankTransferURL!!,
            paymentMethodList,
            Constants.SESSIONS_TIMEOUT_SEC,
            customerName!!,
            Constants.EDIRHAM_CARD,
            "Pending",
            customerEmail!!,
            customerPhone!!,
            customerBillingCity!!,
            customerBillingState!!,
            customerBillingCountry!!,
            customerBillingPostalCode!!,
            sharedPreferenceUtil.getMerchantID(),
            feeDetails,
            payRowVATStatus,
            sharedPreferenceUtil.getReportID(),
            customerEmail!!,
            sharedPreferenceUtil.getTerminalID(),
            customerPhone!!,
            distributorId!!,
            posId!!,
            storeId!!, posType!!, receiptNo, trnNo, payrowInvoiceNo
        )

        val productViewModel =
            ViewModelProvider(
                this,
                ProductViewModelFactory(this)
            )[ProductViewModel::class.java]

        productViewModel.getFeeResponse(feePrepareRequest)
        productViewModel.getData().observeOnce(this) {
            dismissLoadingDialog()
            if (it?.data != null) {
                feeResponseData = it.data
                activityProductDetailsBinding.tvTOTAmount.text = ContextUtils.formatWithCommas(it.data.amount.toDouble()) + " AED"//it.data.amount + " AED"

                payRowSecondaryCharges = it.data.secondaryCharges.toFloat()
                activityProductDetailsBinding.tvPayRowServiceFee.text = ContextUtils.formatWithCommas(payRowSecondaryCharges.toDouble()) + " AED"//"$payRowSecondaryCharges AED"
            } else if (it?.errorMessage != null) {
                showToast(it.errorMessage)
                finish()
            }
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

    override fun onDestroy() {
        super.onDestroy()
        dismissLoadingDialog()
    }
}