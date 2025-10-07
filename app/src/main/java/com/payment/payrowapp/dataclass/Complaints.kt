package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class Complaints(
    @SerializedName("_id") val _id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("typeOfComplaint") val typeOfComplaint: String,
    @SerializedName("customerName") val customerName: String,
    @SerializedName("contact") val contact: String,
    @SerializedName("email") val email: String,
    @SerializedName("remarks") val remarks: Array<String>,
    @SerializedName("complaintDate") val complaintDate: String,
    @SerializedName("status") val status: String,
    @SerializedName("complaintNum") val complaintNum: String,
    @SerializedName("__v") val __v: String,
    @SerializedName("briefCompliant") val briefCompliant: String
)
