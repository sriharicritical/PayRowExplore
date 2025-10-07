package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class CapKeysDataClass(@SerializedName("Scheme") val Schemes: String,
                            @SerializedName("RID") val RID: String,
                            @SerializedName("keyIndex") val keyIndex: String,
                            @SerializedName("Modulus") val Modulus: String,
                            @SerializedName("Exponent") val Exponent: String,
                            @SerializedName("hashInd") val hashInd: String,
                            @SerializedName("arithInd") val arithInd: String,
                            @SerializedName("expDate") val expDate: String,
                            @SerializedName("checkSum") val checkSum: String)
