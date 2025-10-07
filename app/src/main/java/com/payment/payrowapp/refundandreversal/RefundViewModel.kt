package com.payment.payrowapp.refundandreversal

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.payment.payrowapp.R
import com.payment.payrowapp.dataclass.QRCodeResponse
import com.payment.payrowapp.dataclass.RefundResponse
import com.payment.payrowapp.qrcodescan.QRCodeScanActivityRepository
import com.payment.payrowapp.utils.ContextUtils

class RefundViewModel(private val context: Context) : ViewModel() {

    private var response = MutableLiveData<RefundResponse>()

    private var voidResponse = MutableLiveData<RefundResponse>()

    fun getData(): MutableLiveData<RefundResponse> {
        return response
    }


    private var purchaseresponse = MutableLiveData<QRCodeResponse>()

    fun getQrCodeInvoiceLiveData(): MutableLiveData<QRCodeResponse> {
        return purchaseresponse
    }

    fun getQrCodeInvoice(invoiceNumber: String) {
        if (ContextUtils.isNetworkConnected(context)) {
            QRCodeScanActivityRepository.getMutableLiveData(context, invoiceNumber)
            purchaseresponse = QRCodeScanActivityRepository.getLiveData()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.internetnotavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }

     fun reSetPurchaseResp() {
        purchaseresponse.value = null
    }
}