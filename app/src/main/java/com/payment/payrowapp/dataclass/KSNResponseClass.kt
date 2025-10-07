package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class KSNResponseClass(
    @SerializedName("result") val result: KSNResult
)

data class KSNResult(@SerializedName("data") val data: DataClass)

data class DataClass(@SerializedName("WrappedKey") val WrappedKey: WrappedKey)

data class WrappedKey(
    @SerializedName("KeyCheckValue") val KeyCheckValue: String,
    @SerializedName("KeyCheckValueAlgorithm") val KeyCheckValueAlgorithm: String,
    @SerializedName("KeyMaterial") val KeyMaterial: String,
    @SerializedName("WrappedKeyMaterialFormat") val WrappedKeyMaterialFormat: String
)
