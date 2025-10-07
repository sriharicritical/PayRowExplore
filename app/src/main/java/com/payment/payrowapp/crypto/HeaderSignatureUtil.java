package com.payment.payrowapp.crypto;

import android.content.Context;
import android.util.Log;

import com.payment.payrowapp.retrofit.ApiKeys;
import com.payment.payrowapp.sharepref.SharedPreferenceUtil;
import com.payment.payrowapp.utils.ContextUtils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HeaderSignatureUtil {

    public static String SecretKey(String httpMethod, String Uri, String timeStamp, Context context) {
        // Create the signature
        Mac mac = null;
        String signature = null;

        // Create a string to sign
        String strUUID = ContextUtils.Companion.generateUUID();
        SharedPreferenceUtil sharedpreferenceUtil = new SharedPreferenceUtil(context);
        sharedpreferenceUtil.setUUID(strUUID);

      //  String Code = getMd5(ContextUtils.Companion.getDeviceId(context) + strUUID);
        String stringToSign = httpMethod + "|" + ContextUtils.Companion.getDeviceId(context) + "|" +
                strUUID + "|"  + timeStamp + "|" + Uri;
        try {
            mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(sharedpreferenceUtil.getSecretKey().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] rawHmac = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            signature = Base64.getEncoder().encodeToString(rawHmac);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return signature;
    }

    public static String getMd5(String input) {
        try {

            // Static getInstance method is called with hashing MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // digest() method is called to calculate message digest
            // of an input digest() return array of byte
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String CreateHeaderSignature(SharedPreferenceUtil sharedPreferenceUtil,String httpMethod, String Uri, String jsonString2) {
        // Create the signature
        Mac mac = null;
        String signature = null;

        // Create a string to sign
        String queryParams = "";
        String stringToSign = httpMethod + "\n" + Uri + "\n" + queryParams + "\n" + jsonString2;
        Log.v("signatureKey", stringToSign);
        try {
            mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(sharedPreferenceUtil.getSignatureKey().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] rawHmac = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            signature = Base64.getEncoder().encodeToString(rawHmac);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return signature;
    }

    public static String getDeviceSN() {
        String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.getDefault());
        return uuid;
       /* try {
            return MyApplication.app.basicOptV2.getSysParam(AidlConstantsV2.SysParam.SN);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;*/
    }

    public static String getISO() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tz);
        return df.format(new Date());
    }
}
