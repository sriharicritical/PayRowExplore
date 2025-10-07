package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class ComplaintRequest(
    @SerializedName("typeOfComplaint") val typeOfComplaint: String?,
    @SerializedName("briefCompliant") val briefCompliant: String,
    @SerializedName("salesId") val salesId: String,
    @SerializedName("mainMerchantId") val mainMerchantId: String,
    @SerializedName("terminalId") val terminalId: String,
)
