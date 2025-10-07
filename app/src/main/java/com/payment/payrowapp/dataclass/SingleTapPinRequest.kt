package com.payment.payrowapp.dataclass

data class SingleTapPinRequest(
    var feeRefID: String,
    val purchaseFeeRequest: PurchaseRequest,
    var orderRequest: OrderRequest,
    val purchaseRequest: PurchaseRequest,
    val expiryDate: String,
    val cardNumber: String,
    val totalAmount: String,
    val referenceId48: String
)