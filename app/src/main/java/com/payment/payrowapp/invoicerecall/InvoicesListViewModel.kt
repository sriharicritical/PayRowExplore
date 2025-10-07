package com.payment.payrowapp.invoicerecall

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.payment.payrowapp.R
import com.payment.payrowapp.dataclass.*
import com.payment.payrowapp.utils.ContextUtils
import com.payment.payrowapp.utils.LoaderCallback

class InvoicesListViewModel(private val context: Context) : ViewModel() {

    private var response = MutableLiveData<QRCodeResponse>()
    private var dailyReportResponse = MutableLiveData<DailyReportResponse>()

    fun getData(): MutableLiveData<QRCodeResponse> {
        return response
    }

    fun getInvoiceRecall(paymentInvoiceRequest: PaymentInvoiceRequest) {
      //   EnterTransactionIdRepository.getMutableLiveData(context, paymentInvoiceRequest)
        response = EnterTransactionIdRepository.getLiveData()
    }

    fun getDailyReportResponse(invoiceRecallByDatesRequest: InvoiceRecallByDatesRequest,loaderCallback: LoaderCallback) {
        if (ContextUtils.isNetworkConnected(context)) {
            EnterTransactionIdRepository.getDailyReportMutableLiveData(
                context,
                invoiceRecallByDatesRequest, loaderCallback
            )
            dailyReportResponse = EnterTransactionIdRepository.getDailyReportLiveData()
        } else {
            loaderCallback.closeLoader()
            Toast.makeText(
                context,
                context.getString(R.string.internetnotavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun getDailyReportData(): MutableLiveData<DailyReportResponse> {
        return dailyReportResponse
    }
}