package com.payment.payrowapp.paymenthistory

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.payment.payrowapp.dataclass.TotalAmountResponse
import com.payment.payrowapp.dataclass.TotalReportResponse
import com.payment.payrowapp.dataclass.TotalSalesResponse

open class PaymentHistoryViewModel(private val context: Context) : ViewModel() {

    private var totalAmountResponse = MutableLiveData<TotalAmountResponse>()
    private var totalSalesResponse = MutableLiveData<TotalSalesResponse>()
    private var totalReportResponse = MutableLiveData<TotalReportResponse>()

    fun getTotalAmountData(): MutableLiveData<TotalAmountResponse> {
        return totalAmountResponse
    }

    fun getTotalAmountResponse(userId: String) {
        PaymentHistoryRepository.getTotalAmountMutableLiveData(context, userId)
        totalAmountResponse = PaymentHistoryRepository.getTotalAmountLiveData()
    }

    fun getTotalSalesData(): MutableLiveData<TotalSalesResponse> {
        return totalSalesResponse
    }

    fun getTotalSalesResponse(userId: String) {
        PaymentHistoryRepository.getTotalSalesMutableLiveData(context, userId)
        totalSalesResponse = PaymentHistoryRepository.getTotalSalesLiveData()
    }

    fun getTotalReportData(): MutableLiveData<TotalReportResponse> {
        return totalReportResponse
    }

    fun getTotalReportResponse(userId: String) {
        PaymentHistoryRepository.getTotalReportMutableLiveData(context, userId)
        totalReportResponse = PaymentHistoryRepository.getTotalReportLiveData()
    }
}