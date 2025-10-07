package com.payment.payrowapp.paymenthistory

import android.content.Intent
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider
/*import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet*/
import com.payment.payrowapp.R
import com.payment.payrowapp.databinding.ActivityDailyReportDetailBinding
import com.payment.payrowapp.databinding.ActivityPaymentHistoryBinding
import com.payment.payrowapp.dataclass.TotalSalesResponse
import com.payment.payrowapp.invoicerecall.SelectOptionForInvoiceRecallActivity
import com.payment.payrowapp.login.AuthenticationActivity
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.BaseActivity
import com.payment.payrowapp.utils.ContextUtils
import java.util.*


class PaymentHistoryActivity : BaseActivity() {
    var ring: MediaPlayer? = null
    var totalSalesResponse: TotalSalesResponse? = null
    private lateinit var binding: ActivityPaymentHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      //  setContentView(R.layout.activity_payment_history)
        binding = ActivityPaymentHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //setSupportActionBar(myToolbar)
        setupToolbar()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Payment History"

        ring = MediaPlayer.create(this, R.raw.sound_button_click)

        val calendar = Calendar.getInstance()
        val currentDay =
            calendar.get(Calendar.DAY_OF_MONTH)
        val currentMonth =
            calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
        val currentYear = calendar.get(Calendar.YEAR)

        binding.tvTotalSeqLabel.text = "SEQUENCES - $currentMonth"
        binding.tvTotalCredit.text = "TOTAL CREDIT - $currentMonth"
        val paymentHistoryViewModel =
            ViewModelProvider(
                this,
                PaymentHistoryViewModelFactory(this)
            ).get(PaymentHistoryViewModel::class.java)
        val sharedPreferenceUtil = SharedPreferenceUtil(this)
        paymentHistoryViewModel.getTotalAmountResponse(sharedPreferenceUtil.getTerminalID())
        paymentHistoryViewModel.getTotalAmountData().observe(this) {
            try {
                binding.tvTotalSeq.text = it.data.total.count
               /* binding.progressText.text = it.data.total.avgCount
                binding.progressBar.max = it.data.total.avgCount.toInt()
                binding.progressBar.progress = it.data.total.count.toInt()

                val countPercentage =
                    (100 * it.data.total.count.toInt()) / it.data.total.avgCount.toInt()

                if (countPercentage < 50) {
                    setProgressBarColor(Color.RED)
                } else if (countPercentage in 50..75) {
                    setProgressBarColor(Color.YELLOW)
                } else if (countPercentage > 75) {
                    setProgressBarColor(resources.getColor(R.color.color_donut))
                }*/

                binding.tvTotalCreditValue.text = ContextUtils.formatWithCommas(it.data.total.totalCredit.toDouble())//it.data.total.totalCredit
               /* binding.progressCreditText.text = it.data.total.avgValue
                binding.progressCreditBar.max = it.data.total.avgValue.toInt()
                binding.progressCreditBar.progress = it.data.total.totalCredit.toInt()

                val creditPercentage =
                    (100 * it.data.total.totalCredit.toInt()) / it.data.total.avgValue.toInt()

                if (creditPercentage < 50) {
                    setCreditProgressBarColor(Color.RED)
                } else if (creditPercentage in 50..75) {
                    setCreditProgressBarColor(Color.YELLOW)
                } else if (creditPercentage > 75) {
                    setCreditProgressBarColor(resources.getColor(R.color.color_donut))
                }*/

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
     /*   paymentHistoryViewModel.getTotalSalesResponse(sharedPreferenceUtil.getUserID())
        paymentHistoryViewModel.getTotalSalesData().observe(this) {
            totalSalesResponse = it
            setBarChart(it, barChart)
        }*/

        binding.btnDailyReport.setOnClickListener {
            ring?.start()
            val bundle = Bundle()
            bundle.putString("FROM", "DAILY")
            startActivity(Intent(this, AuthenticationActivity::class.java).putExtras(bundle))
        }
        binding.btnMonthlyReport.setOnClickListener {
            ring?.start()
            val bundle = Bundle()
            bundle.putString("FROM", "MONTHLY")
            startActivity(Intent(this, AuthenticationActivity::class.java).putExtras(bundle))
        }
        binding.btnBuyNowPayLater.setOnClickListener {
            ring?.start()
            startActivity(Intent(this, SelectOptionForInvoiceRecallActivity::class.java))
        }

    }

    private fun setCreditProgressBarColor(progressColor: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.progressCreditBar.progressDrawable.colorFilter =
                BlendModeColorFilter(progressColor, BlendMode.SRC_IN)
        } else {
            binding.progressCreditBar.progressDrawable
                .setColorFilter(progressColor, PorterDuff.Mode.SRC_IN)
        }
    }

