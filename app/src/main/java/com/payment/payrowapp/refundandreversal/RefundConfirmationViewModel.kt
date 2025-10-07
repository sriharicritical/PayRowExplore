package com.payment.payrowapp.refundandreversal

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.payment.payrowapp.R
import com.payment.payrowapp.dataclass.OrderResponse
import com.payment.payrowapp.dataclass.RefOrderRequest
import com.payment.payrowapp.utils.ContextUtils

class RefundConfirmationViewModel(private val context: Context) : ViewModel() {
    var orderResponse = MutableLiveData<OrderResponse>()

    fun addOrder(context: Context, orderRequest: RefOrderRequest) {
        if (ContextUtils.isNetworkConnected(context)) {
        RefundRepository.getOrderMutableLiveData(context, orderRequest)
        orderResponse = RefundRepository.getOrderLiveData()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.internetnotavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}