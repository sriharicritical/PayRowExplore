package com.payment.payrowapp.contactpayrow

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.payment.payrowapp.R
import com.payment.payrowapp.dataclass.*
import com.payment.payrowapp.utils.ContextUtils

class ContactUsViewModel(private val context: Context) : ViewModel() {

    private var response = MutableLiveData<ContactUsResponse>()

    fun getData(): MutableLiveData<ContactUsResponse> {
        return response
    }

    fun postContactDetails(contactUsRequest: ContactUsRequest) {
        if (ContextUtils.isNetworkConnected(context)) {
            ContactUsRepository.getMutableLiveData(context, contactUsRequest)
            response = ContactUsRepository.getLiveData()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.internetnotavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }


}