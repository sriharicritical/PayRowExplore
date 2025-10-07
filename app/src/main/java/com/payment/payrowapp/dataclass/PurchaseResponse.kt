package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class PurchaseResponse(
    @SerializedName("result") val purchaseResult: PurchaseResult,
    @SerializedName("errors") val errors: Array<String>
)

class PurchaseResult(
    @SerializedName("0") var MTI: String,
    @SerializedName("1") var RefundNum: String,
    @SerializedName("2") var cardNumber: String,
    @SerializedName("3") var processingCode: String,
    @SerializedName("4") var amount: String,
    @SerializedName("7") var transDateAndTime: String,
    @SerializedName("11") var systemTraceAuditNumber: String,
    @SerializedName("12") var time: String,
    @SerializedName("13") var date: String,
    @SerializedName("18") var merchantType: String,
    @SerializedName("22") var posEntryMode: String,
    @SerializedName("23") var appPANSeqNo: String,
    @SerializedName("25") var posConditionCode: String,
    @SerializedName("26") var posPINCaptureCode: String,
    @SerializedName("32") var acquirerInstIdCode: String, //Amount,Settle processing fee
//                           @SerializedName("35") var track2Data: String,
    @SerializedName("33") var refund33: String,
    @SerializedName("38") var authorizationId: String,
    @SerializedName("39") var responseCode: String,
    @SerializedName("37") var orderNo: String, //Order Number
    @SerializedName("41") var cardAcceptorTerminalID: String, //Card Acceptor Terminal ID
    @SerializedName("42") var cardAcceptorIDCode: String, //Card Acceptor ID Code
    @SerializedName("43") var cardAcceptorNameLocation: String,//Card Acceptor Name ,Location
    @SerializedName("49") var currencyCode: String,
    @SerializedName("52") var terminalOwner: String,//Card and PIN number encrypted format
    @SerializedName("70") var posGeoData: String, //Echo test
    @SerializedName("123") var sponsorBank: String,
    @SerializedName("127.25") var iccData: String,
    @SerializedName("127.37") var extendedResponseCode:String,
    @SerializedName("127.22") var referenceId127_22:String,
    @SerializedName("127.3") var referenceId127_3:String,
    @SerializedName("48") var fssFetch:String,
) //POS data code)
