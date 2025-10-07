package com.payment.payrowapp.introduction

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.payment.payrowapp.R
import com.payment.payrowapp.dataclass.DecryptResponse
import com.payment.payrowapp.utils.ContextUtils

class IntroductionViewModel(private val context: Context) : ViewModel() {

    private var response = MutableLiveData<DecryptResponse>()

    fun getData(): MutableLiveData<DecryptResponse> {
        return response
    }

    fun checkIMEIStatusResponse() {
        if (ContextUtils.isNetworkConnected(context)) {
            IntroductionRepo.getMutableLiveData(context)
            response = IntroductionRepo.getLiveData()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.internetnotavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}