package com.payment.payrowapp.dashboard

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DashboardViewModel(private val context: Context) : ViewModel() {

    private var response = MutableLiveData<String>()

    fun getData(): MutableLiveData<String> {
        return response
    }

   /* fun getConfiguration() {
        DashBoardRepo.getMutableLiveData(context)
        response = DashBoardRepo.getLiveData()
    }*/


}