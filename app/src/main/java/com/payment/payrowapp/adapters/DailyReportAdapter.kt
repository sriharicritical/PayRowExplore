package com.payment.payrowapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.payment.payrowapp.R
import com.payment.payrowapp.dataclass.DailyReportResponse
import com.payment.payrowapp.utils.Constants
import com.payment.payrowapp.utils.ContextUtils


class DailyReportAdapter(
    val context: Context,
    private var totalReportResponse: DailyReportResponse
) : RecyclerView.Adapter<DailyReportAdapter.MyViewHolder>() {

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var txtTime: TextView = view.findViewById(R.id.txtTime)
        var txtTransNo: TextView = view.findViewById(R.id.txtTransNo)
        var txtValue: TextView = view.findViewById(R.id.txtValue)
        var txtStatus: TextView = view.findViewById(R.id.txtStatus)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_daily_report, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val totalReportListItem = totalReportResponse.data[position]

        /* val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
         val output = SimpleDateFormat("h:mm a")
         val date = dateFormat.parse(totalReportListItem.paymentDate)
         var time: String = output.format(date)*/
        if (!totalReportListItem.timeField.isNullOrEmpty()) {
            holder.txtTime.text = totalReportListItem.timeField.substring(0,5)+","+  ContextUtils.formatShortDateTime(totalReportListItem.paymentDate.substring(0,10))//totalReportListItem.paymentDate.substring(5, 10)
        }

        val itemAmount: String = if (totalReportListItem.channel == Constants.CARD) {
            totalReportListItem.PartialApprovedAmount ?: totalReportListItem.totamnt
        } else {
            totalReportListItem.totamnt
        }

        if (itemAmount.isNotEmpty()) {
            holder.txtValue.text = ContextUtils.formatWithCommas(itemAmount.toDouble())//itemAmount
        }

        if (totalReportListItem.orderNumber.length > 10) {
            holder.txtTransNo.text = totalReportListItem.orderNumber.substring(
                totalReportListItem.orderNumber.length - 10,
                totalReportListItem.orderNumber.length
            )
        } else {
            holder.txtTransNo.text = totalReportListItem.orderNumber
        }

        if (totalReportListItem.checkoutStatus.equals("APPROVED", ignoreCase = true) ||totalReportListItem.checkoutStatus.equals("REFUNDED", ignoreCase = true) || totalReportListItem.checkoutStatus.equals("VOIDED", ignoreCase = true) ||totalReportListItem.checkoutStatus.equals("CAPTURED", ignoreCase = true) ||totalReportListItem.checkoutStatus.equals("PARTIAL APPROVED", ignoreCase = true)) {
            holder.txtStatus.text = context.getString(R.string.success)
        } else if (totalReportListItem.checkoutStatus.equals("Pending", ignoreCase = true)) {
            holder.txtStatus.text = "CANCEL"
        } else {
            holder.txtStatus.text = context.getString(R.string.decline)//totalReportListItem.checkoutStatus
        }


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
    }

    override fun getItemCount(): Int {
        return totalReportResponse.data.size
    }

}