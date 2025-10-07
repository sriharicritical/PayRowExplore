package com.payment.payrowapp.newpayment

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.payment.payrowapp.R
import com.payment.payrowapp.dataclass.*
import com.payment.payrowapp.invoicerecall.SendReceiptRepository
import com.payment.payrowapp.utils.ContextUtils

class PaymentSuccessfulActivityViewModel(private val context: Context) : ViewModel() {

    private var response = MutableLiveData<PDFReceiptResponse>()
    private var sendURLResponse = MutableLiveData<SendURLResponse>()

    fun getPDFLinkLiveData(): MutableLiveData<PDFReceiptResponse> {
        return response
    }

    fun getPDFLinkDetails(invoiceNumber: String) {
        if (ContextUtils.isNetworkConnected(context)) {
            SendReceiptRepository.getPDFReceiptData(context, invoiceNumber)
            response = SendReceiptRepository.getLiveData()
        } else {
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