package com.payment.payrowapp.sunmipay;

import android.util.Log;

import com.payment.payrowapp.app.MyApplication;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import fr.arnaudguyon.xmltojsonlib.JsonToXml;

public class KernelDataUtil {

    public static String readKernelData(Map<String, TLV> map) {

        ICCardInfo ICCardInfo = new ICCardInfo();
        PayDetail payDetail = new PayDetail();

        // 55 field
        String xmlString = null;
        String f55DataStr = "";
        final StringBuilder stringBuilder = new StringBuilder();
        JSONObject iccFinalData = new JSONObject();
        try {
            JSONObject iccObject = new JSONObject();

            // Authorized amount
            TLV tlv = map.get("9F02");
            if (tlv != null) {
                f55DataStr += tlv.getValue();
                stringBuilder.append("9f02");
                stringBuilder.append(":");
                stringBuilder.append(tlv.getValue());
                iccObject.put("AmountAuthorized", tlv.getValue());
            }


            // AID
            stringBuilder.append("\n");
            tlv = map.get("84");
            if (tlv != null && tlv.getValue() != null && tlv.getValue().trim().length() > 0) {
                String value = tlv.getValue();
                f55DataStr += value;
                stringBuilder.append("84");
                stringBuilder.append(":");
                stringBuilder.append(tlv.getValue());
                iccObject.put("ApplicationIdentifier", tlv.getValue());
            }

            //Apply interactive features
            stringBuilder.append("\n");
            tlv = map.get("82");
            if (tlv != null) {
                f55DataStr += tlv.getValue();
                stringBuilder.append("82");
                stringBuilder.append(":");
                stringBuilder.append(tlv.getValue());
                iccObject.put("ApplicationInterchangeProfile", tlv.getValue());
            }

            // Apply transaction counter (ATC)
            stringBuilder.append("\n");
            tlv = map.get("9F36");
            if (tlv != null && tlv.getValue() != null && tlv.getValue().length() > 0) {
                f55DataStr += tlv.getValue();
                stringBuilder.append("9F36");
                stringBuilder.append(":");
                stringBuilder.append(tlv.getValue());
                iccObject.put("ApplicationTransactionCounter", tlv.getValue());
            }

            // ARqc Apply ciphertext data AC - CRYPTOGRAM
            stringBuilder.append("\n");
            tlv = map.get("9F26");
            if (tlv != null && tlv.getValue() != null && tlv.getValue().length() > 0) {
                String value = tlv.getValue();
                f55DataStr += value;
                stringBuilder.append("9F26");
                stringBuilder.append(":");
                stringBuilder.append(tlv.getValue());
                iccObject.put("Cryptogram", tlv.getValue());
            }


            // cipher text information data CID = Cryptogram Information Data
            stringBuilder.append("\n");
            tlv = map.get("9F27");
            if (tlv != null && tlv.getValue() != null && tlv.getValue().length() > 0) {
                f55DataStr += tlv.getValue();
                stringBuilder.append("9F27");
                stringBuilder.append(":");
                stringBuilder.append(tlv.getValue());
                iccObject.put("CryptogramInformationData", tlv.getValue());
            }

            //CVM List missing

            // Cardholder Verification Method Results(CVM)
            stringBuilder.append("\n");
            tlv = map.get("9F34");
            if (tlv != null && tlv.getValue() != null && tlv.getValue().trim().length() > 0) {
                f55DataStr += tlv.getValue();
                String value = tlv.getValue();
                stringBuilder.append("9F34");
                stringBuilder.append(":");
                stringBuilder.append(tlv.getValue());
                iccObject.put("CvmResults", tlv.getValue());
                LogUtil.e(Constant.TAG, "Cardholder Verification Method ResultsCVM:" + value);
                boolean b2 = value.endsWith("00");
                boolean b1 = value.startsWith("1E") || value.startsWith("5E");
                if (b1 && b2) {
                    //  payDetail.isNeedSignature = true;
                }
            }

            //Interface Device Serial Number
            String deviceSN = getDeviceSN();
            int deviceSNLength = deviceSN.length();
            stringBuilder.append("\n");
            f55DataStr += deviceSN.substring(deviceSNLength - 8, deviceSNLength);
            stringBuilder.append("9F1E");
            stringBuilder.append(":");
            stringBuilder.append(getDeviceSN());
            iccObject.put("InterfaceDeviceSerialNumber", deviceSN.substring(deviceSNLength - 8, deviceSNLength));


            stringBuilder.append("\n");
            tlv = map.get("9F10");
            if (tlv != null && tlv.getValue() != null && tlv.getValue().trim().length() > 0) {
                f55DataStr += tlv.getValue();
                stringBuilder.append("9F10");
                stringBuilder.append(":");
                stringBuilder.append(tlv.getValue());
                iccObject.put("IssuerApplicationData", tlv.getValue());
            }

            stringBuilder.append("\n");
            tlv = map.get("9F33");
            if (tlv != null && tlv.getValue() != null && tlv.getValue().trim().length() > 0) {
                f55DataStr += tlv.getValue();
                stringBuilder.append("9F33");
                stringBuilder.append(":");
                stringBuilder.append(tlv.getValue());
                iccObject.put("TerminalCapabilities", tlv.getValue());
            } else {
                f55DataStr += "60F800";
                stringBuilder.append("9F33");
                stringBuilder.append(":");
                stringBuilder.append("60F800");
                iccObject.put("TerminalCapabilities", "60F800");
            }

            stringBuilder.append("\n");
            tlv = map.get("95");
            if (tlv != null && tlv.getValue() != null && tlv.getValue().length() > 0) {
                f55DataStr += tlv.getValue();
                stringBuilder.append("95");
                stringBuilder.append(":");
                stringBuilder.append(tlv.getValue());
                iccObject.put("TerminalVerificationResult", tlv.getValue());
            }

            //Application version number
            stringBuilder.append("\n");
            tlv = map.get("9F09");
            if (tlv != null && tlv.getValue() != null && tlv.getValue().trim().length() > 0) {
                f55DataStr += tlv.getValue();
                stringBuilder.append("9F09");
                stringBuilder.append(":");
                stringBuilder.append(tlv.getValue());
                iccObject.put("TerminalApplicationVersionNumber", tlv.getValue());
            } else {
                f55DataStr += "0030";
                stringBuilder.append("9F09");
                stringBuilder.append(":");
                stringBuilder.append("0030");
                iccObject.put("TerminalApplicationVersionNumber", "0030");
            }


            //Terminal country code
            stringBuilder.append("\n");
            f55DataStr += Constant.COUNTRY_CODE;
            stringBuilder.append("9F1A");
            stringBuilder.append(":");
            stringBuilder.append(Constant.COUNTRY_CODE);
            iccObject.put("TerminalCountryCode", Constant.COUNTRY_CODE);


            // terminal type
            stringBuilder.append("\n");
            tlv = map.get("9F35");
            if (tlv != null && tlv.getValue() != null && tlv.getValue().trim().length() > 0) {
                f55DataStr += tlv.getValue();
                stringBuilder.append("9F35");
                stringBuilder.append(":");
                stringBuilder.append(tlv.getValue());
                iccObject.put("TerminalType", tlv.getValue());
            } else {
                f55DataStr += "22";
                stringBuilder.append("9F35");
                stringBuilder.append(":");
                stringBuilder.append("22");
                iccObject.put("TerminalType", "22");
            }


            // Transaction currency code
            stringBuilder.append("\n");
            f55DataStr += Constant.COUNTRY_CODE;
            stringBuilder.append("5F2A");
            stringBuilder.append(":");
            stringBuilder.append(Constant.COUNTRY_CODE);
            iccObject.put("TransactionCurrencyCode", Constant.COUNTRY_CODE);


            // Transaction Date
            stringBuilder.append("\n");
            tlv = map.get("9A");
            if (tlv != null) {
                f55DataStr += tlv.getValue();
                stringBuilder.append("9A");
                stringBuilder.append(":");
                stringBuilder.append(tlv.getValue());
                iccObject.put("TransactionDate", tlv.getValue());
            } else {
                SimpleDateFormat df = new SimpleDateFormat("yyMMdd", Locale.getDefault());
                Date date = new Date();
                String format = df.format(date);
                f55DataStr += format;
                stringBuilder.append("9A");
                stringBuilder.append(":");
                stringBuilder.append(format);
                iccObject.put("TransactionDate", format);
            }

            // Transaction sequence counter
            stringBuilder.append("\n");
            tlv = map.get("9F41");
            if (tlv != null && tlv.getValue() != null && tlv.getValue().trim().length() > 0) {
                f55DataStr += tlv.getValue();
                stringBuilder.append("9F41");
                stringBuilder.append(":");
                stringBuilder.append(tlv.getValue());
                iccObject.put("TransactionSequenceCounter", tlv.getValue());
            }

            // Transaction Type
            stringBuilder.append("\n");
            tlv = map.get("9C");
            if (tlv != null) {
                f55DataStr += tlv.getValue();
                stringBuilder.append("9C");
                stringBuilder.append(":");
                stringBuilder.append(tlv.getValue());
                iccObject.put("TransactionType", tlv.getValue());
            } else {
                f55DataStr += "00";
                stringBuilder.append("9C");
                stringBuilder.append(":");
                stringBuilder.append("00");
                iccObject.put("TransactionType", "00");
            }

            // unpredictable number
            stringBuilder.append("\n");
            tlv = map.get("9F37");
            if (tlv != null) {
                f55DataStr += tlv.getValue();
                stringBuilder.append("9F37");
                stringBuilder.append(":");
                stringBuilder.append(tlv.getValue());
                iccObject.put("UnpredictableNumber", tlv.getValue());
            }

            stringBuilder.append("\n");
            tlv = map.get("9F6E");
            if (tlv != null && tlv.getValue() != null && tlv.getValue().trim().length() > 0) {
                f55DataStr += tlv.getValue();
                stringBuilder.append("9F6E");
                stringBuilder.append(":");
                stringBuilder.append(tlv.getValue());
                iccObject.put("FormFactorIndicator", tlv.getValue());
            }

            JSONObject iccRequest = new JSONObject();
            iccRequest.put("IccRequest", iccObject);
            Log.v("ICCObject", iccRequest.toString());
            iccFinalData.put("IccData", iccRequest);
            Log.v("ICCFinalObject", iccFinalData.toString());

            JsonToXml jsonToXml = new JsonToXml.Builder(iccFinalData).build();
            xmlString = jsonToXml.toString();
            Log.v("ICC XML", xmlString);

        } catch (Exception e) {
            e.printStackTrace();
        }


        Log.v("ICC DATA", String.valueOf(stringBuilder));
        payDetail.ic55Str = f55DataStr;
        ICCardInfo.ic55Str = f55DataStr;
        return xmlString;
    }

    private static String getDeviceSN() {
        try {
            return MyApplication.app.basicOptV2.getSysParam(AidlConstantsV2.SysParam.SN);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
