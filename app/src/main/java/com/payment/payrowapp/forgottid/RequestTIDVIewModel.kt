package com.payment.payrowapp.forgottid

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.payment.payrowapp.R
import com.payment.payrowapp.dataclass.ReqForTIDResp
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.ContextUtils


class RequestTIDVIewModel(private val context: Context) : ViewModel() {

    private var response = MutableLiveData<ReqForTIDResp>()

    fun getData(): MutableLiveData<ReqForTIDResp> {
        return response
    }

    fun requestTIDResponse(email: String, mobileNumber: String,sharedPreferenceUtil: SharedPreferenceUtil) {
        if (ContextUtils.isNetworkConnected(context)) {
            RequestTIDRepo.getMutableLiveData(context, email, mobileNumber,sharedPreferenceUtil)
            response = RequestTIDRepo.getLiveData()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.internetnotavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}