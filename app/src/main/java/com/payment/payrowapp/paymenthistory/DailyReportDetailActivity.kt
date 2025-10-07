package com.payment.payrowapp.paymenthistory

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Base64.encodeToString
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.payment.payrowapp.R
import com.payment.payrowapp.adapters.DailyReportAdapter
import com.payment.payrowapp.dataclass.DailyReportRequest
import com.payment.payrowapp.dataclass.Dates
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.BaseActivity
import java.text.SimpleDateFormat
import android.util.Base64
import com.payment.payrowapp.databinding.ActivityDailyReportDetailBinding
import com.payment.payrowapp.dataclass.ReportDate
import com.payment.payrowapp.dataclass.SendTIDReportReq
import com.payment.payrowapp.utils.Constants
import com.payment.payrowapp.utils.ContextUtils
import org.json.JSONObject
import java.util.*

class DailyReportDetailActivity : BaseActivity() {
    private var reportPath: String? = null
    var ring: MediaPlayer? = null
    private lateinit var instantReportAdapter: DailyReportAdapter
    var currentDate: String? = null
    var currentTime: String? = null
    private var fromDate: String? = null
    var instantReportViewModel: InstantReportViewModel? = null
    var channel: String? = null
    var toDate: String? = null
    private lateinit var binding:ActivityDailyReportDetailBinding

    private var sendTIDReportReq: SendTIDReportReq? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_daily_report_detail)
        binding = ActivityDailyReportDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
     //   setSupportActionBar(myToolbar)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val bundle = intent.extras
        supportActionBar?.title = bundle?.getString("Heading").toString()

        when (bundle?.getString("Heading")) {
            "Tap To Pay" -> channel = "Card"
            "Cash Invoice" -> channel = "Cash"
            "Pay By Link" -> channel = "Paybylink"
            "Pay By QR Code" -> channel = "generateQR"
        }

        ring = MediaPlayer.create(this, R.raw.sound_button_click)

        val sharedPreferenceUtil = SharedPreferenceUtil(this)
        val merchantID =
            sharedPreferenceUtil.getMerchantID()//preference!!.getString(Constants.MERCHANT_ID, "")
        binding.tvVAT.text = "MID: $merchantID"
        val tid = sharedPreferenceUtil.getTerminalID()

        binding.tvNameOfTheBusiness.text =
            sharedPreferenceUtil.getMerchantName() + sharedPreferenceUtil.getMerchantLastName()

        val calendar = Calendar.getInstance().time
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val sdfFrom = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfTime = SimpleDateFormat("hh:mm")

        currentDate = sdf.format(calendar)
        currentTime = sdfTime.format(Date())
        //  fromDate = sdfFrom.format(calendar)

        fromDate = bundle?.getString(Constants.FROM)
        toDate = bundle?.getString(Constants.TO)

        binding.tvDate.text =
            bundle?.getString(Constants.FROM_DATE) + " to " + bundle?.getString(Constants.TO_DATE)//currentDate

        binding.recDailyReport.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        instantReportViewModel =
            ViewModelProvider(
                this,
                InstantReportViewModelFactory(this)
            ).get(InstantReportViewModel::class.java)


        getDailyReport(merchantID, tid,sharedPreferenceUtil.getGatewayMerchantID())

        // select calender
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)


        val dpd = DatePickerDialog(
            this, { view, year, monthOfYear, dayOfMonth ->
                val month = monthOfYear + 1
                binding.tvDate.text = "$dayOfMonth/$month/$year"
                fromDate = "$year-$month-$dayOfMonth"
             //   getDailyReport(merchantID, tid)
            },
            year,
            month,
            day
        )

        binding.ivDate.setOnClickListener {
            /*  ring?.start()
              c.set(year, month - 3, day)
              dpd.datePicker.minDate = c.timeInMillis
              dpd.datePicker.maxDate = System.currentTimeMillis()
              dpd.show()*/
        }

        binding.tvDate.setOnClickListener {
            /*  ring?.start()
              c.set(year, month - 3, day)
              dpd.datePicker.minDate = c.timeInMillis
              dpd.datePicker.maxDate = System.currentTimeMillis()
              dpd.show()*/
        }

        binding.tvDownloadReport.setOnClickListener {
            ring?.start()
            if (reportPath != null) {
                DownloadReportDialog(sendTIDReportReq, this).show()
            } else {
                showToast("There is NO Reports for Download")
            }
        }

        binding.ivDownload.setOnClickListener {
            ring?.start()
            if (reportPath != null) {
                DownloadReportDialog(sendTIDReportReq, this).show()
            } else {
                showToast("There is NO Reports for Download")
            }

        }
    }

    private fun getDailyReport(merchantID: String, tID: String, gatewayMerchantID: String) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please wait...")
        progressDialog.show()
        val jsonObject = JSONObject()
        jsonObject.put("num", ContextUtils.randomValue())
        jsonObject.put("validation", "Key Validation")
        val encodedString = encodeToString(jsonObject.toString().toByteArray(), Base64.DEFAULT)
        val dates = Dates(fromDate!!, toDate!!)
        val dailyReportRequest =
            DailyReportRequest(null, channel, dates, merchantID, encodedString, tID)

        val reportDate = ReportDate(fromDate, toDate)
        sendTIDReportReq = SendTIDReportReq(null, channel, reportDate, merchantID, tID, null, true,
            gatewayMerchantID )
        instantReportViewModel!!.getDailyReportResponse(dailyReportRequest)

        instantReportViewModel!!.getDailyReportData().observe(this) {
            if (it.data.isNotEmpty()) {
                reportPath = "it.reportPath"
                binding.recDailyReport.visibility = View.VISIBLE
                binding.tvNoData.visibility = View.GONE
                binding.ivNoData.visibility = View.GONE
                instantReportAdapter = DailyReportAdapter(this, it)
                binding.recDailyReport.adapter = instantReportAdapter
            } else {
                reportPath = null
                binding.recDailyReport.visibility = View.GONE
                binding.tvNoData.visibility = View.VISIBLE
                binding.ivNoData.visibility = View.VISIBLE
            }
            progressDialog.cancel()
        }
    }
}