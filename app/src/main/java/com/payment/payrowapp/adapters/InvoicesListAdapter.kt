package com.payment.payrowapp.adapters

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.payment.payrowapp.R
import com.payment.payrowapp.dataclass.DailyReportResponse
import com.payment.payrowapp.dialogs.OnItemClickListener
import com.payment.payrowapp.generateqrcode.ECommVOIDRFActivity
import com.payment.payrowapp.generateqrcode.GenerateQRCodeReceiptActivity
import com.payment.payrowapp.newpayment.CardReceiptActivity
import com.payment.payrowapp.newpayment.PaymentSuccessfulActivity
import com.payment.payrowapp.refundandreversal.VoidRFReceiptActivity
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.Constants
import com.payment.payrowapp.utils.ContextUtils

class InvoicesListAdapter(private val ring: MediaPlayer?,
                          val context: Context,
                          private var qrCodeResponse: DailyReportResponse, private val listener: OnItemClickListener
) : RecyclerView.Adapter<InvoicesListAdapter.MyViewHolder>() {

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var txtDate: TextView = view.findViewById(R.id.txtDate)
        var txtAmount: TextView = view.findViewById(R.id.txtAmount)
        var txtTransNo: TextView = view.findViewById(R.id.txtTransNo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_invoice_list, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val itemsList = qrCodeResponse.data[position]
        holder.txtDate.text = itemsList.timeField.substring(
            0,
            5
        ) + "," + ContextUtils.formatShortDateTime(
            itemsList.paymentDate.substring(
                0,
                10
            )
        )  //itemsList.paymentDate.substring(5, 10)

        if (itemsList.checkoutStatus != null) {
            if (itemsList.checkoutStatus == "CAPTURED") {
                holder.txtTransNo.text = context.getText(R.string.success)
                holder.txtTransNo.setTextColor(context.getColor(R.color.color_green))
            } else if (itemsList.checkoutStatus == "NOT REFUNDED"|| itemsList.checkoutStatus == "NOT VOIDED" ||itemsList.checkoutStatus == "Pending" || itemsList.checkoutStatus.equals(
                    "OPEN",
                    ignoreCase = true
                ) ||
                itemsList.checkoutStatus == "NOT CAPTURED" || itemsList.checkoutStatus == "Cancelled" ||
                itemsList.checkoutStatus.equals(
                    "DENIED BY RISK",
                    ignoreCase = true
                ) || itemsList.checkoutStatus.equals(
                    "HOST TIMEOUT",
                    ignoreCase = true
                ) || itemsList.checkoutStatus.equals(
                    "NOT APPROVED",
                    ignoreCase = true
                ) || itemsList.checkoutStatus.equals(
                    "NOT CAPTURED",
                    ignoreCase = true
                ) || itemsList.checkoutStatus.equals(
                    "CLOSED",
                    ignoreCase = true
                ) || itemsList.checkoutStatus.equals(
                    "CANCELED",
                    ignoreCase = true
                )
            ) {
                holder.txtTransNo.text = context.getText(R.string.decline)
                holder.txtTransNo.setTextColor(context.getColor(R.color.colorAccent))
            } else if (itemsList.checkoutStatus.equals(
                    "INPROGRESS",
                    ignoreCase = true
                ) || itemsList.checkoutStatus.equals(
                    "CREATED",
                    ignoreCase = true
                )
            ) {
                if (itemsList.channel!=Constants.CARD) {
                    holder.txtTransNo.text = "IN PROGRESS"//context.getText(R.string.in_progress)
                    holder.txtTransNo.setTextColor(context.getColor(R.color.color_orange))
                } else {
                    holder.txtTransNo.text = context.getText(R.string.decline)
                    holder.txtTransNo.setTextColor(context.getColor(R.color.colorAccent))
                }
            } else {
                when (itemsList.checkoutStatus) {
                    "REFUNDED" -> {
                        holder.txtTransNo.text = "REFUND"
                    }
                    "VOIDED" -> {
                        holder.txtTransNo.text = "VOID"
                    }
                    else -> {
                        holder.txtTransNo.text = itemsList.checkoutStatus
                    }
                }
                val typeface = ResourcesCompat.getFont(context, R.font.roboto_medium)
                holder.txtTransNo.typeface = typeface
                holder.txtTransNo.setTextColor(context.getColor(R.color.color_text))
            }
        } else {
            holder.txtTransNo.text = context.getText(R.string.decline)
            holder.txtTransNo.setTextColor(context.getColor(R.color.colorAccent))
        }

        val itemAmount: String = if (itemsList.channel == Constants.CARD) {
            itemsList.PartialApprovedAmount ?: itemsList.totamnt
        } else {
            itemsList.totamnt
        }

        if (itemAmount.length > 10) {
            holder.txtAmount.text = ContextUtils.formatWithCommas(itemAmount.toDouble())//itemAmount.substring(0, 10)
        } else {
            holder.txtAmount.text = ContextUtils.formatWithCommas(itemAmount.toDouble())//itemAmount
        }

        /* if (itemsList.orderNumber.length > 10) {
             holder.txtTransNo.text = itemsList.orderNumber.substring(
                 itemsList.orderNumber.length - 10,
                 itemsList.orderNumber.length
             )
         } else {
             holder.txtTransNo.text = itemsList.orderNumber
         }*/
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
        holder.itemView.setOnClickListener {
            ring?.start()
            when (itemsList.channel) {
                "Paybylink", "generateQR" -> {

                    val sendBundle = Bundle()
                    sendBundle.putString(Constants.DATE, itemsList.paymentDate.substring(0, 10))
                    sendBundle.putString("Time", itemsList.paymentDate.substring(11, 19))
                    sendBundle.putString("Amount", itemsList.totalAmount)
                    sendBundle.putString("orderNumber", itemsList.orderNumber)
                    sendBundle.putString("status", itemsList.checkoutStatus)
                    sendBundle.putString("cardNumber", itemsList.cardNumber)
                    sendBundle.putString("cardBrand", itemsList.cardBrand)
                    sendBundle.putString("totalAmount", itemsList.amount)
                    sendBundle.putString("type", "invoiceRecall")
                    sendBundle.putString("channel", itemsList.channel)
                    sendBundle.putString("auth", itemsList.auth)

                    itemsList.vatAmount?.let { it1 -> sendBundle.putFloat("payRowVATAmount", it1) }
                    itemsList.vatStatus?.let { it1 ->
                        sendBundle.putBoolean(
                            "payRowVATStatus",
                            it1
                        )
                    }

                    when (itemsList.recordType) {
                        "Refund Order" -> {
                            sendBundle.putString("mode", "Refund")
                            context.startActivity(
                                Intent(
                                    context,
                                    ECommVOIDRFActivity::class.java
                                ).putExtras(sendBundle)
                            )
                        }
                        "Voided" -> {
                            sendBundle.putString("mode", "Void")
                            context.startActivity(
                                Intent(
                                    context,
                                    ECommVOIDRFActivity::class.java
                                ).putExtras(sendBundle)
                            )
                        }
                        else -> {
                            if (itemsList.inquiryStatus != true && (itemsList.checkoutStatus.equals(
                                    "NOT APPROVED",
                                    ignoreCase = true
                                ) || itemsList.checkoutStatus.equals(
                                    "PRESENTED",
                                    ignoreCase = true
                                ) || itemsList.checkoutStatus.equals(
                                    "DENIED BY RISK",
                                    ignoreCase = true
                                ) || itemsList.checkoutStatus.equals(
                                    "HOST TIMEOUT",
                                    ignoreCase = true
                                ) || itemsList.checkoutStatus.equals(
                                    "CLOSED",
                                    ignoreCase = true
                                ) || itemsList.checkoutStatus.equals(
                                    "CANCELED",
                                    ignoreCase = true
                                ) || itemsList.checkoutStatus.equals(
                                    "NOT CAPTURED",
                                    ignoreCase = true
                                ))
                            ) {
                                listener.onItemClicked(itemsList.orderNumber)
                            } else {
                                context.startActivity(
                                    Intent(
                                        context,
                                        GenerateQRCodeReceiptActivity::class.java
                                    ).putExtras(sendBundle)
                                )
                            }
                        }
                    }
                }
                Constants.CASH -> {

                    val sendBundle = Bundle()
                    sendBundle.putString(Constants.DATE, itemsList.paymentDate.substring(0, 10))
                    sendBundle.putString("Time", itemsList.paymentDate.substring(11, 19))
                    sendBundle.putString("status", itemsList.checkoutStatus)
                    sendBundle.putString("CashReceived", itemsList.cashReceived)
                    sendBundle.putString("Balance", itemsList.balance)
                    sendBundle.putString("Amount", itemsList.totalAmount)
                    sendBundle.putString("receiptNo", itemsList.receiptNo)
                    sendBundle.putString(
                        "TotalAmount",
                        ContextUtils.splitDecimal(itemsList.amount.toFloat())
                    )
                    // sendBundle.putString(Constants.DATE, itemsList.paymentDate.substring(0, 10))
                    sendBundle.putString("InvoiceNo", itemsList.orderNumber)
                    sendBundle.putString("type", "invoiceRecall")
                    itemsList.vatAmount?.let { it1 -> sendBundle.putFloat("payRowVATAmount", it1) }
                    itemsList.vatStatus?.let { it1 ->
                        sendBundle.putBoolean(
                            "payRowVATStatus",
                            it1
                        )
                    }
                    context.startActivity(
                        Intent(
                            context,
                            PaymentSuccessfulActivity::class.java
                        ).putExtras(sendBundle)
                    )
                }
                Constants.CARD -> {

                    val sharedPreferenceUtil = SharedPreferenceUtil(context)
                    val bundle = Bundle()
                    bundle.putString("INVOICENO", itemsList.orderNumber)
                    //  bundle.putString(Constants.TOTAL_AMOUNT, itemsList.totalAmount)
                    //bundle.putString(Constants.ADDRESS, itemsList.customerBillingCountry)
                    // bundle.putString(Constants.NAME_OF_THE_BUSINESS, itemsList.customerName)
                    //  bundle.putString(Constants.TOTAL_TAX, itemsList.totalTaxAmount)
                    //  bundle.putString(Constants.MERCHANT_ID, itemsList.mainMerchantId)
                    bundle.putString(Constants.DATE, itemsList.paymentDate.substring(0, 10))
                    bundle.putString("Time", itemsList.paymentDate.substring(11, 19))
                    //  bundle.putString(Constants.TRANSACTION_TYPE, itemsList.channel)
                    bundle.putString("CARDNO", itemsList.cardNumber)
                    bundle.putString("hostRefNO", itemsList.hostReference)
                    bundle.putString("status", itemsList.checkoutStatus)
                    bundle.putString("authCode", itemsList.authorizationId)
                    bundle.putString("type", "invoiceRecall")
                    bundle.putString("cardType", itemsList.cardType)
                    bundle.putString("CardBrand", itemsList.cardBrand)

                    bundle.putBoolean("SignatureStatus", itemsList.SignatureStatus)
                    bundle.putString("panSequenceNo", itemsList.cardsequencenumber)
                    itemsList.vatAmount?.let { it1 -> bundle.putFloat("payRowVATAmount", it1) }
                    itemsList.vatStatus?.let { it1 -> bundle.putBoolean("payRowVATStatus", it1) }

                    bundle.putString("totalAmount", itemAmount)
                    bundle.putString("payRowDigitFee", itemsList.secondaryCharges)


                    if (itemsList.recordType == "Refund Order" || itemsList.recordType == "Refund" || itemsList.recordType == "Void") {
                        bundle.putString("mode", itemsList.recordType)

                        if (itemsList.checkoutStatus == "NOT REFUNDED" || itemsList.checkoutStatus == "NOT VOIDED") {
                            val responseMessage =
                                ContextUtils.responseMessage(itemsList.responseCode)
                            bundle.putInt("responseMessage", responseMessage!!)
                        }
                        context.startActivity(
                            Intent(context, VoidRFReceiptActivity::class.java).putExtras(bundle)
                        )
                    } else {
                        if (itemsList.vatAmount != null) {
                            val serviceCharges =
                                itemAmount.toFloat() - itemsList.totalServicesAmount?.toFloat()!! - itemsList.secondaryCharges!!.toFloat() - itemsList.vatAmount!!
                            bundle.putString(
                                "surcharges",
                                ContextUtils.splitDecimal(serviceCharges)
                            )
                        } else {
                            val serviceCharges =
                                itemAmount.toFloat() - itemsList.totalServicesAmount?.toFloat()!! - itemsList.secondaryCharges!!.toFloat()
                            bundle.putString(
                                "surcharges",
                                ContextUtils.splitDecimal(serviceCharges)
                            )
                        }

                        sharedPreferenceUtil.setAID(itemsList.AID)
                        sharedPreferenceUtil.setAC(itemsList.AC)
                        sharedPreferenceUtil.setACInfo(itemsList.AC_INFO)
                        sharedPreferenceUtil.setTVR(itemsList.TVR)
                        sharedPreferenceUtil.setTransactionType(itemsList.TRANSACTION_TYPE)

                        if (itemsList.responseCode != null && itemsList.checkoutStatus == "NOT CAPTURED") {
                            val responseMessage =
                                ContextUtils.responseMessage(itemsList.responseCode)
                            bundle.putInt("responseMessage", responseMessage!!)
                        }

                        context.startActivity(
                            Intent(context, CardReceiptActivity::class.java).putExtras(
                                bundle
                            )
                        )
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return qrCodeResponse.data.size
    }

}