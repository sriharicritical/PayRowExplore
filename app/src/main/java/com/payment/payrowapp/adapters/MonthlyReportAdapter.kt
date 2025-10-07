package com.payment.payrowapp.adapters

import android.content.Context
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.payment.payrowapp.R
import com.payment.payrowapp.dataclass.*
import com.payment.payrowapp.paymenthistory.DownloadReportDialog
import com.payment.payrowapp.paymenthistory.InstantReportViewModel
import com.payment.payrowapp.paymenthistory.MonthlyReportDetailActivity
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.ContextUtils

class
MonthlyReportAdapter(
    private val ring: MediaPlayer?,
    val activity: MonthlyReportDetailActivity,
    val context: Context,
    private var totalReportResponse: MonthlyReportResponse,
    val year: Int,
    private var channel: String,
    var instantReportViewModel: InstantReportViewModel
) : RecyclerView.Adapter<MonthlyReportAdapter.MyViewHolder>() {

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var txtDay: TextView = view.findViewById(R.id.txtDay)
        var txtValue: TextView = view.findViewById(R.id.txtValue)
        var txtTotalIncome: TextView = view.findViewById(R.id.txtTotalIncome)
        var ivDownload: ImageView = view.findViewById(R.id.ivDownload)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_monthly_report, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val totalReportListItem = totalReportResponse.data[position]

        holder.txtDay.text = totalReportListItem.month//.substring(0, 10)
        holder.txtValue.text = ContextUtils.formatWithCommas(totalReportListItem.credit.toDouble())//totalReportListItem.credit.toString()
        holder.txtTotalIncome.text = totalReportListItem.count.toString()

        if (position % 2 == 1) {
            holder.itemView.setBackgroundColor(
                context.resources.getColor(
                    R.color.color_item_bg,
                    null
                )
            )
        } else {
            holder.itemView.setBackgroundColor(context.resources.getColor(R.color.white, null))
        }

        holder.ivDownload.setOnClickListener {
            ring?.start()
            val fromDate = "$year-${totalReportListItem.monthnumber}-02"
            // val dates = Dates(fromDate)
            val sharedPreferenceUtil = SharedPreferenceUtil(context)
            val merchantID = sharedPreferenceUtil.getMerchantID()
            val tid = sharedPreferenceUtil.getTerminalID()

            if (totalReportListItem.count > 0) {
                val reportDate = ReportDate(fromDate, null)
                val sendTIDReportReq =
                    SendTIDReportReq(
                        "montly", channel, reportDate, merchantID, tid, null, true,
                        sharedPreferenceUtil.getGatewayMerchantID()
                    )
                DownloadReportDialog(sendTIDReportReq, context).show()
            } else {
                Toast.makeText(context, "There is NO Reports for Download", Toast.LENGTH_SHORT)
                    .show()
            }

            /*val jsonObject = JSONObject()
            jsonObject.put("num", ContextUtils.randomValue())
            jsonObject.put("validation", "Key Validation")
            val encodedString =
                Base64.encodeToString(jsonObject.toString().toByteArray(), Base64.DEFAULT)
            val dailyReportRequest =
                DailyReportRequest("montly", channel, dates, merchantID, encodedString, tid)
            instantReportViewModel.getDailyReportResponse(dailyReportRequest)

            instantReportViewModel.getDailyReportData().observe(activity) {
                progressDialog.cancel()
                if (it != null) {
                    DownloadReportDialog(sendTIDReportReq, context).show()
                } else {
                    Toast.makeText(context, "There is NO Reports for Download", Toast.LENGTH_SHORT)
                        .show()
                }
            }*/
        }
    }

    override fun getItemCount(): Int {
        return totalReportResponse.data.size
    }

}