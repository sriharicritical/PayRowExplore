package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class ContactlessConfiguration(
    @SerializedName("AID") val AID: String,
    @SerializedName("TAC Default") val TACDefault: String,
    @SerializedName("TAC Denial") val TACDenial: String,
    @SerializedName("TAC Online") val TACOnline: String,
    @SerializedName("CVM Limt") val CVMLimt: String,
    @SerializedName("Contactless Txn Limit") val ContactlessTxnLimit: String,
    @SerializedName("UDOL") val UDOL: String,
    @SerializedName("CMV Terminal Risk Management") val CMVTerminalRiskManagement: String,
    @SerializedName("KernelConfiguration") val KernelConfiguration: String,
    @SerializedName("Security Capability") val SecurityCapability: String,
    @SerializedName("Card Data Input Cap") val CardDataInputCap: String,
    @SerializedName("Chip CVMCapability Required") val ChipCVMCapabilityRequired: String,
    @SerializedName("Chip CVM CapabilityNot Required") val ChipCVMCapabilityNotRequired: String,
    @SerializedName("MSTRIPEApplicationVersion Number") val MSTRIPEApplicationVersionNumber: String,
    @SerializedName("MSTRIPE CVM Capability Required") val MSTRIPECVMCapabilityRequired: String,
    @SerializedName("MSTRIPE CVM Capability Not Required") val MSTRIPECVMCapabilityNotRequired: String,
    @SerializedName("Contactless Limit No DCV") val ContactlessLimitNoDCV: String,
    @SerializedName("Contactless Limit With Dcv") val ContactlessLimitWithDcv: String,
    @SerializedName("Reader Contactless Floor Limit") val ReaderContactlessFloorLimit : String,
    @SerializedName("Torn Transaction") val TornTransaction : String,
    @SerializedName("contactlesstacdenial") val contactlesstacdenial : String

)