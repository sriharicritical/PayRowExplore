package com.payment.payrowapp.cardpayment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CardPaymentViewModelFactory() :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CardPaymentViewModel() as T
    }
}