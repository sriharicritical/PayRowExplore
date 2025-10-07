package com.payment.payrowapp.product

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.payment.payrowapp.R
import com.payment.payrowapp.dataclass.FeePrepareRequest
import com.payment.payrowapp.dataclass.FeePrepareResponse
import com.payment.payrowapp.utils.ContextUtils

class ProductViewModel(private val context: Context) : ViewModel() {

    private var response = MutableLiveData<FeePrepareResponse>()

    fun getData(): MutableLiveData<FeePrepareResponse> {
        return response
    }

    fun getFeeResponse(feePrepareRequest: FeePrepareRequest) {
        if (ContextUtils.isNetworkConnected(context)) {
            ProductRepository.getFeePrepareAPI(context, feePrepareRequest)
            response = ProductRepository.getFeeResponse()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.internetnotavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}