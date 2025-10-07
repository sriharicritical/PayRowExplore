package com.payment.payrowapp.sunmipay

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.payment.payrowapp.R
import com.payment.payrowapp.dataclass.Product
import com.payment.payrowapp.utils.ContextUtils

import java.util.ArrayList

class ProductAdapter(
    val context: Context,
    val qrCodeResponse: ArrayList<Product>
) : RecyclerView.Adapter<ProductAdapter.MyViewHolder>() {

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
        val productDetails = qrCodeResponse[position]
        if (productDetails.transactionAmount > 0) {
            holder.tvPrice.text = ContextUtils.formatWithCommas(productDetails.transactionAmount)//productDetails.transactionAmount.toString()
        } else {
            holder.tvPrice.text = "Inc"
        }

        holder.tvProductLabel.text =
            productDetails.shortServiceName
    }

    override fun getItemCount(): Int {
        return qrCodeResponse.size
    }
}