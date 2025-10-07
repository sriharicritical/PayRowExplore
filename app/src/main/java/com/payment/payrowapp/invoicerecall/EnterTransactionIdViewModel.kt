package com.payment.payrowapp.invoicerecall

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.payment.payrowapp.dataclass.PaymentInvoiceRequest
import com.payment.payrowapp.dataclass.QRCodeResponse
import com.payment.payrowapp.utils.LoaderCallback

class EnterTransactionIdViewModel(private val context: Context) : ViewModel() {

    private var response = MutableLiveData<QRCodeResponse>()

    fun getData(): MutableLiveData<QRCodeResponse> {
        return response
    }

    fun getInvoiceRecall(paymentInvoiceRequest: PaymentInvoiceRequest,loaderCallback: LoaderCallback) {
        EnterTransactionIdRepository.getMutableLiveData(context, paymentInvoiceRequest,loaderCallback)
        response = EnterTransactionIdRepository.getLiveData()
    }
}