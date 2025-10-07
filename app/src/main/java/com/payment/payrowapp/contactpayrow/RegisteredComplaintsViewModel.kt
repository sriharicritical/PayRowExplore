package com.payment.payrowapp.contactpayrow

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.payment.payrowapp.dataclass.ComplaintResponse
import com.payment.payrowapp.dataclass.ComplaintStatus
import com.payment.payrowapp.dataclass.DecryptResponse

class RegisteredComplaintsViewModel(private val context: Context) : ViewModel() {

    private var response = MutableLiveData<ComplaintResponse>()
    private var complaintStatusResponse = MutableLiveData<DecryptResponse>()

    fun getData(): MutableLiveData<ComplaintResponse> {
        return response
    }
    fun getComplaintStatusResponseData(): MutableLiveData<DecryptResponse> {
        return complaintStatusResponse
    }

    fun getComplaints(context: Context) {
        RegisteredComplaintsRepository.getMutableLiveData(context)
        response = RegisteredComplaintsRepository.getLiveData()
    }
    fun updateComplaintStatusResponse(context: Context,complaintId:String,complaintStatus: ComplaintStatus) {
        RegisteredComplaintsRepository.updateComplaintStatus(context,complaintId,complaintStatus)
        complaintStatusResponse = RegisteredComplaintsRepository.getComplaintStatusData()
    }
}