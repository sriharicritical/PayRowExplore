package com.payment.payrowapp.newpayment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.media.MediaPlayer
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationServices
import com.payment.payrowapp.R
import com.payment.payrowapp.databinding.ActivityEnterAmountToPayCardBinding
import com.payment.payrowapp.databinding.ActivityEnterAmountToPayCashBinding
import com.payment.payrowapp.dataclass.Product
import com.payment.payrowapp.dataclass.PurchaseBreakdown
import com.payment.payrowapp.dataclass.SharedData
import com.payment.payrowapp.mastercloud.CPOCConnectActivity
import com.payment.payrowapp.observeOnce
import com.payment.payrowapp.qrcodescan.QRCodeScanActivity
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.BaseActivity
import com.payment.payrowapp.dataclass.ItemsCountRequest
import com.payment.payrowapp.sunmipay.ProductActivity
import com.payment.payrowapp.utils.ContextUtils
import com.payrow.cardreader.interfaces.GetCardDetails
import org.json.JSONObject
import java.io.Serializable
import java.util.ArrayList

class EnterAmountToPayCardActivity : BaseActivity(), GetCardDetails {

    var ring: MediaPlayer? = null
   // private var ringPay: MediaPlayer? = null
    private var getCardDetails: GetCardDetails? = null
    private var itemCount = 0
    private var totalAmountQRCode = "0"
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
    private var orderNumber: String? = null
    private var paymentType = "card"
    private var customerBillingPostalCode: String? = null
    private var customerBillingState: String? = null
    private var customerBillingCity: String? = null
    private var customerBillingCountry: String? = null
    private var customerName: String? = null
    private var checkoutId: String? = null
    private var bookNumber: String? = null
    private lateinit var sharedPreferenceUtil: SharedPreferenceUtil
    private var qrcodeStatus = false

    private val LOCATION_PERMISSION_REQUEST_CODE = 102
    val REQUEST_AUDIO_PERMISSION_CODE = 101

