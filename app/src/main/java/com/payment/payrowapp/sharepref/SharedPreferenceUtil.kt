package com.payment.payrowapp.sharepref

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.payment.payrowapp.R
import com.payment.payrowapp.utils.Constants
import java.math.BigInteger


class SharedPreferenceUtil(context: Context) {

    private lateinit var securePreferences: SharedPreferences
    private var keyGenParameterSpec: KeyGenParameterSpec? = null


    private var mainKeyAlias: String? = null

    init {
        keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
        // mainKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec!!)

        val advancedSpec = KeyGenParameterSpec.Builder(
            MASTER_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).apply {
            setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            setKeySize(KEY_SIZE)
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                 setUnlockedDeviceRequired(false)
                 setRandomizedEncryptionRequired(true)
                 setCertificateSerialNumber(BigInteger.valueOf(1L))
                 try {
                     if (isStrongboxPM(context)) {
                         setIsStrongBoxBacked(true)
                     }
                 } catch (e: StrongBoxUnavailableException) {
                     e.printStackTrace()
                 }
             }
        }.build()

        val advancedKeyAlias = MasterKeys.getOrCreate(advancedSpec)

        try {
            securePreferences = EncryptedSharedPreferences.create(
                context.resources.getString(R.string.app_name),
                advancedKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun clearPreferences() {
        val editor: SharedPreferences.Editor = securePreferences.edit()
        editor.clear()
        editor.commit()
        //   securePreferences.all.clear()
    }

    private fun setPreference(key: String, value: String?) {
        with(securePreferences.edit()) {
            putString(key, value)
            commit()
        }
    }

    private fun setBoolPreference(key: String, value: Boolean?) {
        with(securePreferences!!.edit()) {
            putBoolean(key, value!!)
            commit()
        }
    }

    fun setLoginType(value: String?) {
        setPreference(Constants.LOGIN_TYPE, value)
    }

    fun setID(value: String?) {
        setPreference(Constants._ID, value)
    }

    fun setLoginToken(value: String?) {
        setPreference(Constants.LOGIN_TOKEN, value)
    }

    fun setMerchantID(value: String?) {
        setPreference(Constants.MERCHANT_ID, value)
    }

    fun setUserID(value: String?) {
        setPreference(Constants.USER_ID, value)
    }

    fun setRole(value: String?) {
        setPreference(Constants.ROLE, value)
    }

    fun setStoreID(value: String?) {
        setPreference(Constants.STORE_ID, value)
    }

    fun setMerchantMobileNumber(value: String?) {
        setPreference(Constants.MERCHANT_MOBILE_NUMBER, value)
    }

    fun setEmailID(value: String?) {
        setPreference(Constants.EMAIL_ID, value)
    }

    fun setDistributionID(value: String?) {
        setPreference(Constants.DISTRIBUTOR_ID, value)
    }

    fun setFirstName(value: String?) {
        setPreference(Constants.FIRST_NAME, value)
    }

    fun setLastName(value: String?) {
        setPreference(Constants.LAST_NAME, value)
    }

    fun setBusinessType(value: String?) {
        setPreference(Constants.BUSINESS_TYPE, value)
    }

    fun setBOBox(value: String?) {
        setPreference(Constants.BO_BOX, value)
    }

    fun setCountry(value: String?) {
        setPreference(Constants.COUNTRY, value)
    }

    fun setAddressDetails(value: String?) {
        setPreference(Constants.ADDRESS_DETAILS, value)
    }

    fun getMerchantName(): String {
        return securePreferences.getString(Constants.FIRST_NAME, "").toString()

    }

    fun getMerchantLastName(): String {
        return securePreferences.getString(Constants.LAST_NAME, "").toString()
    }

    fun getBusinessType(): String {
        return securePreferences.getString(Constants.BUSINESS_TYPE, "").toString()

    }

    fun getBOBox(): String {
        return securePreferences.getString(Constants.BO_BOX, "").toString()
    }

    fun getCountry(): String {
        return securePreferences.getString(Constants.COUNTRY, "").toString()

    }

    fun getAddress(): String {

        return securePreferences.getString(Constants.ADDRESS_DETAILS, "").toString()
    }

    fun getDistributorID(): String {
        return securePreferences.getString(Constants.DISTRIBUTOR_ID, "").toString()
    }

    fun getMailID(): String {
        return securePreferences.getString(Constants.EMAIL_ID, "").toString()
    }

    fun getMerchantID(): String {
        return securePreferences.getString(Constants.MERCHANT_ID, "").toString()
    }

    fun getStoreID(): String {
        return securePreferences.getString(Constants.STORE_ID, "").toString()
    }

    fun getUserRole(): String {
        return securePreferences.getString(Constants.ROLE, "").toString()
    }

    fun getUserID(): String {
        return securePreferences.getString(Constants.USER_ID, "").toString()
    }

    fun getEmailID(): String {
        return securePreferences.getString(Constants.EMAIL_ID, "").toString()
    }

    fun getMerchantMobileNumber(): String {
        return securePreferences.getString(Constants.MERCHANT_MOBILE_NUMBER, "").toString()
    }

    fun getAuthToken(): String {
        return securePreferences.getString(Constants.LOGIN_TOKEN, "").toString()
    }

    fun setISLogin(value: Boolean?) {
        setBoolPreference(Constants.IS_LOGIN, value)
    }

    fun setTerminalID(value: String?) {
        setPreference(Constants.TERMINAL_ID, value)
    }

    fun getTerminalID(): String {
        return securePreferences.getString(Constants.TERMINAL_ID, "").toString()
    }

    fun getISLogin(): Boolean {
        return securePreferences.getBoolean(Constants.IS_LOGIN, false)
    }

    fun setAmount(value: String?) {
        setPreference(Constants.AMOUNT, value)
    }

    fun getAmount(): String {
        return securePreferences.getString(Constants.AMOUNT, "").toString()
    }

    fun setConfigurationFiles(value: String?) {
        setPreference(Constants.CONFIG_FILES, value)
    }

    fun getConfigFiles(): String {
        return securePreferences.getString(Constants.CONFIG_FILES, "").toString()
    }

    fun setAdditionalInfo(value: String?) {
        setPreference(Constants.AdditionalInfo, value)
    }

    fun geAdditionalInfo(): String {
        return securePreferences.getString(Constants.AdditionalInfo, "").toString()
    }

    fun setCAPublicKeys(value: String?) {
        setPreference(Constants.CA_PUBLIC_KEYS, value)
    }

    fun getCAPublicKeys(): String {
        return securePreferences.getString(Constants.CA_PUBLIC_KEYS, "").toString()
    }

    fun setCertificateRevocationList(value: String?) {
        setPreference(Constants.CERTIFICATE_REVOCATION_LIST, value)
    }

    fun getCertificateRevocationList(): String {
        return securePreferences.getString(Constants.CERTIFICATE_REVOCATION_LIST, "").toString()
    }


    fun setDefaultTerminalData(value: String?) {
        setPreference(Constants.DEFAULT_TERMINAL_DATA, value)
    }

    fun getDefaultTerminalData(): String {
        return securePreferences.getString(Constants.DEFAULT_TERMINAL_DATA, "").toString()
    }

    fun setExceptionFile(value: String?) {
        setPreference(Constants.EXCEPTION_FILE, value)
    }

    fun getExceptionFile(): String {
        return securePreferences.getString(Constants.EXCEPTION_FILE, "").toString()
    }

    fun setManifest(value: String?) {
        setPreference(Constants.MANIFEST, value)
    }

    fun getManifest(): String {
        return securePreferences.getString(Constants.MANIFEST, "").toString()
    }

    fun setProfileData(value: String?) {
        setPreference(Constants.PROFILE_DATA, value)
    }

    fun getProfileData(): String {
        return securePreferences.getString(Constants.PROFILE_DATA, "").toString()
    }

    fun setProfileReaderActivate(value: String?) {
        setPreference(Constants.PROFILE_READER_ACTIVATE, value)
    }

    fun getProfileReaderActivate(): String {
        return securePreferences.getString(Constants.PROFILE_READER_ACTIVATE, "").toString()
    }

    fun setProfileReaderInit(value: String?) {
        setPreference(Constants.PROFILE_READER_INITIALISE, value)
    }

    fun getProfileReaderInit(): String {
        return securePreferences.getString(Constants.PROFILE_READER_INITIALISE, "").toString()
    }

    fun setReferenceTags(value: String?) {
        setPreference(Constants.REFERENCE_TAGS, value)
    }

    fun getReferenceTags(): String {
        return securePreferences.getString(Constants.REFERENCE_TAGS, "").toString()
    }

    fun setTLVTerminal(value: String?) {
        setPreference(Constants.TLV_TERMINAL, value)
    }

    fun getTLVTerminal(): String {
        return securePreferences.getString(Constants.TLV_TERMINAL, "").toString()
    }

    fun setV3DataDictionary(value: String?) {
        setPreference(Constants.V3_DATA_DICTIONARY, value)
    }

    fun getV3DataDictionary(): String {
        return securePreferences.getString(Constants.V3_DATA_DICTIONARY, "").toString()
    }

    fun setAESKey(value: String?) {
        setPreference(Constants.AES_KEY, value)
    }

    fun getAESKey(): String {
        return securePreferences.getString(Constants.AES_KEY, "").toString()
    }

    fun setIV(value: String?) {
        setPreference(Constants.IV, value)
    }

    fun getIV(): String {
        return securePreferences.getString(Constants.IV, "").toString()
    }


    fun setMIT(value: String?) {
        setPreference(MTI, value)
    }

    fun getMIT(): String {
        return securePreferences.getString(MTI, "").toString()
    }

    fun setProcessingCode(value: String?) {
        setPreference(PROCESSING_CODE, value)
    }

    fun getProcessingCode(): String {
        return securePreferences.getString(PROCESSING_CODE, "").toString()
    }

    fun setSystemTraceAuditNumber(value: String?) {
        setPreference(SYSTEM_TRACE_AUDIT_NUMBER, value)
    }

    fun getSystemTraceAuditNumber(): String {
        return securePreferences.getString(SYSTEM_TRACE_AUDIT_NUMBER, "").toString()
    }

    fun setMerchantType(value: String?) {
        setPreference(MERCHANT_TYPE, value)
    }

    fun getMerchantType(): String {
        return securePreferences.getString(MERCHANT_TYPE, "").toString()
    }

    fun setPOSEntryMode(value: String?) {
        setPreference(POS_ENTRY_MODE, value)
    }

    fun getPOSEntryMode(): String {
        return securePreferences.getString(POS_ENTRY_MODE, "").toString()
    }

    fun setAppPanSequenceNo(value: String?) {
        setPreference(APP_PAN_SEQUENCE_NO, value)
    }

    fun getAppPanSequenceNo(): String {
        return securePreferences.getString(APP_PAN_SEQUENCE_NO, "").toString()
    }

    fun setPOSConditionCode(value: String?) {
        setPreference(POS_CONDITION_CODE, value)
    }

    fun getPOSConditionCode(): String {
        return securePreferences.getString(POS_CONDITION_CODE, "").toString()
    }

    fun setAccInstIdCode(value: String?) {
        setPreference(ACQUIRER_INST_ID_CODE, value)
    }

    fun getAccInstIdCode(): String {
        return securePreferences.getString(ACQUIRER_INST_ID_CODE, "").toString()
    }

    fun setPOSPinCaptureCode(value: String?) {
        setPreference(POS_PIN_CAPTURE_CODE, value)
    }

    fun getPOSPinCaptureCode(): String {
        return securePreferences.getString(POS_PIN_CAPTURE_CODE, "").toString()
    }

    fun setCardAcceptorTerminalId(value: String?) {
        setPreference(CARD_ACCEPTOR_TERMINAL_ID, value)
    }

    fun getCardAcceptorTerminalId(): String {
        return securePreferences.getString(CARD_ACCEPTOR_TERMINAL_ID, "").toString()
    }

    fun setCardAcceptorIdCode(value: String?) {
        setPreference(CARD_ACCEPTOR_ID_CODE, value)
    }

    fun getCardAcceptorIdCode(): String {
        return securePreferences.getString(CARD_ACCEPTOR_ID_CODE, "").toString()
    }

    fun setCardAcceptorNameLocation(value: String?) {
        setPreference(CARD_ACCEPTOR_NAME_LOCATION, value)
    }

    fun getCardAcceptorNameLocation(): String {
        return securePreferences.getString(CARD_ACCEPTOR_NAME_LOCATION, "").toString()
    }

    fun setCurrencyCode(value: String?) {
        setPreference(CURRENCY_CODE, value)
    }

    fun getCurrencyCode(): String {
        return securePreferences.getString(CURRENCY_CODE, "").toString()
    }

    fun setSponsorBank(value: String?) {
        setPreference(SPONSOR_BANK, value)
    }

    fun getSponsorBank(): String {
        return securePreferences.getString(SPONSOR_BANK, "").toString()
    }

    fun setTerminalType(value: String?) {
        setPreference(TERMINAL_TYPE, value)
    }

    fun getTerminalType(): String {
        return securePreferences.getString(TERMINAL_TYPE, "").toString()
    }

    fun setPosGeoData(value: String?) {
        setPreference(POS_GEO_DATA, value)
    }

    fun getPosGeoData(): String {
        return securePreferences.getString(POS_GEO_DATA, "").toString()
    }

    fun setURN(value: String?) {
        setPreference(URN, value)
    }

    fun getURN(): String {
        return securePreferences.getString(URN, "").toString()
    }

    fun setPayByLinkID(value: String?) {
        setPreference(PAY_BY_LINK_ID, value)
    }

    fun getPayByLinkID(): String {
        return securePreferences.getString(PAY_BY_LINK_ID, "").toString()
    }

    fun setVoidMTI(value: String?) {
        setPreference(VOID_MTI, value)
    }

    fun getVoidMTI(): String {
        return securePreferences.getString(VOID_MTI, "").toString()
    }


    fun setAlgorithm(value: String?) {
        setPreference(AES_ALGORITHM, value)
    }

    fun getAlgorithm(): String {
        return securePreferences.getString(AES_ALGORITHM, "").toString()
    }

    fun setAlg(value: String?) {
        setPreference(AES_ALG, value)
    }

    fun getAlg(): String {
        return securePreferences.getString(AES_ALG, "").toString()
    }

    fun setTMK(value: Int?) {
        with(securePreferences.edit()) {
            putInt("TMK", value!!)
            commit()
        }
    }

    fun getTMK(): Int {
        return securePreferences.getInt("TMK", 0)
    }

    fun setPKI(value: Int?) {
        with(securePreferences.edit()) {
            putInt("PKI", value!!)
            commit()
        }
    }

    fun getPKI(): Int {
        return securePreferences.getInt("PKI", 0)
    }

    fun setOrderNum(value: String?) {
        setPreference("orderNum", value)
    }

    fun getOrderNum(): String {
        return securePreferences.getString("orderNum", "").toString()
    }

    fun setQRStoreID(value: String?) {
        setPreference("storeID", value)
    }

    fun getQRStoreID(): String {
        return securePreferences.getString("storeID", "").toString()
    }


    fun setQRDistID(value: String?) {
        setPreference("distributorId", value)
    }

    fun getQRDistID(): String {
        return securePreferences.getString("distributorId", "").toString()
    }

    fun setQRUser(value: String?) {
        setPreference("userId", value)
    }

    fun getQRUser(): String {
        return securePreferences.getString("userId", "").toString()
    }

    fun setQRMerEmail(value: String?) {
        setPreference("QRmerchantEmail", value)
    }

    fun getQRMerEmail(): String {
        return securePreferences.getString("QRmerchantEmail", "").toString()
    }

    fun setQRReNo(value: String?) {
        setPreference("receiptNo", value)
    }

    fun getQRReNo(): String {
        return securePreferences.getString("receiptNo", "").toString()
    }


    fun setQRTrNo(value: String?) {
        setPreference("trnNo", value)
    }

    fun getQRTrNo(): String {
        return securePreferences.getString("trnNo", "").toString()
    }

    fun setQRPInvoiceNo(value: String?) {
        setPreference("payrowInvoiceNo", value)
    }

    fun getQRPInvoiceNo(): String {
        return securePreferences.getString("payrowInvoiceNo", "").toString()
    }

    fun setQRPosType(value: String?) {
        setPreference("posType", value)
    }

    fun getQRPosType(): String {
        return securePreferences.getString("posType", "").toString()
    }

    fun setQRPosID(value: String?) {
        setPreference("posId", value)
    }

    fun getQRPosID(): String {
        return securePreferences.getString("posId", "").toString()
    }

    fun setQRTransURL(value: String?) {
        setPreference("bankTransferURL", value)
    }

    fun getQRTransURL(): String {
        return securePreferences.getString("bankTransferURL", "").toString()
    }


    fun setQRCheckID(value: String?) {
        setPreference("checkoutId", value)
    }

    fun getQRCheckID(): String {
        return securePreferences.getString("checkoutId", "").toString()
    }

    fun setQRCName(value: String?) {
        setPreference("customerName", value)
    }

    fun getQRCName(): String {
        return securePreferences.getString("customerName", "").toString()
    }

    fun setQRCCountry(value: String?) {
        setPreference("customerBillingCountry", value)
    }

    fun getQRCCountry(): String {
        return securePreferences.getString("customerBillingCountry", "").toString()
    }

    fun setQRCCity(value: String?) {
        setPreference("customerBillingCity", value)
    }

    fun getQRCCity(): String {
        return securePreferences.getString("customerBillingCity", "").toString()
    }

    fun setQRCState(value: String?) {
        setPreference("customerBillingState", value)
    }

    fun getQRCState(): String {
        return securePreferences.getString("customerBillingState", "").toString()
    }

    fun setQRCPCode(value: String?) {
        setPreference("customerBillingPostalCode", value)
    }

    fun getQRCPCode(): String {
        return securePreferences.getString("customerBillingPostalCode", "").toString()
    }

    fun setQRCPhone(value: String?) {
        setPreference("customerPhone", value)
    }

    fun getQRCPhone(): String {
        return securePreferences.getString("customerPhone", "").toString()
    }

    fun setQRCEmail(value: String?) {
        setPreference("customerEmail", value)
    }

    fun getQRCEmail(): String {
        return securePreferences.getString("customerEmail", "").toString()
    }


    fun setMerchantEmail(value: String?) {
        setPreference("merchantEmail", value)
    }

    fun getMerchantEmail(): String {
        return securePreferences.getString("merchantEmail", "").toString()
    }

    fun setMerchantPhone(value: String?) {
        setPreference("merchantMobile", value)
    }

    fun getMerchantPhone(): String {
        return securePreferences.getString("merchantMobile", "").toString()
    }

    fun setBussinessId(value: String?) {
        setPreference("businessId", value)
    }

    fun getBussinessId(): String {
        return securePreferences.getString("businessId", "").toString()
    }

    fun setCity(value: String?) {
        setPreference("city", value)
    }

    fun getCity(): String {
        return securePreferences.getString("city", "").toString()
    }

    fun setPKey(value: String?) {
        setPreference("pKey", value)
    }

    fun getPKey(): String {
        return securePreferences.getString("pKey", "").toString()
    }

    fun setZKey(value: String?) {
        setPreference("zpkKey", value)
    }

    fun getZKey(): String {
        return securePreferences.getString("zpkKey", "").toString()
    }

    fun setCType(value: String?) {
        setPreference("CardType", value)
    }

    fun getCType(): String {
        return securePreferences.getString("CardType", "").toString()
    }

    fun setAID(value: String?) {
        setPreference(AID, value)
    }

    fun getAID(): String {
        return securePreferences.getString(AID, "").toString()
    }

    fun setAC(value: String?) {
        setPreference(AC, value)
    }

    fun getAC(): String {
        return securePreferences.getString(AC, "").toString()
    }

    fun setACInfo(value: String?) {
        setPreference(AC_INFO, value)
    }

    fun getACInfo(): String {
        return securePreferences.getString(AC_INFO, "").toString()
    }

    fun setTVR(value: String?) {
        setPreference(TVR, value)
    }

    fun getTVR(): String {
        return securePreferences.getString(TVR, "").toString()
    }

    fun setTransactionType(value: Int?) {
        with(securePreferences.edit()) {
            putInt(TRANSACTION_TYPE, value!!)
            commit()
        }
    }

    fun getTransactionType(): Int {
        return securePreferences.getInt(TRANSACTION_TYPE, 0)
    }

    fun setReportID(value: String?) {
        setPreference(REPORT_ID, value)
    }

    fun getReportID(): String {
        return securePreferences.getString(SALE_ID, "").toString()
    }

    fun setSaleID(value: String?) {
        setPreference(SALE_ID, value)
    }

    fun getSaleID(): String {
        return securePreferences.getString(SALE_ID, "").toString()
    }

    fun setTTQ(value: String?) {
        setPreference(TTQ, value)
    }

    fun getTTQ(): String {
        return securePreferences.getString(TTQ, "").toString()
    }

    fun setChipCapability(value: String?) {
        setPreference(CHIP_CAPABILITY, value)
    }

    fun getChipCapability(): String {
        return securePreferences.getString(CHIP_CAPABILITY, "").toString()
    }

    fun setTapCapability(value: String?) {
        setPreference(TAP_CAPABILITY, value)
    }

    fun getTapCapability(): String {
        return securePreferences.getString(TAP_CAPABILITY, "").toString()
    }

    fun setCurrencyExponent(value: String?) {
        setPreference(Currency_Exponent, value)
    }

    fun getCurrencyExponent(): String {
        return securePreferences.getString(Currency_Exponent, "").toString()
    }

    fun setKBPK(value: String?) {
        setPreference(KPBK, value)
    }

    fun getKBPK(): String {
        return securePreferences.getString(KPBK, "").toString()
    }

    fun setUUID(value: String?) {
        setPreference(UUID, value)
    }

    fun getUUID(): String {
        return securePreferences.getString(UUID, "").toString()
    }


    fun setVATCalculator(value: Boolean) {
        setBoolPreference(VAT_CALCULATOR, value)
    }

    fun getVATCalculator(): Boolean {
        return securePreferences.getBoolean(VAT_CALCULATOR, false)
    }


    fun setCataLogAmount(value: Boolean) {
        setBoolPreference(CATALOG_AMOUNT, value)
    }

    fun getCataLogAmount(): Boolean {
        return securePreferences.getBoolean(CATALOG_AMOUNT, false)
    }

    fun setSecretKey(value: String?) {
        setPreference(SECRET_KEY, value)
    }

    fun getSecretKey(): String {
        return securePreferences.getString(SECRET_KEY, "").toString()
    }

    fun setSignatureKey(value: String?) {
        setPreference(SIGNATURE_KEY, value)
    }

    fun getSignatureKey(): String {
        return securePreferences.getString(SIGNATURE_KEY, "").toString()
    }

    fun setGatewayMerchantID(value: String?) {
        setPreference(GATEWAY_MERCHANT_ID, value)
    }

    fun getGatewayMerchantID(): String {
        return securePreferences.getString(GATEWAY_MERCHANT_ID, "").toString()
    }


    fun setAuthKey(value: String?) {
        setPreference(AUTH_KEY, value)
    }

    fun getAuthKey(): String {
        return securePreferences.getString(AUTH_KEY, "").toString()
    }

    fun setHashKey(value: String?) {
        setPreference(HASH_KEY, value)
    }

    fun getHashKey(): String {
        return securePreferences.getString(HASH_KEY, "").toString()
    }

    fun setScanBarCode(value: Boolean) {
        setBoolPreference(SCAN_BAR_CODE, value)
    }

    fun getScanBarCode(): Boolean {
        return securePreferences.getBoolean(SCAN_BAR_CODE, false)
    }


    fun setPayRowDigital(value: Boolean) {
        setBoolPreference(PAYROW_DIGITAL_FEE, value)
    }

    fun getPayRowDigital(): Boolean {
        return securePreferences.getBoolean(PAYROW_DIGITAL_FEE, false)
    }

    fun setLastLogin(value: Long) {
        setLongPreference(LAST_LOGIN, value)
    }

    fun getLastLogin(): Long {
        return securePreferences.getLong(LAST_LOGIN, 0)
    }

    private fun setLongPreference(key: String, value: Long?) {
        with(securePreferences.edit()) {
            putLong(key, value!!)
            commit()
        }
    }

    companion object {
        const val LAST_LOGIN ="Last Login Time"
        const val PAYROW_DIGITAL_FEE = "PayRow Digital Fee"
        const val SCAN_BAR_CODE = "Scan Bar Code"
        const val AUTH_KEY = "Auth Key"
        const val HASH_KEY = "Hash Key"
        const val GATEWAY_MERCHANT_ID = "gateway merchant id"
        const val SIGNATURE_KEY = "signature key"
        const val SECRET_KEY = "secret key"
        const val CATALOG_AMOUNT  = "catalog amount"
        const val VAT_CALCULATOR = "vat calculator"
        const val UUID = "uuid"
        const val KPBK = "kbpk"
        const val Currency_Exponent = "currencyExponent"
        const val TAP_CAPABILITY = "TapCapability"
        const val CHIP_CAPABILITY = "ChipCapability"
        const val TTQ = "ttq"
        const val SALE_ID = "Sale ID"
        const val REPORT_ID = "Report ID"
        const val TRANSACTION_TYPE = "Transaction Type"
        const val TVR = "TVR"
        const val AC_INFO = "AC Info"
        const val AC = "AC"
        const val AID = "AID"
        const val AES_ALG = "AES ALG"
        const val AES_ALGORITHM = "AES Algorithm"
        const val MTI = "MTI"
        const val PROCESSING_CODE = "Processing Code"
        const val SYSTEM_TRACE_AUDIT_NUMBER = "System Trade Audit Number"
        const val MERCHANT_TYPE = "Merchant Type"
        const val POS_ENTRY_MODE = "POS Entry Mode"
        const val APP_PAN_SEQUENCE_NO = "App PAN Seq No"
        const val POS_CONDITION_CODE = "POS Condition COde"
        const val ACQUIRER_INST_ID_CODE = "Acquirer INST ID Code"
        const val POS_PIN_CAPTURE_CODE = "POS Pin Capture Code"
        const val CARD_ACCEPTOR_TERMINAL_ID = "Card Acceptor Terminal ID"
        const val CARD_ACCEPTOR_ID_CODE = "Card Acceptor ID Code"
        const val CARD_ACCEPTOR_NAME_LOCATION = "Card Acceptor Name Location"
        const val CURRENCY_CODE = "Currency Code"
        const val SPONSOR_BANK = "Sponsor Bank"
        const val TERMINAL_TYPE = "Terminal Type"
        const val POS_GEO_DATA = "POS GEO Data"
        const val URN = "URN"
        const val PAY_BY_LINK_ID = "PayByLinkId"
        const val VOID_MTI = "Void MTI"

        const val KEY_SIZE = 256
        const val MASTER_KEY_ALIAS = "_androidx_security_master_key_"
    }

    private fun isStrongboxPM(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            context.packageManager
                .hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)
        } else {
            false
        }
    }

}