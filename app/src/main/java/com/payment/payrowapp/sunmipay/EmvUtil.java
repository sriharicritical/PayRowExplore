package com.payment.payrowapp.sunmipay;

import android.content.Context;
import android.util.Log;

import com.payment.payrowapp.app.MyApplication;
import com.payment.payrowapp.dataclass.AidCapListDataClass;
import com.payment.payrowapp.dataclass.AidKeysDataClass;
import com.payment.payrowapp.dataclass.CapKeysDataClass;
import com.payment.payrowapp.dataclass.ContactlessConfiguration;
import com.payment.payrowapp.newpayment.TinyDB;
import com.payment.payrowapp.utils.Constants;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;
import com.sunmi.pay.hardware.aidlv2.bean.AidV2;
import com.sunmi.pay.hardware.aidlv2.bean.CapkV2;
import com.sunmi.pay.hardware.aidlv2.bean.EmvTermParamV2;
import com.sunmi.pay.hardware.aidlv2.emv.EMVOptV2;

import java.util.ArrayList;
import java.util.Map;

public final class EmvUtil {
    public static final String COUNTRY_CHINA = "China";
    public static final String COUNTRY_RUSSIA = "Russia";
    public static final String COUNTRY_INDIA = "India";
    public static final String COUNTRY_UAE = "UAE";

    private EmvUtil() {
    }

