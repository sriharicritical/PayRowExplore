package com.payment.payrowapp.login

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.payment.payrowapp.R
import com.payment.payrowapp.dataclass.CreatePINRequest
import com.payment.payrowapp.dataclass.DecryptResponse
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.ContextUtils


class CreatePinViewModelClass(private val context: Context) : ViewModel() {

    private var response = MutableLiveData<DecryptResponse>()

    fun getData(): MutableLiveData<DecryptResponse> {
        return response
    }

    fun getPINResponse(
        createPINRequest: CreatePINRequest,
        sharedPreferenceUtil: SharedPreferenceUtil,
        loginActivity: LoginActivity
    ) {
        if (ContextUtils.isNetworkConnected(context)) {
            CreatePINRepository.getMutableLiveData(context, createPINRequest,sharedPreferenceUtil,loginActivity)
            response = CreatePINRepository.getLiveData()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.internetnotavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}