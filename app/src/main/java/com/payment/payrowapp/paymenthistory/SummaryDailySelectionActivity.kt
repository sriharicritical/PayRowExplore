package com.payment.payrowapp.paymenthistory

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Bundle
import android.text.format.DateFormat
import android.view.WindowManager
import com.payment.payrowapp.R
import com.payment.payrowapp.databinding.ActivityStoreItemsBinding
import com.payment.payrowapp.databinding.ActivitySummaryDailySelectionBinding
import com.payment.payrowapp.utils.BaseActivity
import com.payment.payrowapp.utils.Constants
import com.payment.payrowapp.utils.ContextUtils
import com.payment.payrowapp.utils.UtilityClass

import java.text.SimpleDateFormat
import java.util.*

class SummaryDailySelectionActivity : BaseActivity() {

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
    var ring: MediaPlayer? = null
    var type = 0
   private lateinit var summaryDailySelectionActivity: ActivitySummaryDailySelectionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_summary_daily_selection)
        summaryDailySelectionActivity = ActivitySummaryDailySelectionBinding.inflate(layoutInflater)
        setContentView(summaryDailySelectionActivity.root)

       // setSupportActionBar(myToolbar)
        setupToolbar()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val bundleExtras = intent.extras
        supportActionBar?.title = bundleExtras?.getString("FROM").toString()
        ring = MediaPlayer.create(this, R.raw.sound_button_click)

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

        summaryDailySelectionActivity.tvFromDate.text = "$day $currentMonth"
        summaryDailySelectionActivity.tvToDate.text = "$day $currentMonth"

        // set current day
        val calendar = Calendar.getInstance().time
        val sdf =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val currentDate = sdf.format(calendar)
        val date = sdf.parse(currentDate)
        val dayOfTheWeek = DateFormat.format("EEEE", date) as String // Thursday


        summaryDailySelectionActivity.tvFromDay.text = dayOfTheWeek
        summaryDailySelectionActivity.tvToDay.text = dayOfTheWeek
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
                    summaryDailySelectionActivity.tvFromDate.text = "" + dayOfMonth + " " + months[monthOfYear]
                    val month = monthOfYear + 1
                    fromDate = "$year-$month-$dayOfMonth"
                    val fromSelDate = sdfDate.parse("$dayOfMonth-$month-$year")
                    summaryDailySelectionActivity.tvFromDay.text = simpleDateFormat.format(fromSelDate)
                } else if (toDateClicked) {
                    formatedToDate = ContextUtils.formatDatetoformat(year, monthOfYear, dayOfMonth)
                    val month = monthOfYear + 1
                    summaryDailySelectionActivity.tvToDate.text = "" + dayOfMonth + " " + months[monthOfYear]
                    toDate = "$year-$month-$dayOfMonth"
                    val toSelDate = sdfDate.parse("$dayOfMonth-$month-$year")
                    summaryDailySelectionActivity.tvToDay.text = simpleDateFormat.format(toSelDate)
                }
            },
            year,
            c.get(Calendar.MONTH),
            day
        )

        summaryDailySelectionActivity.btnSearchTransId.setOnClickListener {
            ring?.start()
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
                    if (type == 1) {
                        val bundle = Bundle()
                        bundle.putString("Heading",bundleExtras?.getString("FROM").toString())
                        bundle.putString(Constants.FROM, fromDate)
                        bundle.putString(Constants.TO, toDate)
                        bundle.putString(Constants.FROM_DATE, summaryDailySelectionActivity.tvFromDate.text.toString())
                        bundle.putString(Constants.TO_DATE, summaryDailySelectionActivity.tvToDate.text.toString())
                        startActivity(
                            Intent(
                                this,
                                DailyReportDetailActivity::class.java
                            ).putExtras(bundle)
                        )
                    } else if (type == 0){
                        val bundle = Bundle()
                        bundle.putString("Heading",bundleExtras?.getString("FROM").toString())
                        bundle.putString(Constants.FROM, fromDate)
                        bundle.putString(Constants.TO, toDate)
                        bundle.putString(Constants.FROM_DATE, summaryDailySelectionActivity.tvFromDate.text.toString())
                        bundle.putString(Constants.TO_DATE, summaryDailySelectionActivity.tvToDate.text.toString())
                        startActivity(
                            Intent(
                                this,
                                SummaryReportActivity::class.java
                            ).putExtras(bundle)
                        )
                    }
                }
            } else {
                showToast("Start date should not exceed End date")
            }
        }

        val imgSelect: Drawable = baseContext.resources.getDrawable(R.drawable.ic_vector_2x, null)
        val imgDeSelect: Drawable =
            baseContext.resources.getDrawable(R.drawable.ic_ellipse_2x, null)

        summaryDailySelectionActivity.clFromDate.setOnClickListener {
            ring?.start()

            c.set(year, month - 4, day)
            fromDateClicked = true
            toDateClicked = false
            dpd.datePicker.minDate = c.timeInMillis
            dpd.datePicker.maxDate = System.currentTimeMillis()
            dpd.show()
        }
        summaryDailySelectionActivity.clToDate.setOnClickListener {
            ring?.start()
            c.set(year, month - 4, day)
            toDateClicked = true
            fromDateClicked = false
            dpd.datePicker.minDate = c.timeInMillis
            dpd.datePicker.maxDate = System.currentTimeMillis()
            dpd.show()
        }

        summaryDailySelectionActivity.btnByTransId.setOnClickListener {
            ring?.start()
            type = 0
         //   cardFromToDate.visibility = View.GONE

            summaryDailySelectionActivity.btnByDate.setCompoundDrawablesWithIntrinsicBounds(null, null, imgDeSelect, null)
            summaryDailySelectionActivity.btnByTransId.setCompoundDrawablesWithIntrinsicBounds(null, null, imgSelect, null)
        }
        summaryDailySelectionActivity.btnByDate.setOnClickListener {
            ring?.start()
            type = 1
          //  btnSearchTransId.setBackgroundResource(R.drawable.button_round_dark_gray_bg_fill)
           // btnSearchTransId.isEnabled = true
           // cardFromToDate.visibility = View.VISIBLE

            summaryDailySelectionActivity.btnByDate.setCompoundDrawablesWithIntrinsicBounds(null, null, imgSelect, null)
            summaryDailySelectionActivity.btnByTransId.setCompoundDrawablesWithIntrinsicBounds(null, null, imgDeSelect, null)
        }
    }
}