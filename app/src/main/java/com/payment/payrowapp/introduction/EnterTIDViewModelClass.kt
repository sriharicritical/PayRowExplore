package com.payment.payrowapp.introduction

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.payment.payrowapp.R
import com.payment.payrowapp.dataclass.DecryptResponse
import com.payment.payrowapp.dataclass.VerifyDeviceRequest
import com.payment.payrowapp.dataclass.VerifyDeviceResponse
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.ContextUtils

class EnterTIDViewModelClass(private val context: Context) : ViewModel() {

    private var response = MutableLiveData<VerifyDeviceResponse>()

    fun getData(): MutableLiveData<VerifyDeviceResponse> {
        return response
    }

    fun verifyDeviceResponse(sharedPreferenceUtil: SharedPreferenceUtil, verifyDeviceRequest: VerifyDeviceRequest) {
        if (ContextUtils.isNetworkConnected(context)) {
            EnterTIDRepository.getMutableLiveData(context, verifyDeviceRequest,sharedPreferenceUtil)
            response = EnterTIDRepository.getLiveData()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.internetnotavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private var initResponse = MutableLiveData<DecryptResponse>()

    fun getInitData(): MutableLiveData<DecryptResponse> {
        return initResponse
    }

    fun initKeyResponse(sharedPreferenceUtil: SharedPreferenceUtil) {
        if (ContextUtils.isNetworkConnected(context)) {
            EnterTIDRepository.getKeyMutableLiveData(context,sharedPreferenceUtil)
            initResponse = EnterTIDRepository.getInitKeyLiveData()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.internetnotavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}