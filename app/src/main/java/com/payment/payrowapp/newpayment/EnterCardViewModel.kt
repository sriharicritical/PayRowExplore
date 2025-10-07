package com.payment.payrowapp.newpayment

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.payment.payrowapp.R
import com.payment.payrowapp.dataclass.DecryptResponse
import com.payment.payrowapp.utils.ContextUtils

class EnterCardViewModel(context: Context) : ViewModel() {

    private var terminalResponse = MutableLiveData<DecryptResponse>()

    fun getTerminalData(): MutableLiveData<DecryptResponse> {
        return terminalResponse
    }

    fun callTerminal(context: Context) {
        if (ContextUtils.isNetworkConnected(context)) {
            CardRepo.getTerminalData(context)
            terminalResponse = CardRepo.getLiveData()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.internetnotavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}