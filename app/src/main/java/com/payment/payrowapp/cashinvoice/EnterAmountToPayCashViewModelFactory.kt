package com.payment.payrowapp.cashinvoice

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class EnterAmountToPayCashViewModelFactory(private val context: Context) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EnterAmountToPayCashViewModel(context) as T
    }
}