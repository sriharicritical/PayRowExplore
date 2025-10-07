package com.payment.payrowapp.dataclass

data class FailedRequstClass(
    var orderRequest: OrderRequest,
    val channel: String?,
    val bankTransferURL: String?,
    val onlineStatus: Boolean?,
    val signatureStatus: Boolean?
)
