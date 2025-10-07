package com.payment.payrowapp.sunmipay;

import java.io.Serializable;

public class ICCardInfo implements Serializable {

    private static final long serialVersionUID = -7617606055679080404L;

    public String AID;                 // Application Identifier (AID)
    public String ATC;                 // Application Transaction Counter (ATC)
    public String TSI;                 // Transaction Status Information (TSI)
    public String TVR;                 // Terminal Verification Results (TVR)
    public String CID;                 // Cryptogram Information Data (CID)
    public String ARpC;                // Application Cryptogram (AC)
    public String appName;             // Application Name
    public String appLabel;            // Application Label

    public String ic55Str;             // EMV/NFC tag data

    public String scriptResult;        // Script processing results

}
