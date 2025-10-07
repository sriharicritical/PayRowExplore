package com.payment.payrowapp.contactpayrow

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.payment.payrowapp.dataclass.ComplaintRequest
import com.payment.payrowapp.dataclass.ComplaintResponse
import com.payment.payrowapp.dataclass.ComplaintsCount
import com.payment.payrowapp.dataclass.DecryptResponse

open class SupportViewModel(private val context: Context) : ViewModel() {

    private var response = MutableLiveData<DecryptResponse>()

    private var complaintResponse = MutableLiveData<ComplaintResponse>()

    fun getData(): MutableLiveData<DecryptResponse> {
        return response
    }

    fun postComplaint(complaintRequest: ComplaintRequest) {
        SupportRepository.getMutableLiveData(context, complaintRequest)
        response = SupportRepository.getLiveData()
    }

    fun getComplaints(context: Context) {
        RegisteredComplaintsRepository.getMutableLiveData(context)
        complaintResponse = RegisteredComplaintsRepository.getLiveData()
    }

    fun getComplaintData(): MutableLiveData<ComplaintResponse> {
        return complaintResponse
    }

    fun getComplaintsCount(context: Context) {
        RegisteredComplaintsRepository.getComplaintCountMutableLiveData(context)
        countResponse = RegisteredComplaintsRepository.getCountLiveData()
    }

    fun getComplaintCountsData(): MutableLiveData<ComplaintsCount> {
        return countResponse
    }

    private var countResponse = MutableLiveData<ComplaintsCount>()
}