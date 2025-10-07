package com.payment.payrowapp.mastercloud;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PaymentDataModel implements Serializable {

    private String amount;
    private String merchantIdentifier;
    private String orderId;
    private String transactionType;
    private String transactionCurrencyCode;
    private String transactionCategoryCode;
    private Map<String,String> merchantCustomData;
    private HashMap<String,String> configData;

    private ArrayList<TaxDetailModel> taxDetails;
    private String tip;
    private String discount;
    private String subtotal;

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getMerchantIdentifier() {
        return merchantIdentifier;
    }

    public void setMerchantIdentifier(String merchantIdentifier) {
        this.merchantIdentifier = merchantIdentifier;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getTransactionCurrencyCode() {
        return transactionCurrencyCode;
    }

    public void setTransactionCurrencyCode(String transactionCurrencyCode) {
        this.transactionCurrencyCode = transactionCurrencyCode;
    }

    public String getTransactionCategoryCode() {
        return transactionCategoryCode;
    }

    public void setTransactionCategoryCode(String transactionCategoryCode) {
        this.transactionCategoryCode = transactionCategoryCode;
    }

    public Map<String, String> getMerchantCustomData() {
        return merchantCustomData;
    }

    public void setMerchantCustomData(Map<String, String> merchantCustomData) {
        this.merchantCustomData = merchantCustomData;
    }

    public HashMap<String, String> getConfigData() {
        return configData;
    }

    public void setConfigData(HashMap<String, String> configData) {
        this.configData = configData;
    }

    public ArrayList<TaxDetailModel> getTaxDetails() {
        return taxDetails;
    }

    public void setTaxDetails(ArrayList<TaxDetailModel> taxDetails) {
        this.taxDetails = taxDetails;
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public String getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(String subtotal) {
        this.subtotal = subtotal;
    }

    @Override
    public String toString() {
        return "PaymentDataModel{" +
                "amount='" + amount + '\'' +
                ", merchantIdentifier='" + merchantIdentifier + '\'' +
                ", orderId='" + orderId + '\'' +
                ", transactionType='" + transactionType + '\'' +
                ", transactionCurrencyCode='" + transactionCurrencyCode + '\'' +
                ", transactionCategoryCode='" + transactionCategoryCode + '\'' +
                ", merchantCustomData=" + merchantCustomData +
                ", configData=" + configData +
                ", taxDetails=" + taxDetails +
                ", tip='" + tip + '\'' +
                ", discount='" + discount + '\'' +
                ", subtotal='" + subtotal + '\'' +
                '}';
    }
}
