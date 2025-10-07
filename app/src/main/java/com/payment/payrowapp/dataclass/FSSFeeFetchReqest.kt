package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class FSSFeeFetchReqest(
    @SerializedName("curExponent") var curExponent: String?,
    @SerializedName("txnAmount") var txnAmount: String?,
    @SerializedName("tranType") var tranType: String?,
    @SerializedName("correlationId") var correlationId: String?,
    @SerializedName("schemeId") var schemeId: String?,
    @SerializedName("pmtTxnRefCode") var pmtTxnRefCode: String?,
    @SerializedName("cardType") var cardType: String?,
    @SerializedName("merchantTrn") var merchantTrn: String?,
    @SerializedName("serviceTrn") var serviceTrn: String?,
    @SerializedName("refId") var refId: String?,
    @SerializedName("serviceData") var serviceData: ArrayList<FeeServiceData>?
)