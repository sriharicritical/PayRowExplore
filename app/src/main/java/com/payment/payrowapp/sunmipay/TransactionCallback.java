package com.payment.payrowapp.sunmipay;

import com.payment.payrowapp.dataclass.FailedRequstClass;
import com.payment.payrowapp.dataclass.ResultRequestClass;
import com.payment.payrowapp.dataclass.SingleTapPinRequest;

public interface TransactionCallback {

    void onTransactionStatus(ResultRequestClass resultRequestClass, Boolean pinBlock);

    void onTransactionFailed(FailedRequstClass failedRequstClass, Boolean pinBlock);

    void singleTapPinRequest(SingleTapPinRequest singleTapPinRequest);
}
