package com.payment.payrowapp.paymenthistory

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.payment.payrowapp.R
import com.payment.payrowapp.dataclass.SendURLRequest
import com.payment.payrowapp.dataclass.SendURLResponse
import com.payment.payrowapp.dataclass.SummaryReportRequest
import com.payment.payrowapp.dataclass.SummaryReportResp
import com.payment.payrowapp.invoicerecall.SendReceiptRepository
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.ContextUtils

class SummaryReportViewModel(private val context: Context) : ViewModel() {

    private var summaryResponse = MutableLiveData<SummaryReportResp>()

    fun getData(): MutableLiveData<SummaryReportResp> {
        return summaryResponse
    }

    fun getSummary(context: Context, summaryReportRequest: SummaryReportRequest, sharedPreferenceUtil: SharedPreferenceUtil) {
        if (ContextUtils.isNetworkConnected(context)) {
            SummaryReportRepository.getMutableLiveData(context, summaryReportRequest,sharedPreferenceUtil)
            summaryResponse = SummaryReportRepository.getLiveData()
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
    private var sendURLResponse = MutableLiveData<SendURLResponse>()
}