package com.payment.payrowapp.paymenthistory

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.dzmitry_lakisau.month_year_picker_dialog.MonthYearPickerDialog
import com.payment.payrowapp.R
import com.payment.payrowapp.adapters.MonthlyReportAdapter
import com.payment.payrowapp.databinding.ActivityMonthlyReportDetailBinding
import com.payment.payrowapp.databinding.ActivitySelectOptionBinding
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.BaseActivity
import java.text.SimpleDateFormat
import java.util.*

class MonthlyReportDetailActivity : BaseActivity(), MonthYearPickerDialog.OnDateSetListener {
    var ring: MediaPlayer? = null
    private lateinit var instantReportAdapter: MonthlyReportAdapter
    var currentDate: String? = null
    var currentTime: String? = null
    var fromDate: String? = null
    var instantReportViewModel: InstantReportViewModel? = null
    lateinit var channel: String
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
    private lateinit var binding: ActivityMonthlyReportDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //  setContentView(R.layout.activity_monthly_report_detail)
        binding = ActivityMonthlyReportDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //  setSupportActionBar(myToolbar)
        setupToolbar()

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        getSupportActionBar()?.setDisplayShowHomeEnabled(true)

        ring = MediaPlayer.create(this, R.raw.sound_button_click)

        val bundle = intent.extras
        getSupportActionBar()?.title = bundle?.getString("FROM").toString()
        when (bundle?.getString("FROM")) {
            "Tap To Pay" -> channel = "Card"
            "Cash Invoice" -> channel = "Cash"
            "Pay By Link" -> channel = "Paybylink"
            "Pay By QR Code" -> channel = "generateQR"
        }


        val sharedPreferenceUtil = SharedPreferenceUtil(this)
        val merchantID =
            sharedPreferenceUtil.getMerchantID()//preference!!.getString(Constants.MERCHANT_ID, "")
        binding.tvVAT.text = "MID: $merchantID"

        binding.tvNameOfTheBusiness.text =
            sharedPreferenceUtil.getMerchantName() + sharedPreferenceUtil.getMerchantLastName()

        val calendar = Calendar.getInstance()
        val sdfFrom = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val sdfTime = SimpleDateFormat("hh:mm")
        currentDate = sdf.format(Date())
        currentTime = sdfTime.format(Date())
        fromDate = sdfFrom.format(calendar.time)
        val currentMonth =
            calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
        val currentYear = calendar.get(Calendar.YEAR)
        binding.tvMonth.text = "$currentYear"

        binding.recMonthlyReport.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        instantReportViewModel =
            ViewModelProvider(
                this,
                InstantReportViewModelFactory(this)
            ).get(InstantReportViewModel::class.java)


        // set calender
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        getDailyReport(year)

        val dpd = DatePickerDialog(
            this, { view, year, monthOfYear, dayOfMonth ->
                val month = monthOfYear + 1
                binding.tvMonth.text = "" + months[monthOfYear] + "-" + year
                fromDate = "$year-$month-$dayOfMonth"
                getDailyReport(year)
            },
            year,
            month,
            day
        )

        val datePicker = MonthYearPickerDialog.Builder(
            this,
            R.style.Style_MonthYearPickerDialog_Orange,
            onDateSetListener = this,
            selectedYear = year
        )
            .setMinYear(2015)
            .setMaxYear(year)
            .setMode(MonthYearPickerDialog.Mode.YEAR_ONLY)
            .setOnYearSelectedListener { year ->
                // do something

            }
            .build()

        binding.ivDate.setOnClickListener {
            ring?.start()
            dpd.datePicker.maxDate = System.currentTimeMillis()
            datePicker.show()
        }

        binding.tvMonth.setOnClickListener {
            ring?.start()
            dpd.datePicker.maxDate = System.currentTimeMillis()
            datePicker.show()
        }
        /* instantReportViewModel.getTotalReportResponse(ContextUtils.getUserId(this))

         instantReportViewModel.getTotalReportData().observe(this) {
             val adapter = MonthlyReportAdapter(this, it)
             recMonthlyReport.adapter = adapter
         }
 */
    }

    private fun getDailyReport(year: Int) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please wait...")
        progressDialog.show()
        // val monthlyReportRequest = MonthlyReportRequest(year, channel)
        instantReportViewModel!!.getMonthlyReportResponse(year, channel)

        instantReportViewModel!!.getMonthlyReportData().observe(this) {
            if (it?.data != null && it.data.isNotEmpty()) {
                binding.recMonthlyReport.visibility = View.VISIBLE
                binding.tvNoData.visibility = View.GONE
                binding.ivNoData.visibility = View.GONE
                instantReportAdapter =
                    MonthlyReportAdapter(
                        ring,
                        this,
                        this,
                        it,
                        year,
                        channel,
                        instantReportViewModel!!
                    )
                binding.recMonthlyReport.adapter = instantReportAdapter
            } else {
                binding.recMonthlyReport.visibility = View.GONE
                binding.tvNoData.visibility = View.VISIBLE
                binding.ivNoData.visibility = View.VISIBLE
            }
            progressDialog.cancel()
        }
        /* val dates = Dates(fromDate!!)
         val dailyReportRequest = DailyReportRequest("montly", channel, dates)
         instantReportViewModel!!.getDailyReportResponse(dailyReportRequest)

         instantReportViewModel!!.getDailyReportData().observe(this) {
             if (it != null) {
                 recMonthlyReport.visibility = View.VISIBLE
                 tvNoData.visibility = View.GONE
                 ivNoData.visibility = View.GONE
                 instantReportAdapter = MonthlyReportAdapter(this, it)
                 recMonthlyReport.adapter = instantReportAdapter
             } else {
                 recMonthlyReport.visibility = View.GONE
                 tvNoData.visibility = View.VISIBLE
                 ivNoData.visibility = View.VISIBLE
             }
             progressDialog.cancel()
         }*/
    }

    override fun onDateSet(year: Int, month: Int) {
        binding.tvMonth.text = "$year"
        getDailyReport(year)
    }
}