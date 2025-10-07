package com.payment.payrowapp.mastercloud;


import android.content.Context;

import com.payment.payrowapp.R;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.security.cert.CertificateFactory;


public class CryptoUtils {

    public static X509Certificate loadCertificateFromRaw(Context context) {
        X509Certificate certificate = null;
        try {
            // Open the raw certificate file
            InputStream is = context.getResources().openRawResource(Integer.parseInt(""));//R.raw.mtf_publickey); // Change "certificate" to your file name

            // Create a CertificateFactory to parse the X.509 certificate
            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            // Generate the X509Certificate object from the InputStream
            certificate = (X509Certificate) cf.generateCertificate(is);

            // Close the InputStream
            is.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return certificate;
    }
    public static String getSHA256Fingerprint(X509Certificate cert) throws Exception {
        MessageDigest md = MessageDigest.getInstance("256");
        byte[] der = cert.getEncoded();
        md.update(der);
        byte[] shaFingerprint = md.digest();
        return Base64.getEncoder().encodeToString(shaFingerprint);
    }
}
