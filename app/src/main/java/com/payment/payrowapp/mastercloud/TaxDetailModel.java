package com.payment.payrowapp.mastercloud;

import java.io.Serializable;

public class TaxDetailModel implements Serializable {
    private String amount;
    private String type;

    public TaxDetailModel() {
    }

    public String getAmount() {
        return this.amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
