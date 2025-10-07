package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ServicesResponse(
    @SerializedName("success") val Success: String,
    @SerializedName("data") val data: ArrayList<Services>
)

data class Services(
    @SerializedName("categoryId") val categoryId: String,
    @SerializedName("categoryName") val categoryName: String,
    @SerializedName("serviceItems") val serviceItems: ArrayList<ServiceItems>,
    @SerializedName("size") var size: Int
)

data class Category(
    @SerializedName("categoryId") val categoryId: String,
    @SerializedName("categoryName") val categoryName: String,
)

data class Product(
    @SerializedName("serviceCode") val serviceId: String,
    @SerializedName("shortServiceName") val shortServiceName: String,
    @SerializedName("quantity") var quantity: Int,
    @SerializedName("unitPrice") val unitPrice: Double,
    @SerializedName("numberOfUnits") val numberOfUnits: Int,
    @SerializedName("transactionAmount") var transactionAmount: Double,
    @SerializedName("serviceType") val serviceType: String?)

class ProductDetails(@SerializedName("service") val service: ArrayList<Product>)

data class ServiceItems(
    @SerializedName("serviceId") val serviceId: String,
    @SerializedName("serviceName") val serviceName: String,
    @SerializedName("shortServiceName") val shortServiceName: String,
    @SerializedName("englishDescription") val englishDescription: String,
    @SerializedName("unitPrice") val unitPrice: String,
    @SerializedName("_id") val _id: String,
    @SerializedName("serviceType") val serviceType: String
) : Serializable


data class BarCodeItem(
    @SerializedName("serviceCode") val serviceCode: String,
    @SerializedName("shortServiceName") val shortServiceName: String,
    @SerializedName("unitPrice") val unitPrice: String,
    @SerializedName("quantity") var quantity: Int,
    @SerializedName("transactionAmount") var transactionAmount: Double,
    @SerializedName("categoryId") val categoryId: String)