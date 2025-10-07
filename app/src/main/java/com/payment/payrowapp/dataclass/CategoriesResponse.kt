package com.payment.payrowapp.dataclass

import com.google.gson.annotations.SerializedName

data class CategoriesResponse(@SerializedName("success") val success: String,
                         @SerializedName("data") val data: ArrayList<Categories>,
                              @SerializedName("error") val error: String)

data class Categories(@SerializedName("catId") val catId: String,
                      @SerializedName("categoryName") val categoryName: String,
                      @SerializedName("categoryItems") val categoryItems: ArrayList<Products>)
data class Products(@SerializedName("productId") val productId: String,
               @SerializedName("itemName") val itemName: String,
               @SerializedName("itemDescription") val itemDescription: String,
               @SerializedName("status") val status: String,
               @SerializedName("_id") val _id: String)
