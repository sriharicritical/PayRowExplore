package com.payment.payrowapp.retrofit;

import android.security.keystore.KeyProperties;

public class ApiKeys {

    public static String AES_Algorithm = KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/PKCS5Padding";

    public static String AES_GCM_Algorithm = KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_GCM + "/" +
            KeyProperties.ENCRYPTION_PADDING_NONE;
}
