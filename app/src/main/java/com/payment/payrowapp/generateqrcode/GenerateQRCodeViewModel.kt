package com.payment.payrowapp.generateqrcode

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.payment.payrowapp.R
import com.payment.payrowapp.dataclass.EnquiryRequestClass
import com.payment.payrowapp.dataclass.EnquiryResponseDataClass
import com.payment.payrowapp.dataclass.GenerateQRRequest
import com.payment.payrowapp.dataclass.QRResponse
import com.payment.payrowapp.utils.ContextUtils

class GenerateQRCodeViewModel(private val context: Context) : ViewModel() {

    private var response = MutableLiveData<QRResponse>()

    fun getData(): MutableLiveData<QRResponse> {
        return response
    }

    fun getQRResponse(generateQRRequest: GenerateQRRequest) {
        if (ContextUtils.isNetworkConnected(context)) {
            GenarateQRCodeRepository.getMutableLiveData(context, generateQRRequest)
            response = GenarateQRCodeRepository.getLiveData()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.internetnotavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private var enquiryResponse = MutableLiveData<EnquiryResponseDataClass>()

    fun getEnquiryData(): MutableLiveData<EnquiryResponseDataClass> {
        return enquiryResponse
    }

    fun getEnquiryResponse(enquiryRequestClass: EnquiryRequestClass, onResult: ((Boolean) -> Unit)? = null) {
        if (ContextUtils.isNetworkConnected(context)) {
            GenarateQRCodeRepository.getEnquiryMutableLiveData(context, enquiryRequestClass) { success ->
                onResult?.invoke(success)
            }
            enquiryResponse = GenarateQRCodeRepository.getEnquiryLiveData()
        } else {
            onResult?.invoke(true)
            Toast.makeText(
                context,
                context.getString(R.string.internetnotavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}