package com.payment.payrowapp.paymentlink

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.payment.payrowapp.R
import com.payment.payrowapp.dataclass.*
import com.payment.payrowapp.invoicerecall.SendReceiptRepository
import com.payment.payrowapp.utils.ContextUtils
import com.payment.payrowapp.utils.LoaderCallback

open class PaymentLinkViewModel(private val context: Context) : ViewModel() {

    private var response = MutableLiveData<PaymentLinkResponse>()
    private var sendURLResponse = MutableLiveData<SendURLResponse>()

    fun getData(): MutableLiveData<PaymentLinkResponse> {
        return response
    }

    fun getPaymentLinkResponse(paymentLinkRequest: PaymentLinkRequest,loaderCallback: LoaderCallback) {
        if (ContextUtils.isNetworkConnected(context)) {
            PaymentLinkRepository.getMutableLiveData(context, paymentLinkRequest,loaderCallback)
            response = PaymentLinkRepository.getLiveData()
        } else {
            loaderCallback.closeLoader()
            Toast.makeText(
                context,
                context.getString(R.string.internetnotavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }


    fun sendURLLiveData(): MutableLiveData<SendURLResponse> {
        return sendURLResponse
    }

    fun sendURLDetails(sendURLRequest: SendURLRequest) {
        if (ContextUtils.isNetworkConnected(context)) {
            SendReceiptRepository.sendURLTOMail(context, sendURLRequest)
            sendURLResponse = SendReceiptRepository.sendURLLiveData()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.internetnotavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}