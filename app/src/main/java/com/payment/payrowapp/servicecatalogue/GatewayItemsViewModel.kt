package com.payment.payrowapp.servicecatalogue

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.payment.payrowapp.R
import com.payment.payrowapp.dataclass.ServicesResponse
import com.payment.payrowapp.utils.ContextUtils


class GatewayItemsViewModel(private val context: Context) : ViewModel() {

    private var response = MutableLiveData<ServicesResponse>()

    fun getAddItemLiveData(): MutableLiveData<ServicesResponse> {
        return response
    }

    fun getAddItemDetails() {
        if (ContextUtils.isNetworkConnected(context)) {
            GatewayItemsRepository.getAddItemData(context)
            response = GatewayItemsRepository.getLiveData()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.internetnotavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }

}