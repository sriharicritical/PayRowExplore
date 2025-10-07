package com.payment.payrowapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.payment.payrowapp.R
import com.payment.payrowapp.dataclass.BarCodeItem
import java.util.ArrayList

class BarItemAdapter(
    val context: Context,
    val barCodeItemList: ArrayList<BarCodeItem>
) : RecyclerView.Adapter<BarItemAdapter.MyViewHolder>() {

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvProductLabel: TextView = view.findViewById(R.id.tvProductLabel)
        var tvPrice: TextView = view.findViewById(R.id.tvPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.product_list_item_layout, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val productDetails = barCodeItemList[position]
        if (productDetails.transactionAmount > 0) {
            holder.tvPrice.text = productDetails.transactionAmount.toString()
        } else {
            holder.tvPrice.text = "Inc"
        }

        holder.tvProductLabel.text =
            productDetails.shortServiceName
    }

    override fun getItemCount(): Int {
        return barCodeItemList.size
    }
}