    private lateinit var binding:ActivityEnterAmountToPayCardBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_enter_amount_to_pay_card)
        binding = ActivityEnterAmountToPayCardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
      //  setSupportActionBar(myToolbar)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Tap To Pay"

        getCardDetails = this
        ring = MediaPlayer.create(this, R.raw.sound_button_click)
       // ringPay = MediaPlayer.create(this, R.raw.sound_pay_button)
        // ring.setVolume(1.05,2.09)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        sharedPreferenceUtil = SharedPreferenceUtil(this)
        val merchantID =
            sharedPreferenceUtil.getMerchantID()
        binding.btnInvoiceNumber.text = "MID: $merchantID"
        binding.tvStoreName.text =
            sharedPreferenceUtil.getMerchantName() + sharedPreferenceUtil.getMerchantLastName()

        if (sharedPreferenceUtil.getScanBarCode() && sharedPreferenceUtil.getCataLogAmount()) {
            binding.etEnterAmount.isEnabled = false
            binding.btnAddItem.visibility = View.VISIBLE

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
            binding.etEnterAmount.isEnabled = false
            binding.btnAddItem.visibility = View.VISIBLE

            /*  val marginInDp = 16
              val marginInPx = (marginInDp * resources.displayMetrics.density).toInt()
              val params = btnScanBarCode.layoutParams as ConstraintLayout.LayoutParams
              params.topMargin = marginInPx

              btnScanBarCode.layoutParams = params
              btnScanBarCode.visibility = View.GONE*/
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
            StoreItemsRepository.getItemCountLiveData()
                .observeOnce(this@EnterAmountToPayCardActivity) {
                    if (it?.data != null) {
                        SharedData.selectedItems = ArrayList<Product>()

                        val dataObject = JSONObject(it.data)

                        //val posObject = dataObject.getJSONArray("defaultPosServ").getJSONObject(0)
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

        binding.etEnterAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                //To change body of created functions use File | Settings | File Templates.

                if (binding.etEnterAmount.text.toString().isNotEmpty()) {
                    //   sharedPreferenceUtil.setAmount(etEnterAmount.text.toString())
                }
                binding.btnPay.setBackgroundResource(R.drawable.button_round_dark_gray_bg_fill)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //To change body of created functions use File | Settings | File Templates.
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //To change body of created functions use File | Settings | File Templates.
                //var formatter: NumberFormat = DecimalFormat("#,###,###.##")
                try {
                    if (p0?.length == 0) {
                        binding.btnPay.setBackgroundResource(R.drawable.button_round_gray_bg_fill)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        })

        binding.btnAddItem.setOnClickListener {
            ring?.start()
            val bundle1 = Bundle()
            bundle1.putString("TYPE", "TAPTOPAY")
            startActivityForResult(
                (Intent(this, StoreItemsActivity::class.java)), 2
            )
        }
        binding.btnScanBarCode.setOnClickListener {
            ring?.start()
            startActivityForResult((Intent(this, QRCodeScanActivity::class.java)), 3)
        }
        binding.btnPay.setOnClickListener {
            ring?.start()
            if (itemCount > 0) {
                if (binding.etEnterAmount.text.toString().isNotEmpty()) {
                    if (binding.etEnterAmount.text.toString().toFloat() <= 1000000) {
                        if (binding.etEnterAmount.text.toString().toFloat() > 0) {
                          //  ring?.start()

                           /* val intent =
                                Intent(baseContext, CPOCConnectActivity::class.java)
                            intent.putExtra("AMOUNT", etEnterAmount.text.toString())
                            startActivity(intent)*/

                            val intent = Intent(this, ProductActivity::class.java)

                            if (purchaseBreakdown != null) {
                                intent.putExtra(
                                    "purchaseBreakDown",
                                    purchaseBreakdown as Serializable
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
                            if (mainMerchantId != null) {
                                intent.putExtra(
                                    "mainMerchantId",
                                    mainMerchantId
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

                            if (posId != null) {
                                intent.putExtra(
                                    "posId",
                                    posId
                                )
                            }

                            if (posType != null) {
                                intent.putExtra(
                                    "posType",
                                    posType
                                )
                            }

                            if (payrowInvoiceNo != null) {
                                intent.putExtra(
                                    "payrowInvoiceNo",
                                    payrowInvoiceNo
                                )
                            }

                            if (trnNo != null) {
                                intent.putExtra(
                                    "trnNo",
                                    trnNo
                                )
                            }

                            if (receiptNo != null) {
                                intent.putExtra(
                                    "receiptNo",
                                    receiptNo
                                )
                            }

                            if (merchantEmail != null) {
                                intent.putExtra(
                                    "merchantEmail",
                                    merchantEmail
                                )
                            }
                            if (userId != null) {
                                intent.putExtra(
                                    "userId",
                                    userId
                                )
                            }
                            if (distributorId != null) {
                                intent.putExtra(
                                    "distributorId",
                                    distributorId
                                )
                            }

                            if (storeId != null) {
                                intent.putExtra(
                                    "storeId",
                                    storeId
                                )
                            }

                            if (customerName != null) {
                                intent.putExtra(
                                    "customerName",
                                    customerName
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

                            if (checkoutId != null) {
                                intent.putExtra(
                                    "checkoutId",
                                    checkoutId
                                )
                            }

                            if (bookNumber != null) {
                                intent.putExtra(
                                    "bookNumber",
                                    bookNumber
                                )
                            }

                            intent.putExtra("PAYMENT TYPE", paymentType)
                            intent.putExtra("AMOUNT", binding.etEnterAmount.text.toString())

                            binding.etEnterAmount.setText("")
                            // if (paymentType == "scan qr") {
                            if (sharedPreferenceUtil.getCataLogAmount() || qrcodeStatus) {
                                itemCount = 0
                                binding.btnItemCount.visibility = View.GONE
                            }
                            // }
                            startActivity(intent)
                        } else {
                            showToast(getString(R.string.please_enter_amount_greater_zero))
                        }
                    } else {
                        showToast(getString(R.string.kindly_enter_lesser_amount))
                    }
                } else {
                    showToast(resources.getString(R.string.please_enter_the_amount_to_proceed))
                }
            } else {
              //  showToast(getString(R.string.please_select_item_to_proceed))
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

        val cardViewModel =
            ViewModelProvider(
                this,
                CardViewModelFactory(this)
            )[EnterCardViewModel::class.java]
        cardViewModel.callTerminal(this)

        checkLocationPermission()
    }

    override fun showCardDetails(cardNumber: String, cardExpiry: String) {
        showToast("Card:$cardNumber Expiry:$cardExpiry")
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        //constructTerminalData()
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            // data.getStringExtra("itemList")
            itemCount = data.getIntExtra("itemLength", 0)
            //  tvTapToPay.text = "New Payment"
            totalAmountQRCode = data.getStringExtra("totalAmount").toString()
            paymentType = "card"


            if (resultCode == 2) {
                if (itemCount > 0) {
                    //  selectedItemAmount = data.getStringExtra("transactionAmount").toString()
                    //if (sharedPreferenceUtil.getCataLogAmount()) {
                    binding.etEnterAmount.isEnabled = false
                    val tranAmount = data.getStringExtra("transactionAmount").toString()
                    binding.etEnterAmount.setText(ContextUtils.formatWithCommas(tranAmount.toDouble()))
                    // }

                    binding.btnItemCount.visibility = View.VISIBLE
                    binding.btnItemCount.text =
                        "+" + data.getIntExtra("itemLength", 0).toString() + " items"
                }
            } else if (resultCode == 3) {

                binding.etEnterAmount.isEnabled = false
                if (itemCount > 0) {
                    if (!sharedPreferenceUtil.getCataLogAmount()) {
                        SharedData.selectedItems = arrayListOf()
                    }

                    qrcodeStatus = true
                    binding.btnItemCount.visibility = View.VISIBLE
                    binding.btnItemCount.text =
                        "+" + data.getIntExtra("itemLength", 0).toString() + " items"
                    paymentType = "scan qr"
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
                    bankTransferURL = data.extras!!.getString("MerchantBankURL")

                    customerName = data.extras!!.getString("customerName")
                    customerBillingCountry = data.extras!!.getString("customerBillingCountry")
                    customerBillingCity = data.extras!!.getString("customerBillingCity")
                    customerBillingState = data.extras!!.getString("customerBillingState")
                    customerBillingPostalCode = data.extras!!.getString("customerBillingPostalCode")
                    checkoutId = data.extras!!.getString("checkoutId")
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

    // Method to check permissions and request them
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, show explanation or request permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission is already granted
            if (isGPSDisabled()) {
                Toast.makeText(
                    this,
                    "Please Enable the Location to use the application",
                    Toast.LENGTH_SHORT
                ).show()
            }
            getLocation()
        }
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission granted, proceed with location access
                    if (isGPSDisabled()) {
                        Toast.makeText(
                            this,
                            "Please Enable the Location to use the application",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        getLocation()
                        if (isAudioPermissionNotGranted()) {
                            requestAudioPermissions()
                        }
                    }

                } else {
                    // Permission denied, show an explanation
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
            REQUEST_AUDIO_PERMISSION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && permissions.size > 0 && permissions[0] == Manifest.permission.RECORD_AUDIO) {
                    //nothing to do
                } else {
                    Toast.makeText(
                        this@EnterAmountToPayCardActivity,
                        "Cloud Commerce needs access to your microphone to protect your transactions. You can continue using the app, but not accept contactless payments.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }

            else -> {
                // Ignore all other requests
            }
        }
    }

    private fun isGPSDisabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun getLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations, this can be null.
                location?.let {
                    val latitude = it.latitude
                    val longitude = it.longitude
                    // Use the location data
                    //    Toast.makeText(this, "Lat: $latitude, Lng: $longitude", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun isAudioPermissionNotGranted(): Boolean {
        val audioPermissionResult = ContextCompat.checkSelfPermission(
            applicationContext, Manifest.permission.RECORD_AUDIO
        )
        return audioPermissionResult != PackageManager.PERMISSION_GRANTED
    }

    private fun requestAudioPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            REQUEST_AUDIO_PERMISSION_CODE
        )
    }
}

