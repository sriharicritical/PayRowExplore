package com.payment.payrowapp.crypto;

import android.security.keystore.KeyProperties;

import com.payment.payrowapp.retrofit.ApiKeys;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptDecrypt {
    private static SecretKeySpec secretKey;
    private static final int GCM_TAG_LENGTH = 16;

    public static void setKey(String aesKey) {
        try {
            byte[] key = Base64.getDecoder().decode(aesKey);
            secretKey = new SecretKeySpec(key, "AES");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String decrypt(String strToDecrypt, String secret, String iv, String alg) {
        try {
            setKey(secret);
            IvParameterSpec ivspec = new IvParameterSpec(Base64.getDecoder().decode(iv));
            Cipher cipher = Cipher.getInstance(alg);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
            return new String(cipher.doFinal(hexStringToByteArray(strToDecrypt)));
        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e);
        }
        return null;
    }

    public static String encrypt(String strToEncrypt, String secret, String iv, String alg) {
        try {
            setKey(secret);
            IvParameterSpec ivspec = new IvParameterSpec(Base64.getDecoder().decode(iv));
            Cipher cipher = Cipher.getInstance(alg);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
            return byteArrayToHexString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
            // return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e);
        }
        return null;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String byteArrayToHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }


    public static byte[] generateKey(String deviceId, String keyValidation) throws NoSuchAlgorithmException {
        // Initialize MessageDigest with SHA-256 algorithm
        MessageDigest sha256Digest = MessageDigest.getInstance("SHA-256");

        // Update digest with deviceId and keyValidation strings
        sha256Digest.update(deviceId.getBytes(StandardCharsets.UTF_8));
        sha256Digest.update(keyValidation.getBytes(StandardCharsets.UTF_8));

        // Complete the hash and slice to 32 bytes
        byte[] fullHash = sha256Digest.digest();
        return Arrays.copyOf(fullHash, 32);  // AES-256 requires a 32-byte key
    }


    public static String decryptDataGCM(String encryptedData, String validation, String authTag, byte[] key) throws Exception {
        byte[] iv = Base64.getDecoder().decode(validation); // Decode IV from Base64
        byte[] cipherText = Base64.getDecoder().decode(encryptedData); // Decode Encrypted Data from Base64
        byte[] authTagBytes = Base64.getDecoder().decode(authTag); // Decode Auth Tag from Base64

        // Combine cipherText + authTag (needed for Java decryption)
        byte[] encryptedBytes = new byte[cipherText.length + authTagBytes.length];
        System.arraycopy(cipherText, 0, encryptedBytes, 0, cipherText.length);
        System.arraycopy(authTagBytes, 0, encryptedBytes, cipherText.length, authTagBytes.length);

        // Initialize Cipher
        Cipher cipher = Cipher.getInstance(ApiKeys.AES_GCM_Algorithm);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, KeyProperties.KEY_ALGORITHM_AES);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmParameterSpec);

        // Decrypt Data
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}

