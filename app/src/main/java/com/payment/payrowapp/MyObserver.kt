package com.payment.payrowapp

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer


fun <T> MutableLiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        /*  fun onChanged(t: T?) {
             if (t != null) {
                 observer.onChanged(t)
                 postValue(null)
                 removeObserver(this)
             }
         }*/

        override fun onChanged(t: T) {
            if (t != null) {
                observer.onChanged(t)
                postValue(null)
                removeObserver(this)
            }
        }
    })
}
