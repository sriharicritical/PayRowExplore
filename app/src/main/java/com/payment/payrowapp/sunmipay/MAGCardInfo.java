package com.payment.payrowapp.sunmipay;

import java.io.Serializable;

public class MAGCardInfo implements Serializable {

    private static final long serialVersionUID = -6131481914061907998L;

    public String track1;          // track1
    public String track2;          // track2
    public String track3;          // track3

    public String track2Cipher;    // Track2 Cryptogram Information
    public String track3Cipher;    // Track3 Cryptogram Information

    public boolean isIccCard;      // is contact or contactless
    public String expireDate;      // Card Expire Date
    public String cardHolder;      // Card Holder Name
    public String countryCode;     // Country Code

}
