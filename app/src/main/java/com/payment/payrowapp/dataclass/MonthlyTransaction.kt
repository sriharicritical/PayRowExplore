package com.payment.payrowapp.dataclass

data class MonthlyTransaction(val _id:Id,val cashValue:String,val tapValue:String,val totalSales:String)
class Id(val month:String,val year:String)
