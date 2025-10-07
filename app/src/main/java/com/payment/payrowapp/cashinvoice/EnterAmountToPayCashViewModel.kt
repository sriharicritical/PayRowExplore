package com.payment.payrowapp.cashinvoice

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.payment.payrowapp.R
import com.payment.payrowapp.dataclass.OrderRequest
import com.payment.payrowapp.dataclass.OrderResponse
import com.payment.payrowapp.utils.ContextUtils

class EnterAmountToPayCashViewModel(private val context: Context) : ViewModel() {

    private var orderResponse = MutableLiveData<OrderResponse>()

    fun getOrderData(): MutableLiveData<OrderResponse> {
        return orderResponse
    }

    fun addOrder(context: Context, orderRequest: OrderRequest) {
        if (ContextUtils.isNetworkConnected(context)) {
            EnterAmountToPayCashRepository.getOrderMutableLiveData(context, orderRequest)
            orderResponse = EnterAmountToPayCashRepository.getOrderLiveData()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.internetnotavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun postQR(context: Context, jsonObject: OrderResponse, url: String) {
        if (ContextUtils.isNetworkConnected(context)) {
            EnterAmountToPayCashRepository.postQRResponseMutableLiveData(context, jsonObject, url)
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.internetnotavailable),
                Toast.LENGTH_LONG
            ).show()
        }
        //  orderResponse = EnterAmountToPayCashRepository.getOrderLiveData()
    }

}