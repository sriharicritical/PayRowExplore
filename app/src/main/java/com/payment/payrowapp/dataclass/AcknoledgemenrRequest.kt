package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class AcknoledgemenrRequest(
    @SerializedName("txnAmount") var txnAmount: String?,
    @SerializedName("tranType") var tranType: String?,
    @SerializedName("corelationId") var corelationId: String?,
    @SerializedName("schemeId") var schemeId: String?,
    @SerializedName("pmtTxnRefCode") var pmtTxnRefCode: String?,
    @SerializedName("postDate") var postDate: String?,
    @SerializedName("authRespCode") var authRespCode: String?,
    @SerializedName("authCode") var authCode: String?,
    @SerializedName("txnStatus") var txnStatus: String?,
    @SerializedName("refId") var refId: String?,
    @SerializedName("acqTransactionCompletionDate") var acqTransactionCompletionDate: String?,
    @SerializedName("acqRRN") var acqRRN: String?,
    @SerializedName("acqPaymentMode") var acqPaymentMode: String? )