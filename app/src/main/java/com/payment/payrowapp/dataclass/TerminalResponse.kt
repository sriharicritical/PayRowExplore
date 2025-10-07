package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class TerminalResponse(@SerializedName("data")val data: ArrayList<TerminalData>,@SerializedName("success") val success: Boolean)

data class TerminalData(
    @SerializedName("MTI") val MTI: String,
    @SerializedName("PROCESSING_CODE") val PROCESSING_CODE: String,
    @SerializedName("SYSTEM_TRACE_AUDIT_NUMBER")  val SYSTEM_TRACE_AUDIT_NUMBER: String,
    @SerializedName("MERCHANT_TYPE")  val MERCHANT_TYPE: String,
    @SerializedName("POS_ENTRY_MODE")   val POS_ENTRY_MODE: String,
    @SerializedName("APP_PAN_SEQUENCE_NO")   val APP_PAN_SEQUENCE_NO: String,
    @SerializedName("POS_CONDITION_CODE")  val POS_CONDITION_CODE: String,
    @SerializedName("ACQUIRER_INST_ID_CODE")  val ACQUIRER_INST_ID_CODE: String,
    @SerializedName("POS_PIN_CAPTURE_CODE")  val POS_PIN_CAPTURE_CODE: String,
    @SerializedName("CARD_ACCEPTOR_TERMINAL_ID")  val CARD_ACCEPTOR_TERMINAL_ID: String,
    @SerializedName("CARD_ACCEPTOR_ID_CODE")  val CARD_ACCEPTOR_ID_CODE: String,
    @SerializedName("CARD_ACCEPTOR_NAME_LOCATION")  val CARD_ACCEPTOR_NAME_LOCATION: String,
    @SerializedName("CURRENCY_CODE")  val CURRENCY_CODE: String,
    @SerializedName("SPONSOR_BANK")  val SPONSOR_BANK: String,
    @SerializedName("TERMINAL_TYPE")  val TERMINAL_TYPE: String,
    @SerializedName("POS_GEO_DATA")val POS_GEO_DATA: String,
    @SerializedName("VOIDMTI")val VOIDMTI: String
)
