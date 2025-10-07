package com.payment.payrowapp.newpayment

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.payment.payrowapp.R
import com.payment.payrowapp.dataclass.*
import com.payment.payrowapp.utils.ContextUtils

class EnterPinActivityViewModel(private val context: Context) : ViewModel() {

    private var orderResponse = MutableLiveData<OrderResponse>()

    fun getOrderData(): MutableLiveData<OrderResponse> {
        return orderResponse
    }

    fun addOrder(
        context: Context, orderRequest: OrderRequest, paymentType: String?,
        bankTransferURL: String?
    ) {
        if (ContextUtils.isNetworkConnected(context)) {
            CardReceiptActivityRepository.getOrderMutableLiveData(context, orderRequest,paymentType,bankTransferURL)
            orderResponse = CardReceiptActivityRepository.getOrderLiveData()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.internetnotavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }

   /* private fun postQR(context: Context, jsonObject: OrderResponse, url: String) {
        if (ContextUtils.isNetworkConnected(context)) {
            EnterAmountToPayCashRepository.postQRResponseMutableLiveData(context, jsonObject, url)
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.internetnotavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }*/
}