    private fun setProgressBarColor(color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.progressBar.progressDrawable.colorFilter =
                BlendModeColorFilter(color, BlendMode.SRC_IN)
        } else {
            binding.progressBar.progressDrawable
                .setColorFilter(color, PorterDuff.Mode.SRC_IN)
        }
    }

   /* private fun setBarChart(totalSalesResponse: TotalSalesResponse, barChart: BarChart) {
        val entries = ArrayList<BarEntry>()
        if (!totalSalesResponse.data.isNullOrEmpty() && totalSalesResponse.data[0].count > 0) {
            entries.add(
                BarEntry(
                    0f,
                    totalSalesResponse.data[0].total
                )
            )
        } else {
            entries.add(BarEntry(0f, 0f))
        }

        if (!totalSalesResponse.data.isNullOrEmpty() && totalSalesResponse.data[1].count > 0) {
            entries.add(
                BarEntry(
                    1f,
                    totalSalesResponse.data[1].total
                )
            )
        } else {
            entries.add(BarEntry(1f, 0f))
        }
        if (!totalSalesResponse.data.isNullOrEmpty() && totalSalesResponse.data[2].count > 0) {
            entries.add(
                BarEntry(
                    2f,
                    totalSalesResponse.data[2].total
                )
            )
        } else {
            entries.add(BarEntry(2f, 0f))
        }
        if (!totalSalesResponse.data.isNullOrEmpty() && totalSalesResponse.data[3].count > 0) {
            entries.add(
                BarEntry(
                    3f,
                    totalSalesResponse.data[3].total
                )
            )
        } else {
            entries.add(BarEntry(3f, 0f))
        }
        if (!totalSalesResponse.data.isNullOrEmpty() && totalSalesResponse.data[4].count > 0) {
            entries.add(
                BarEntry(
                    4f,
                    totalSalesResponse.data[4].total
                )
            )
        } else {
            entries.add(BarEntry(4f, 0f))
        }
        if (!totalSalesResponse.data.isNullOrEmpty() && totalSalesResponse.data[5].count > 0) {
            entries.add(
                BarEntry(
                    5f,
                    totalSalesResponse.data[5].total
                )
            )
        } else {
            entries.add(BarEntry(5f, 0f))
        }
        if (!totalSalesResponse.data.isNullOrEmpty() && totalSalesResponse.data[6].count > 0) {
            entries.add(
                BarEntry(
                    6f,
                    totalSalesResponse.data[6].total
                )
            )
        } else {
            entries.add(BarEntry(6f, 0f))
        }
        if (!totalSalesResponse.data.isNullOrEmpty() && totalSalesResponse.data[7].count > 0) {
            entries.add(
                BarEntry(
                    7f,
                    totalSalesResponse.data[7].total
                )
            )
        } else {
            entries.add(BarEntry(7f, 0f))
        }
        if (!totalSalesResponse.data.isNullOrEmpty() && totalSalesResponse.data[8].count > 0) {
            entries.add(
                BarEntry(
                    8f,
                    totalSalesResponse.data[8].total
                )
            )
        } else {
            entries.add(BarEntry(8f, 0f))
        }
        if (!totalSalesResponse.data.isNullOrEmpty() && totalSalesResponse.data[9].count > 0) {
            entries.add(
                BarEntry(
                    9f, totalSalesResponse.data[9].total
                )
            )
        } else {
            entries.add(BarEntry(9f, 0f))
        }
        if (!totalSalesResponse.data.isNullOrEmpty() && totalSalesResponse.data[10].count > 0) {
            entries.add(
                BarEntry(
                    10f, totalSalesResponse.data[10].total
                )
            )
        } else {
            entries.add(BarEntry(10f, 0f))
        }
        if (!totalSalesResponse.data.isNullOrEmpty() && totalSalesResponse.data[11].count > 0) {
            entries.add(
                BarEntry(
                    11f, totalSalesResponse.data[11].total
                )
            )
        } else {
            entries.add(BarEntry(11f, 0f))
        }

        entries.add(BarEntry(12f, null))
        *//* entries.add(BarEntry(12f, 0f))
         entries.add(BarEntry(13f, 0f))
         entries.add(BarEntry(0f, 0f))*//*
        val barDataSet = BarDataSet(entries, "")
        barDataSet.color = Color.rgb(61, 165, 255)
        barDataSet.setDrawValues(true) // set above bar values

        val label = ArrayList<IBarDataSet>()
        val labels: ArrayList<String> = ArrayList()
        val serviceCPUStringList: ArrayList<String> = ArrayList()
        labels.add(0, "Jan")
        labels.add(1, "Feb")
        labels.add(2, "Mar")
        labels.add(3, "Apr")
        labels.add(4, "May")
        labels.add(5, "Jun")
        labels.add(6, "Jul")
        labels.add(7, "Aug")
        labels.add(8, "Sep")
        labels.add(9, "Oct")
        labels.add(10, "Nov")
        labels.add(11, "Dec")
        labels.add(12, "")
        // labels.add(13,"")
        label.add(barDataSet)
        val data = BarData(label)


        // scaling can now only be done on x- and y-axis separately
        // barChart.setPinchZoom(false)
        //  barChart.description.isEnabled = false
        barChart.description = null  // set the description
        barChart.setDrawBarShadow(false)
        barChart.setDrawGridBackground(false)


        val xAxis: XAxis = barChart.xAxis
        xAxis.labelCount = 13
        xAxis.textColor = resources.getColor(R.color.color_pin)
        xAxis.position = XAxisPosition.BOTTOM
        xAxis.setDrawAxisLine(false)
        xAxis.setDrawGridLines(false)
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)//setting String values in Xaxis

        barChart.data = data // set the data and list of lables into chart
        barChart.axisRight.setDrawLabels(false)
        barChart.axisLeft.setDrawLabels(true)
        // barChart.axisLeft.textColor = resources.getColor(R.color.color_pin)

        barChart.axisLeft.setDrawGridLines(false)

        barDataSet.color = resources.getColor(R.color.color_bar_graph)

        barChart.axisRight.setDrawAxisLine(false)
        barChart.axisLeft.setDrawAxisLine(false)
        barChart.animateY(1500)
        barChart.isDoubleTapToZoomEnabled = false

        val mv = XYMarkerView(this, IndexAxisValueFormatter(labels))
        mv.chartView = barChart // For bounds control
        barChart.marker = mv
        for (set in barChart.data.dataSets) {
            // val set =barChart.getData().getDataSets().get(barChart.getData().getDataSets().size-1)
            if (barChart.data.dataSets.indexOf(set) == barChart.data
                    .dataSets.size - 1
            ) {
                set.setDrawValues(!set.isDrawValuesEnabled)
            } else {
                set.setDrawValues(set.isDrawValuesEnabled)
            }
        }

        barChart.invalidate()

        barChart.legend.isEnabled = false
    }*/
}