    public static synchronized int saveDUKPT(byte[] ksnValue, byte[] keyValue, byte[] kcvValue) {
        int result = -1;
        try {
            result = MyApplication.app.securityOptV2.saveKeyDukpt(AidlConstantsV2.Security.KEY_TYPE_DUPKT_IPEK, keyValue, kcvValue, ksnValue, AidlConstantsV2.Security.KEY_ALG_TYPE_3DES, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String keyValueStr = ByteUtil.bytes2HexStr(keyValue);
        String ksnValueStr = ByteUtil.bytes2HexStr(ksnValue);

        LogUtil.e(Constant.TAG, "保存DUKPT result：" + result);
        LogUtil.e(Constant.TAG, "保存DUKPT keyValueStr：" + keyValueStr);
        LogUtil.e(Constant.TAG, "保存DUKPT ksnValueStr：" + ksnValueStr);
        return result;
    }


    /**
     * Convert Hex string AID to AidV2
     */
    public static AidV2 hexStr2Aid(String hexStr) {
        AidV2 aidV2 = new AidV2();
        Map<String, TLV> map = TLVUtil.buildTLVMap(hexStr);
        TLV tlv = map.get("DF21");
        if (tlv != null) {
            aidV2.cvmLmt = ByteUtil.hexStr2Bytes(tlv.getValue());
        }
        tlv = map.get("DF20");
        if (tlv != null) {
            aidV2.termClssLmt = ByteUtil.hexStr2Bytes(tlv.getValue());
        }
        tlv = map.get("DF19");
        if (tlv != null) {
            aidV2.termClssOfflineFloorLmt = ByteUtil.hexStr2Bytes(tlv.getValue());
        }
        tlv = map.get("9F7B");
        if (tlv != null) {
            aidV2.termOfflineFloorLmt = ByteUtil.hexStr2Bytes(tlv.getValue());
        }
        tlv = map.get("9F06");
        if (tlv != null) {
            aidV2.aid = ByteUtil.hexStr2Bytes(tlv.getValue());
        }
        tlv = map.get("DF01");
        if (tlv != null) {
            aidV2.selFlag = ByteUtil.hexStr2Byte(tlv.getValue());
        }
        tlv = map.get("DF17");
        if (tlv != null) {
            aidV2.targetPer = ByteUtil.hexStr2Byte(tlv.getValue());
        }
        tlv = map.get("DF16");
        if (tlv != null) {
            aidV2.maxTargetPer = ByteUtil.hexStr2Byte(tlv.getValue());
        }
        tlv = map.get("9F1B");
        if (tlv != null) {
            aidV2.floorLimit = ByteUtil.hexStr2Bytes(tlv.getValue());
        }
        tlv = map.get("DF15");
        if (tlv != null) {
            aidV2.threshold = ByteUtil.hexStr2Bytes(tlv.getValue());
        }
        tlv = map.get("DF13");
        if (tlv != null) {
            aidV2.TACDenial = ByteUtil.hexStr2Bytes(tlv.getValue());
        }
        tlv = map.get("DF12");
        if (tlv != null) {
            aidV2.TACOnline = ByteUtil.hexStr2Bytes(tlv.getValue());
        }
        tlv = map.get("DF11");
        if (tlv != null) {
            aidV2.TACDefault = ByteUtil.hexStr2Bytes(tlv.getValue());
        }
        tlv = map.get("9F01");
        if (tlv != null) {
            aidV2.AcquierId = ByteUtil.hexStr2Bytes(tlv.getValue());
        }
        tlv = map.get("DF14");
        if (tlv != null) {
            aidV2.dDOL = ByteUtil.hexStr2Bytes(tlv.getValue());
        }
        tlv = map.get("9F09");
        if (tlv != null) {
            aidV2.version = ByteUtil.hexStr2Bytes(tlv.getValue());
        }
        tlv = map.get("9F4E");
        if (tlv != null) {
            aidV2.merchName = ByteUtil.hexStr2Bytes(tlv.getValue());
        }
        tlv = map.get("9F15");
        if (tlv != null) {
            aidV2.merchCateCode = ByteUtil.hexStr2Bytes(tlv.getValue());
        }
        tlv = map.get("9F16");
        if (tlv != null) {
            aidV2.merchId = ByteUtil.hexStr2Bytes(tlv.getValue());
        }
        tlv = map.get("9F3C");
        if (tlv != null) {
            aidV2.referCurrCode = ByteUtil.hexStr2Bytes(tlv.getValue());
        }
        tlv = map.get("9F3D");
        if (tlv != null) {
            aidV2.referCurrExp = ByteUtil.hexStr2Byte(tlv.getValue());
        }
        tlv = map.get("DFC108");// N
        if (tlv != null) {
            aidV2.clsStatusCheck = ByteUtil.hexStr2Byte(tlv.getValue());
        }
        tlv = map.get("DFC109");//N
        if (tlv != null) {
            aidV2.zeroCheck = ByteUtil.hexStr2Byte(tlv.getValue());
        }
        tlv = map.get("DFC10A"); //N
        if (tlv != null) {
            aidV2.kernelType = ByteUtil.hexStr2Byte(tlv.getValue());
        }
        tlv = map.get("DFC10B");//N
        if (tlv != null) {
            aidV2.paramType = ByteUtil.hexStr2Byte(tlv.getValue());
        }
        tlv = map.get("9F66");//N
        if (tlv != null) {
            aidV2.ttq = ByteUtil.hexStr2Bytes(tlv.getValue());
        }
        tlv = map.get("9F1C");
        if (tlv != null) {
            aidV2.termId = ByteUtil.hexStr2Bytes(tlv.getValue());
        }
        tlv = map.get("9F1D");
        if (tlv != null) {
            aidV2.riskManData = ByteUtil.hexStr2Bytes(tlv.getValue());
        }
        tlv = map.get("DF8101");
        if (tlv != null) {
            aidV2.referCurrCon = ByteUtil.hexStr2Bytes(tlv.getValue());
        }
        tlv = map.get("DF8102");
        if (tlv != null) {
            aidV2.tDOL = ByteUtil.hexStr2Bytes(tlv.getValue());
        }
        tlv = map.get("DFC10C"); // N
        if (tlv != null) {
            aidV2.kernelID = ByteUtil.hexStr2Bytes(tlv.getValue());
        }
        return aidV2;
    }

    /**
     * Convert Hex string RID to CapkV2
     */
    public static CapkV2 hexStr2Rid(String hexStr) {
        CapkV2 capkV2 = new CapkV2();
        Map<String, TLV> map = TLVUtil.buildTLVMap(hexStr);
        TLV tlv = map.get("9F06");
        if (tlv != null) {
            capkV2.rid = ByteUtil.hexStr2Bytes(tlv.getValue());
        }
        tlv = map.get("9F22");
        if (tlv != null) {
            capkV2.index = ByteUtil.hexStr2Byte(tlv.getValue());
        }
        tlv = map.get("DF06");
        if (tlv != null) {
            capkV2.hashInd = ByteUtil.hexStr2Byte(tlv.getValue());
        }
        tlv = map.get("DF07");
        if (tlv != null) {
            capkV2.arithInd = ByteUtil.hexStr2Byte(tlv.getValue());
        }
        tlv = map.get("DF02");
        if (tlv != null) {
            capkV2.modul = ByteUtil.hexStr2Bytes(tlv.getValue());
        }
        tlv = map.get("DF04");
        if (tlv != null) {
            capkV2.exponent = ByteUtil.hexStr2Bytes(tlv.getValue());
        }
        tlv = map.get("DF05");
        if (tlv != null) {
            capkV2.expDate = ByteUtil.hexStr2Bytes(tlv.getValue());
        }
        tlv = map.get("DF03");
        if (tlv != null) {
            capkV2.checkSum = ByteUtil.hexStr2Bytes(tlv.getValue());
        }
        return capkV2;
    }



    public static void injectAIDAndCAPKeys(String terminalType,Context context, String addCapability, String terminalId, String merchantID, String ectTlVal, String capability, String TTQ, String currencyCode,

                                           AidCapListDataClass aidCapListDataClass) {
        try {
            EMVOptV2 emvOptV2 = MyApplication.app.emvOptV2;

            // clear SDK built-in AIDs and CAPKs
            emvOptV2.deleteAid(null);
            emvOptV2.deleteCapk(null, null);

            ArrayList<ContactlessConfiguration> contactlessConfigurationArrayList = new ArrayList<>();

            //Injecting the AID keys
            for (int i = 0; i < aidCapListDataClass.getAIdkeys().size(); i++) {
                if (aidCapListDataClass.getAIdkeys().get(i) != null) {
                    AidKeysDataClass aidKeysDataClass = aidCapListDataClass.getAIdkeys().get(i);
                    Log.e("AidKeys:Before", aidKeysDataClass.toString());
                    Log.e("AidKeys:After", getAid(aidKeysDataClass));

                    if ((aidKeysDataClass.getSchemes().equals("Maestro") || aidKeysDataClass.getSchemes().equals("Mastercard"))
                            && aidKeysDataClass.getMode().equals("Contactless")) {
                        ContactlessConfiguration contactlessConfiguration = new ContactlessConfiguration(
                                aidKeysDataClass.getAID(), aidKeysDataClass.getTACDefault(), aidKeysDataClass.getTACDenial(), aidKeysDataClass.getTACOnline(),
                                aidKeysDataClass.getCVMLimt(), aidKeysDataClass.getContactlessTxnLimit(),
                                aidKeysDataClass.getUDOL(), aidKeysDataClass.getCMVTerminalRiskManagement(),
                                aidKeysDataClass.getKernelConfiguration(), aidKeysDataClass.getSecurityCapability(), aidKeysDataClass.getCardDataInputCap(),
                                aidKeysDataClass.getChipCVMCapabilityRequired(), aidKeysDataClass.getChipCVMCapabilityNotRequired(),
                                aidKeysDataClass.getMSTRIPEApplicationVersionNumber(), aidKeysDataClass.getMSTRIPECVMCapabilityRequired(),
                                aidKeysDataClass.getMSTRIPECVMCapabilityNotRequired(),
                                aidKeysDataClass.getContactlessLimitNoDCV(), aidKeysDataClass.getContactlessLimitWithDcv(),aidKeysDataClass.getReaderContactlessFloorLimit(),aidKeysDataClass.getTornTransaction(),aidKeysDataClass.getContactlesstacdenial());

                        contactlessConfigurationArrayList.add(contactlessConfiguration);
                    }
                    if (!aidKeysDataClass.getAID().equals("A0000000043060C123456789")) {
                        AidV2 aidV2 = hexStr2Aid(getAid(aidKeysDataClass));
                        emvOptV2.addAid(aidV2);
                    }
                }
            }

            // save contactless data
            if (contactlessConfigurationArrayList.size() >= 1) {
                TinyDB tinyDB = new TinyDB(context);
                tinyDB.putConfigListObject(Constants.CONTACTLESS_CONFIGURATION, contactlessConfigurationArrayList);
            }

            //Injecting the CAP keys
            for (int i = 0; i < aidCapListDataClass.getCapKeys().size(); i++) {
                if (aidCapListDataClass.getCapKeys().get(i) != null) {
                    CapKeysDataClass capKeysDataClass = aidCapListDataClass.getCapKeys().get(i);
                    CapkV2 capkV2 = hexStr2Rid(getCapKey(capKeysDataClass));
                    emvOptV2.addCapk(capkV2);
                }
            }


            LogUtil.e(Constant.TAG, "init AID and CAPKs success");
            EmvTermParamV2 emvTermParamV2 = new EmvTermParamV2();
            String deviceSN = getDeviceSN();
            int deviceSNLength = deviceSN.length();
            emvTermParamV2.ifDsn = deviceSN.substring(deviceSNLength - 8, deviceSNLength);//"3030303030393035";  // param.term_9F1E;
            emvTermParamV2.terminalType = terminalType;         // param.term_9F35;
            emvTermParamV2.countryCode = currencyCode;        // param.term_9F1A;
            emvTermParamV2.forceOnline = Boolean.FALSE;
            emvTermParamV2.getDataPIN = Boolean.TRUE;
            emvTermParamV2.surportPSESel = Boolean.TRUE;
            emvTermParamV2.useTermAIPFlg = Boolean.TRUE;
            emvTermParamV2.termAIP = Boolean.TRUE;
            emvTermParamV2.bypassAllFlg = Boolean.TRUE;
            emvTermParamV2.bypassPin = Boolean.FALSE;
            emvTermParamV2.batchCapture = Boolean.TRUE;
            emvTermParamV2.ectSiFlg = Boolean.TRUE;
            emvTermParamV2.ectSiVal = Boolean.TRUE;
            emvTermParamV2.ectTlFlg = Boolean.TRUE;
            emvTermParamV2.ectTlVal = ectTlVal;         // param.term_ectTlVal;
            emvTermParamV2.capability = capability;       // param.term_9F33;
            emvTermParamV2.addCapability = addCapability;// param.term_9F40;
            emvTermParamV2.scriptMode = Boolean.FALSE;
            emvTermParamV2.adviceFlag = Boolean.TRUE;
            emvTermParamV2.isSupportSM = Boolean.TRUE;
            emvTermParamV2.isSupportTransLog = Boolean.TRUE;
            emvTermParamV2.isSupportMultiLang = Boolean.TRUE;
            emvTermParamV2.isSupportExceptFile = Boolean.TRUE;
            emvTermParamV2.isSupportAccountSelect = Boolean.TRUE;
            emvTermParamV2.TTQ = TTQ;            // param.term_TTQ;
            emvTermParamV2.IsReadLogInCard = Boolean.FALSE;
            emvTermParamV2.currencyCode = currencyCode;
            emvOptV2.setTerminalParam(emvTermParamV2);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e(Constant.TAG, "initAIDAndRid fail");
        }
    }

    private static String getCapKey(CapKeysDataClass capKeysDataClass) {
        final StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("9F0605").append(capKeysDataClass.getRID());
        stringBuilder.append("9F2201").append(capKeysDataClass.getKeyIndex());
        stringBuilder.append(new TLV("DF02", capKeysDataClass.getModulus()).recoverToHexStr());
        stringBuilder.append("DF0401").append(capKeysDataClass.getExponent());
        stringBuilder.append(new TLV("DF03", capKeysDataClass.getCheckSum()).recoverToHexStr());
        stringBuilder.append("DF0601").append(capKeysDataClass.getHashInd());
        stringBuilder.append("DF0701").append(capKeysDataClass.getArithInd());

        if (!capKeysDataClass.getExpDate().equals("N/A")) {
            stringBuilder.append("DF0503").append(capKeysDataClass.getExpDate());
        }

        return String.valueOf(stringBuilder);
    }

    private static String getAid(AidKeysDataClass aidKeysDataClass) {
        final StringBuilder stringBuilder = new StringBuilder();

        if (aidKeysDataClass.getMode().equals("Contact")) {
            stringBuilder.append(new TLV("DFC10B", "01").recoverToHexStr());
        } else {
            stringBuilder.append(new TLV("DFC10B", "02").recoverToHexStr());

            //aidKeysDataClass.getCVMLimt());
            stringBuilder.append("DF2006").append(aidKeysDataClass.getContactlessTxnLimit()); //"000099999999");
            stringBuilder.append("DF2106").append(aidKeysDataClass.getCVMLimt());
        }

        stringBuilder.append("9F0607").append(aidKeysDataClass.getAID());
        stringBuilder.append("9F0902").append(aidKeysDataClass.getApplicationVersionNumber());

        stringBuilder.append("DF1105").append(aidKeysDataClass.getTACDefault());
        stringBuilder.append("DF1305").append(aidKeysDataClass.getTACDenial());

        stringBuilder.append("DF1205").append(aidKeysDataClass.getTACOnline());
        stringBuilder.append("9F1B04").append(aidKeysDataClass.getFloorLimit());//"00000000");

        if (!aidKeysDataClass.getTTQ().equals("N/A")) {
            stringBuilder.append("9F6604").append(aidKeysDataClass.getTTQ());
        }

        if (!aidKeysDataClass.getTDOL().equals("N/A")) {
            stringBuilder.append("DF810203").append(aidKeysDataClass.getTDOL());
        }

        if (!aidKeysDataClass.getCMVTerminalRiskManagement().equals("N/A")) {
            stringBuilder.append(new TLV("9F1D", aidKeysDataClass.getCMVTerminalRiskManagement()).recoverToHexStr());
        }

        stringBuilder.append("DF1906").append(aidKeysDataClass.getTermClssOfflineFloorLmt()); //termClssOfflineFloorLmt
        stringBuilder.append("9F3C02").append(aidKeysDataClass.getCountryCode());//countryCode
        stringBuilder.append("9F3D01").append(aidKeysDataClass.getRefCurrExponent()); // refCurrExponent
       // stringBuilder.append(new TLV("9F1C", "3030323939333538").recoverToHexStr()); //terminalId

       // stringBuilder.append("9F15024814"); // matcCatCode

        if (!aidKeysDataClass.getKernelUse().equals("N/A")) {
            stringBuilder.append(new TLV("DFC10A", aidKeysDataClass.getKernelUse()).recoverToHexStr());
        }

       // stringBuilder.append(new TLV("9F16", "393939393832303030202020202020").recoverToHexStr());//merchID

        return String.valueOf(stringBuilder).replaceAll("\\s", "");
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
