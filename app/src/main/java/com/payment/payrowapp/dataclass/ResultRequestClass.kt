package com.payment.payrowapp.dataclass

data class ResultRequestClass(
    var orderRequest: OrderRequest,
    val orderNumber: String?,
    val cardNumber: String?,
    val hostRefNo: String?,
    val vasRefNo: String?,
    val status: String?,
    val channel: String?,
    val bankTransferURL: String?,
    val authorizationId: String?,
    val appPANSeqNo: String?,
    var iccData: String?,
    val signatureStatus: Boolean
)