package com.payment.payrowapp.invoicerecall

import android.app.ProgressDialog
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.payment.payrowapp.R
import com.payment.payrowapp.adapters.InvoicesListAdapter
import com.payment.payrowapp.databinding.ActivityEnterTidactivityBinding
import com.payment.payrowapp.databinding.ActivityInvoicesListBinding
import com.payment.payrowapp.dataclass.Date
import com.payment.payrowapp.dataclass.EnquiryRequestClass
import com.payment.payrowapp.dataclass.InvoiceRecallByDatesRequest
import com.payment.payrowapp.dialogs.OnItemClickListener
import com.payment.payrowapp.generateqrcode.GenerateQRCodeReceiptActivity
import com.payment.payrowapp.generateqrcode.GenerateQRCodeViewModel
import com.payment.payrowapp.generateqrcode.GenerateQRCodeViewModelFactory
import com.payment.payrowapp.observeOnce
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.sunmipay.LoadingDialog
import com.payment.payrowapp.utils.BaseActivity
import com.payment.payrowapp.utils.Constants
import com.payment.payrowapp.utils.ContextUtils
import com.payment.payrowapp.utils.LoaderCallback
import org.json.JSONObject

class InvoicesListActivity : BaseActivity(), OnItemClickListener, LoaderCallback {
    var ring: MediaPlayer? = null
    var from: String? = null
    var to: String? = null
    private lateinit var generateQRCodeViewModel: GenerateQRCodeViewModel
    private lateinit var sharedPreferenceUtil: SharedPreferenceUtil
    private var loadDialog: LoadingDialog? = null

    private lateinit var binding: ActivityInvoicesListBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_invoices_list)
        binding = ActivityInvoicesListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // setSupportActionBar(myToolbar)
        setupToolbar()

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        getSupportActionBar()?.setDisplayShowHomeEnabled(true)

        getSupportActionBar()?.title = "Invoice List"

        ring = MediaPlayer.create(this, R.raw.sound_button_click)

        generateQRCodeViewModel =
            ViewModelProvider(
                this,
                GenerateQRCodeViewModelFactory(this)
            ).get(GenerateQRCodeViewModel::class.java)
         sharedPreferenceUtil = SharedPreferenceUtil(this)
        val merchantID =
            sharedPreferenceUtil.getMerchantID()//preference!!.getString(Constants.MERCHANT_ID, "")
        binding.tvVAT.text = "MID: $merchantID"

        binding.tvNameOfTheBusiness.text =
            sharedPreferenceUtil.getMerchantName() + sharedPreferenceUtil.getMerchantLastName()

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please wait...")
        progressDialog.show()

        val bundle = intent.extras
        try {

            from = bundle?.getString(Constants.FROM)
            to = bundle?.getString(Constants.TO)
            binding.tvDate.setText(
                bundle?.getString(Constants.FROM_DATE) + " to " + bundle?.getString(
                    Constants.TO_DATE
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        binding.recInvoiceReport.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        val invoicesListViewModel =
            ViewModelProvider(
                this,
                InvoicesListViewModelFactory(this)
            ).get(InvoicesListViewModel::class.java)

        val dates = Date(from!!, to!!)

        val jsonObject = JSONObject()
        jsonObject.put("num", ContextUtils.randomValue())
        jsonObject.put("validation", "Key Validation")
        val encodedString =
            Base64.encodeToString(jsonObject.toString().toByteArray(), Base64.DEFAULT)
        val invoiceRecallByDatesRequest = InvoiceRecallByDatesRequest(
            dates, null, encodedString,
            sharedPreferenceUtil.getTerminalID()
        )
        invoicesListViewModel.getDailyReportResponse(invoiceRecallByDatesRequest, this)

        invoicesListViewModel.getDailyReportData().observe(this) {
            if (it.data.isNotEmpty()) {
                binding.recInvoiceReport.visibility = View.VISIBLE
                binding.tvNoData.visibility = View.GONE
                binding.ivNoData.visibility = View.GONE
                val adapter = InvoicesListAdapter(ring,this, it,this)
                binding.recInvoiceReport.adapter = adapter
            } else {
                binding.recInvoiceReport.visibility = View.GONE
                binding.tvNoData.visibility = View.VISIBLE
                binding.ivNoData.visibility = View.VISIBLE
            }
            progressDialog.cancel()
        }

    }


    override fun onItemClicked(orderNumber: String) {
        getEnquiryStatus(orderNumber)
    }

    private fun getEnquiryStatus(orderNumber: String) {
        runOnUiThread {
            showLoadingDialog("Please wait..")
            val enquiryRequestClass =
                EnquiryRequestClass(orderNumber, sharedPreferenceUtil.getGatewayMerchantID())
            generateQRCodeViewModel.getEnquiryResponse(enquiryRequestClass) { result ->
                dismissLoadingDialog()
                showToast(getString(R.string.something_went_wrong_try))
            }
            generateQRCodeViewModel.getEnquiryData()
                .observeOnce(this@InvoicesListActivity) {
                    dismissLoadingDialog()
                    if (it.data != null && !it.data?.checkoutStatus.isNullOrEmpty()) {
                        val intent =
                            Intent(
                                this@InvoicesListActivity,
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

    override fun closeLoader() {
        runOnUiThread {
            showToast(getString(R.string.something_went_wrong_try))
            dismissLoadingDialog()
            finish()
        }
    }
}