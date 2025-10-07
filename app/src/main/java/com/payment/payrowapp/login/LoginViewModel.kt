package com.payment.payrowapp.login

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.payment.payrowapp.R
import com.payment.payrowapp.dataclass.DecryptResponse
import com.payment.payrowapp.dataclass.LoginRequest
import com.payment.payrowapp.utils.ContextUtils

open class LoginViewModel(private val context: Context) : ViewModel() {

    private var response = MutableLiveData<DecryptResponse>()

    fun getData(): MutableLiveData<DecryptResponse>{
        return response
    }

    fun getLoginResponse(loginRequest: LoginRequest) {
        if (ContextUtils.isNetworkConnected(context)) {
            LoginRepository.getMutableLiveData(context, loginRequest)
            response = LoginRepository.getLiveData()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.internetnotavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}