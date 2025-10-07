package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PurchaseRequest(
    @SerializedName("0") var MTI: String?,
    @SerializedName("1") var RefundNum: String?,
    @SerializedName("2") var cardNumber: String?,
    @SerializedName("3") var processingCode: String?,
    @SerializedName("4") var amount: String?,
    @SerializedName("7") var transDateAndTime: String?,
    @SerializedName("11") var systemTraceAuditNumber: String?,
    @SerializedName("12") var time: String?,
    @SerializedName("13") var date: String?,
    @SerializedName("14") var expiryDate: String?,
    @SerializedName("15") var settlementdate: String?,
    @SerializedName("18") var merchantType: String?,
    @SerializedName("22") var posEntryMode: String?,
    @SerializedName("23") var appPANSeqNo: String?,
    @SerializedName("25") var posConditionCode: String?,
    @SerializedName("26") var posPINCaptureCode: String?,
    @SerializedName("32") var acquirerInstIdCode: String?, //Amount,Settle processing fee
    @SerializedName("35") var track2Data: String?,
    @SerializedName("37") var orderNo: String?, //Order Number
    @SerializedName("38") var authorizationId: String?,
    @SerializedName("39") var responseCode: String?,
    @SerializedName("41") var cardAcceptorTerminalID: String?, //Card Acceptor Terminal ID
    @SerializedName("42") var cardAcceptorIDCode: String?, //Card Acceptor ID Code
    @SerializedName("43") var cardAcceptorNameLocation: String?,//Card Acceptor Name ,Location
    @SerializedName("49") var currencyCode: String?,
    @SerializedName("52") var pinData: String?,//Card and PIN number encrypted format
    @SerializedName("70") var posGeoData: String?, //Echo test
    @SerializedName("90") var field90: String?,
    @SerializedName("123") var sponsorBank: String?,
    @SerializedName("45") var track1Data: String?,
    @SerializedName("56") var reasonCode: String?,
    @SerializedName("127.25") var fiftyFiveData: String?,
    @SerializedName("127.33") var extendedTXNType: String?,
    @SerializedName("127.12") var terminalOwner: String?,
    @SerializedName("127.2") var switchKey: String?,
    @SerializedName("127.11") var originalKey: String?,
    @SerializedName("KSNNumber") var KSNNumber: String?,
    @SerializedName("127.22") var structuredData: String?,
    @SerializedName("48") var fssFetch: String?,
    @SerializedName("60") var ackCardBrand: String?
) //POS data code
    : Serializable {
    constructor(cardNumber: String?) : this(
        null, null, cardNumber, null, null, null, null, null, null, null, null, null, null, null, null, null,
        null, null,null, null, null, null, null, null, null, null, null, null, null, null,null,null, null,null,null,null,null, null,null,null)

}


data class RequestObject(
    @SerializedName("ContentType") var success: String,
    @SerializedName("Label") var Label: String,
    @SerializedName("LenType") var LenType: String,
    @SerializedName("MaxLen") var MaxLen: Int,
    @SerializedName("varue") var varue: String
)
