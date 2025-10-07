package com.payment.payrowapp.paymenthistory

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.payment.payrowapp.R
import com.payment.payrowapp.dataclass.*
import com.payment.payrowapp.utils.ContextUtils

open class InstantReportViewModel(private val context: Context) : ViewModel() {

    private var totalReportResponse = MutableLiveData<TotalReportResponse>()
    private var dailyReportResponse = MutableLiveData<DailyReportResponse>()
    private var monthlyReportResponse = MutableLiveData<MonthlyReportResponse>()

    fun getTotalReportData(): MutableLiveData<TotalReportResponse> {
        return totalReportResponse
    }

    fun getTotalReportResponse(userId: String) {
        PaymentHistoryRepository.getTotalReportMutableLiveData(context, userId)
        totalReportResponse = PaymentHistoryRepository.getTotalReportLiveData()
    }

    fun getDailyReportResponse(dailyReportRequest: DailyReportRequest) {
        if (ContextUtils.isNetworkConnected(context)) {
            PaymentHistoryRepository.getDailyReportMutableLiveData(context, dailyReportRequest)
            dailyReportResponse = PaymentHistoryRepository.getDailyReportLiveData()
        } else {
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

    fun getMonthlyReportResponse(year: Int, channel: String) {
        if (ContextUtils.isNetworkConnected(context)) {
            PaymentHistoryRepository.getMonthlyReportMutableLiveData(context, year, channel)
            monthlyReportResponse = PaymentHistoryRepository.getMonthlyReportLiveData()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.internetnotavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun getMonthlyReportData(): MutableLiveData<MonthlyReportResponse> {
        return monthlyReportResponse
    }

}