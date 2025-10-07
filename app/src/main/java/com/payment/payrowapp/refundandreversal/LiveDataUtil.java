package com.payment.payrowapp.refundandreversal;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

public class LiveDataUtil {

    public static <T> void observeOnce(LifecycleOwner owner, LiveData<T> liveData, Observer<T> observer) {
        liveData.observe(owner, new Observer<T>() {
            @Override
            public void onChanged(T t) {
                observer.onChanged(t);
                liveData.removeObserver(this); // Remove the observer after it is triggered once
            }
        });
    }
}
