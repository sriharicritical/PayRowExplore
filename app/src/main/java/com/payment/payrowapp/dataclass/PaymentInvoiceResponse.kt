package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class PaymentInvoiceResponse(@SerializedName("success") val success: Boolean,
                                  @SerializedName("message") val message: String,
                                  @SerializedName("data") val data: DataN,
                                  @SerializedName("error") val error: String)
class DataN(  val path: String,
              val _id: String,
              val createdAt: String,
              val updatedAt: String,
              val __v: String,)