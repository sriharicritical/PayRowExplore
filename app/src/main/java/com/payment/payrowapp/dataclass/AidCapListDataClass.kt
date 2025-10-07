package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class AidCapListDataClass(
    @SerializedName("AIdkeys") val AIdkeys: ArrayList<AidKeysDataClass>,
    @SerializedName("CapKeys") val CapKeys: ArrayList<CapKeysDataClass>
)

/*
data class AidCapDataClass(
    @SerializedName("AIdkeys") val AIdkeys: ArrayList<AidKeysDataClass>,
    @SerializedName("CapKeys") val CapKeys: ArrayList<CapKeysDataClass>
)*/
