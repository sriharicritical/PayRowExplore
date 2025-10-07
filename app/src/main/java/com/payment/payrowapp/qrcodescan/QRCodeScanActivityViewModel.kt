package com.payment.payrowapp.qrcodescan

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.payment.payrowapp.R
import com.payment.payrowapp.dataclass.QRCodeResponse
import com.payment.payrowapp.utils.ContextUtils
import com.payment.payrowapp.dataclass.BarCodeResponse

open class QRCodeScanActivityViewModel(private val context: Context) : ViewModel() {

    private var response = MutableLiveData<QRCodeResponse>()

    fun getQrCodeInvoiceLiveData(): MutableLiveData<QRCodeResponse> {
        return response
    }

    fun getQrCodeInvoice(invoiceNumber: String) {
        if (ContextUtils.isNetworkConnected(context)) {
            QRCodeScanActivityRepository.getMutableLiveData(context, invoiceNumber)
            response = QRCodeScanActivityRepository.getLiveData()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.internetnotavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }


    private var barCodeResponse = MutableLiveData<BarCodeResponse>()

    fun getBarCodeItemLiveData(): MutableLiveData<BarCodeResponse> {
        return barCodeResponse
    }

    fun getQrCodeItem(invoiceNumber: String) {
        if (ContextUtils.isNetworkConnected(context)) {
            QRCodeScanActivityRepository.getBarCodeMutableLiveData(context, invoiceNumber)
            barCodeResponse = QRCodeScanActivityRepository.getBarLiveData()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.internetnotavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private var gateWayBarCodeResponse = MutableLiveData<BarCodeResponse>()

    fun getGatewayBarCodeItemLiveData(): MutableLiveData<BarCodeResponse> {
        return gateWayBarCodeResponse
    }

    fun getGatewayBarCodeItem(invoiceNumber: String) {
        if (ContextUtils.isNetworkConnected(context)) {
            QRCodeScanActivityRepository.getGatewayBarCodeMutableLiveData(context, invoiceNumber)
            gateWayBarCodeResponse = QRCodeScanActivityRepository.getGatewayBarLiveData()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.internetnotavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}