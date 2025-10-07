package com.payment.payrowapp.sunmipay;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;

import com.payment.payrowapp.R;
import com.payment.payrowapp.app.MyApplication;
import com.payment.payrowapp.cardpayment.CardPaymentViewModel;
import com.payment.payrowapp.cardpayment.CardPaymentViewModelFactory;
import com.payment.payrowapp.dashboard.DashboardActivity;
import com.payment.payrowapp.dataclass.ContactlessConfiguration;
import com.payment.payrowapp.dataclass.FailedRequstClass;
import com.payment.payrowapp.dataclass.FeeResponseData;
import com.payment.payrowapp.dataclass.FeeServiceData;
import com.payment.payrowapp.dataclass.PurchaseBreakdownDetails;
import com.payment.payrowapp.dataclass.PurchaseRequest;
import com.payment.payrowapp.dataclass.ResultRequestClass;
import com.payment.payrowapp.dataclass.SingleTapPinRequest;
import com.payment.payrowapp.newpayment.PaymentConfirmationActivity;
import com.payment.payrowapp.newpayment.TinyDB;
import com.payment.payrowapp.sharepref.SharedPreferenceUtil;
import com.payment.payrowapp.utils.BaseActivity;
import com.payment.payrowapp.utils.Constants;
import com.payment.payrowapp.utils.MoneyUtil;
import com.payment.payrowapp.utils.UtilityClass;
import com.sunmi.pay.hardware.aidl.AidlConstants;
import com.sunmi.pay.hardware.aidl.SPErrorCode;
import com.sunmi.pay.hardware.aidl.bean.CardInfo;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;
import com.sunmi.pay.hardware.aidlv2.AidlErrorCodeV2;
import com.sunmi.pay.hardware.aidlv2.bean.EMVCandidateV2;
import com.sunmi.pay.hardware.aidlv2.bean.EMVTransDataV2;
import com.sunmi.pay.hardware.aidlv2.bean.PinPadConfigV2;
import com.sunmi.pay.hardware.aidlv2.emv.EMVListenerV2;
import com.sunmi.pay.hardware.aidlv2.emv.EMVOptV2;
import com.sunmi.pay.hardware.aidlv2.pinpad.PinPadListenerV2;
import com.sunmi.pay.hardware.aidlv2.pinpad.PinPadOptV2;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;
import com.sunmi.pay.hardware.aidlv2.readcard.ReadCardOptV2;

import org.json.JSONObject;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.arnaudguyon.xmltojsonlib.XmlToJson;

/**
 * This page show the emv procedure.
 * Any transaction which should do emv process can refer
 * to this page.
 */
public class ICProcessActivity extends BaseActivity implements View.OnClickListener, TransactionCallback {

    private EMVOptV2 mEMVOptV2;
    private PinPadOptV2 mPinPadOptV2;
    private ReadCardOptV2 mReadCardOptV2;

    private EditText mEditAmount;

    private int mCardType;  // card type
    private String mCardNo; // card number
    private int mPinType;   // 0-online pin, 1-offline pin
    private int mSelectIndex;

    private int mAppSelect = 0;

    private int mProcessStep = 0;
    private AlertDialog mAppSelectDialog;

    private static final int CARD_READ_FAILED = 2000;
    private static final int EMV_APP_SELECT = 1;
    private static final int EMV_FINAL_APP_SELECT = 2;
    private static final int EMV_CONFIRM_CARD_NO = 3;
    private static final int EMV_CERT_VERIFY = 4;
    private static final int EMV_SHOW_PIN_PAD = 5;
    private static final int EMV_ONLINE_PROCESS = 6;
    private static final int EMV_SIGNATURE = 7;
    private static final int EMV_TRANS_SUCCESS = 888;
    private static final int EMV_TRANS_FAIL = 999;
    private static final int REMOVE_CARD = 1000;

    private static final int PIN_CLICK_NUMBER = 50;
    private static final int PIN_CLICK_PIN = 51;
    private static final int PIN_CLICK_CONFIRM = 52;
    private static final int PIN_CLICK_CANCEL = 53;
    private static final int PIN_ERROR = 54;
    private LoadingDialog loadDialog;

    String expiryDate2;
    String serviceCode2;
    String appIdentifier;
    private final Handler dlgHandler = new Handler();
    String amount;
    String paymentTypeChan;
    PurchaseBreakdownDetails purchaseDetails;
    Button cancelBtn;
    TextView tvTimer;
    String pinblockStr;
    String payRowDigitFee;

    String paymentType = "unknown";
    SharedPreferenceUtil sharedPreferenceUtil;
    boolean fallBack = false;
    String track2Data;
    String track1;
    String panSequenceNumber;
    CountDownTimer countDownTimer;
    Map<String, TLV> field55Data = new TreeMap<>();
    String ksnStr;
    private String mTag9F06Value;
    boolean signatureStatus = false;
    boolean onlineProc = false, isPinBlock = false;
    String[] iccValues = {"", "", "", "", ""};
    ResultRequestClass resultRequestClass;
    String magTrack2Data;
    Boolean onlineDecline = false, singleTap = false;
    FailedRequstClass declineOrderRequest;
    CardPaymentViewModel cardPaymentViewModel;
    SingleTapPinRequest singleTapPinRequest;
    Boolean pinBlockStatus;
    Boolean pinAvailable = false;
    Boolean payRowVATStatus;
    Float payRowVATAmount;
    ArrayList<FeeServiceData> feeServiceData = new ArrayList<>();
    FeeResponseData feeResponseData;
    ProgressDialog progressDialog;
    String orderNumber;


    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case EMV_FINAL_APP_SELECT:
                    importFinalAppSelectStatus(0);
                    break;
                case EMV_APP_SELECT:
                    dismissLoadingDialog();
                    String[] candiNames = (String[]) msg.obj;
                    mAppSelectDialog = new AlertDialog.Builder(ICProcessActivity.this)
                            .setTitle(R.string.emv_app_select)
                            .setNegativeButton(R.string.cancel, (dialog, which) -> importAppSelect(-1)
                            )
                            .setPositiveButton(R.string.ok, (dialog, which) -> {
                                        showLoadingDialog(R.string.handling);
                                        importAppSelect(mSelectIndex);
                                    }
                            )
                            .setSingleChoiceItems(candiNames, 0, (dialog, which) -> {
                                        mSelectIndex = which;
                                        LogUtil.e(Constant.TAG, "singleChoiceItems which:" + which);
                                    }
                            ).create();
                    mSelectIndex = 0;
                    mAppSelectDialog.show();
                    break;
                case EMV_CONFIRM_CARD_NO:
                    importCardNoStatus(0);
                    break;
                case EMV_CERT_VERIFY:
                    importCertStatus(0);
                    break;
                case EMV_SHOW_PIN_PAD:
                    dismissLoadingDialog();
                    initPinPad();
                    break;
                case EMV_ONLINE_PROCESS:
                    mockRequestToServer();
                    break;
                case EMV_SIGNATURE:
                    importSignatureStatus(0);
                    break;
                case PIN_CLICK_NUMBER:
                    break;
                case PIN_CLICK_PIN:

                    if (singleTap) {
                        singleTapTransaction();
                    } else if (mCardType == AidlConstantsV2.CardType.MAGNETIC.getValue()) {
                        // showToast("Success");
                        makeMagCardTransaction();
                        // resetUI(true, "", 0);
                    } else {
                        importPinInputStatus(0);
                    }
                    break;
                case PIN_CLICK_CONFIRM:
                    if (mCardType != AidlConstantsV2.CardType.MAGNETIC.getValue() && !singleTap) {
                        importPinInputStatus(2);
                    }
                    break;
                case PIN_CLICK_CANCEL:
                    showToast("user cancel");
                    if (mCardType == AidlConstantsV2.CardType.MAGNETIC.getValue()) {
                        cardFailed("user cancel the pin", 1000);
                    } else {
                        importPinInputStatus(1);
                    }
                    break;
                case PIN_ERROR:
                    showToast("error:" + msg.obj + " -- " + msg.arg1);
                    if (mCardType == AidlConstantsV2.CardType.MAGNETIC.getValue()) {
                        cardFailed((String) msg.obj, msg.arg1);
                    } else {
                        importPinInputStatus(3);
                    }
                    break;
                case EMV_TRANS_FAIL:
                    resetUI(false, (String) msg.obj, msg.arg1);
                    // dismissLoadingDialog();
                    showToast("error:" + msg.obj + " -- " + msg.arg1);
                    break;
                case EMV_TRANS_SUCCESS:
                    resetUI(true, "", 0);

                    checkAndRemoveCard();
                    //  showToast("Success");
                    break;
                case REMOVE_CARD:
                    checkAndRemoveCard();
                    break;
                case CARD_READ_FAILED:
                    cardFailed((String) msg.obj, msg.arg1);
                    showToast("error:" + msg.obj + " -- " + msg.arg1);
                    break;
            }
        }
    };

    private void singleTapTransaction() {
        runOnUiThread(() -> {
            // showLoadingDialog("Please wait");
            PurchaseRequest purchaseRequest = singleTapPinRequest.getPurchaseRequest();

            if (pinblockStr != null) {
                pinAvailable = true;
            }

            if (pinblockStr != null && !pinblockStr.equals("00")) {
                purchaseRequest.setPinData(pinblockStr);
                purchaseRequest.setKSNNumber(ksnStr);
                isPinBlock = true;
                purchaseRequest.setPosPINCaptureCode("12");
                purchaseRequest.setStructuredData("212Postilion:TM21816ScaInd18TMVLPCSB");
            }
            cardPaymentViewModel.sendPurchaseReq(singleTapPinRequest.getReferenceId48(),singleTapPinRequest.getFeeRefID(), singleTapPinRequest.getPurchaseFeeRequest(), true, this, singleTapPinRequest.getCardNumber(),
                    singleTapPinRequest.getTotalAmount(), singleTapPinRequest.getExpiryDate(),
                    purchaseRequest, this, sharedPreferenceUtil, singleTapPinRequest.getOrderRequest(), isPinBlock, false);
        });
    }

    private void makeMagCardTransaction() {
        PurchaseRequest purchaseRequest = new PurchaseRequest(null);
        purchaseRequest.setCardNumber(mCardNo);
        purchaseRequest.setTrack2Data(magTrack2Data);

        if (mCardType == AidlConstantsV2.CardType.MAGNETIC.getValue() || mCardType == AidlConstantsV2.CardType.IC.getValue()) {
            purchaseRequest.setTrack1Data(track1);

        }

        if (pinblockStr != null) {
            pinAvailable = true;
        }

        if (pinblockStr != null && !pinblockStr.equals("00")) {
            purchaseRequest.setPinData(pinblockStr);
            purchaseRequest.setKSNNumber(ksnStr);
            isPinBlock = true;
        }

        purchaseRequest = cardPaymentViewModel.constructPurchaseRequest(isPinBlock, purchaseRequest, sharedPreferenceUtil, mEditAmount.getText().toString());
        if (fallBack) {
            purchaseRequest.setReasonCode("4021");
            purchaseRequest.setPosEntryMode("801");
        }

        cancelTimer();
        //   showLoadingDialog("Please Wait");
        cardPaymentViewModel.initiateTransaction(orderNumber, feeResponseData, payRowDigitFee, payRowVATStatus, payRowVATAmount, pinAvailable, isPinBlock, onlineProc, this, signatureStatus, mPinType, mCardNo, mEditAmount.getText().toString(), expiryDate2, paymentTypeChan, amount, purchaseRequest, this, sharedPreferenceUtil);
    }


    private void cardFailed(String message, int code) {
        runOnUiThread(() -> {
            dismissLoadingDialog();
          //  cardPaymentViewModel.cardDeclineUpdate(orderNumber, payRowDigitFee, amount, payRowVATStatus, payRowVATAmount, signatureStatus, isPinBlock, mEditAmount.getText().toString(), paymentTypeChan, /*purchaseDetails, */this, sharedPreferenceUtil, message, code);
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tap_to_pay);
        initData();
        initView();
    }

    private void initView() {
        mEMVOptV2 = MyApplication.app.emvOptV2;
        mPinPadOptV2 = MyApplication.app.pinPadOptV2;
        mReadCardOptV2 = MyApplication.app.readCardOptV2;

        setTermParam();
        mEditAmount = findViewById(R.id.edtAmount);

        cancelBtn = findViewById(R.id.buttonCancel);
        findViewById(R.id.buttonCancel).setOnClickListener(this);
        tvTimer = findViewById(R.id.textViewTimeoutSeconds);

        Bundle bundle = getIntent().getExtras();
        amount = bundle.getString("AMOUNT");

        orderNumber = bundle.getString("OrderNumber");
        paymentTypeChan = bundle.getString("PAYMENT TYPE");
        payRowDigitFee = bundle.getString("PayRowDigFee");

        payRowVATAmount = bundle.getFloat("payRowVATAmount");
        payRowVATStatus = bundle.getBoolean("payRowVATStatus");

        // mEditAmount.setText(bundle.getString("AmountVAT"));


    //    purchaseDetails = (PurchaseBreakdownDetails) bundle.get("purchaseDetails");

        feeResponseData = bundle.getParcelable("feeResponseData");
        mEditAmount.setText(feeResponseData.getAmount());
        // feeServiceData = feeResponseData.getServicedata();

     //   Log.v("purchaseDetails->", purchaseDetails.toString());
        //   purchaseDetails = new PurchaseBreakdownDetails(listItem);

        cardPaymentViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new CardPaymentViewModelFactory()).get(CardPaymentViewModel.class);
        showAnimatedProgressDialog();
        countDownTimer = new CountDownTimer(33000, 1000) {

            public void onTick(long millisUntilFinished) {
                int secsLeft = (int) (millisUntilFinished / 1000);
                if (secsLeft == 30) {
                    closeProgressDialog();
                }
                tvTimer.setText("" + millisUntilFinished / 1000);
                // logic to set the EditText could go here
            }

            public void onFinish() {
                cancel();
                finish();
            }

        }.start();

        initiateCard();
    }

    private void initiateCard() {
        if (mProcessStep == 0) {
            LogUtil.e(Constant.TAG, "***************************************************************");
            LogUtil.e(Constant.TAG, "****************************Start Process**********************");
            LogUtil.e(Constant.TAG, "***************************************************************");
            // mTvShowInfo.setText("");
            String text = mEditAmount.getText().toString();
            try {
                // Before check card, initialize emv process(clear all TLV)
                mEMVOptV2.abortTransactProcess();
                mEMVOptV2.initEmvProcess();
                long limit = MoneyUtil.stringMoney2LongCent(text);
                if (limit > 0) {
                    checkCard();
                } else {
                    showToast("Please input correct cost amount");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showToast("It's a Exception..");
            }
        }
    }

    /**
     * Do essential initialization, client App should
     * initialize their own data at this step.
     * Note: this method just show how to initialize data before
     * the emv process, the initialized data may not useful for your
     * App. Please init your own data.
     */
    private void initData() {

        sharedPreferenceUtil = new SharedPreferenceUtil(getBaseContext());
        sharedPreferenceUtil.setAID("");
        sharedPreferenceUtil.setAC("");
        sharedPreferenceUtil.setACInfo("");
        sharedPreferenceUtil.setTVR("");
        sharedPreferenceUtil.setTransactionType(0);
        sharedPreferenceUtil.setCType("");
        // disable check card buzzer
        SettingUtil.setBuzzerEnable(false);
    }

    @Override
    public void onBackPressed() {
        if (mProcessStep == EMV_APP_SELECT) {
            importAppSelect(-1);
        } else if (mProcessStep == EMV_FINAL_APP_SELECT) {
            importFinalAppSelectStatus(-1);
        } else if (mProcessStep == EMV_CONFIRM_CARD_NO) {
            importCardNoStatus(1);
        } else if (mProcessStep == EMV_CERT_VERIFY) {
            importCertStatus(1);
        } else if (mProcessStep == PIN_ERROR) {
            importPinInputStatus(3);
        } else if (mProcessStep == EMV_ONLINE_PROCESS) {
            importOnlineProcessStatus(1, null, null, null);
        } else if (mProcessStep == EMV_SIGNATURE) {
            importSignatureStatus(1);
        }
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.buttonCancel) {

            if (Objects.equals(paymentTypeChan, "scan qr")) {
                cardPaymentViewModel.cancelTransaction(orderNumber, this, payRowDigitFee, amount, payRowVATStatus, payRowVATAmount, signatureStatus, isPinBlock, mEditAmount.getText().toString(), paymentTypeChan, /*purchaseDetails, */this, sharedPreferenceUtil);
            } else {
                Intent intent = new Intent(this, DashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        }
    }

    private void setTermParam() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("optOnlineRes", true);
        try {
            mEMVOptV2.setTermParamEx(bundle);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Set tlv essential tlv data
     *
     * @param tag9F06Value
     */
    private void initEmvTlvData(String tag9F06Value) {
        try {
            // set PayPass(MasterCard) tlv data
            String terminalRiskMgmt;
            String tacOnline;
            String tacDefault;
            String tacDenial;

            TinyDB tinyDB = new TinyDB(ICProcessActivity.this);
            ArrayList<ContactlessConfiguration> contactLessListItem = tinyDB.getListConfig(
                    Constants.CONTACTLESS_CONFIGURATION,
                    ContactlessConfiguration.class
            );

            if (!contactLessListItem.isEmpty()) {
                for (ContactlessConfiguration item : contactLessListItem) {
                    if (item.getAID().equals(tag9F06Value)) {

                        String[] tagsPayPass = {"DF8117", "DF8118", "DF8119", "DF811F",
                                "DF811E", "DF812C", "DF8123",
                                "DF8124", "DF8125", "DF8126",
                                "DF811B", "DF811D",
                                "DF8122", "DF8120", "DF8121",
                                "9F6D", "DF811A", "9F1D"};
                        String[] valuesPayPass = {item.getCardDataInputCap(), item.getChipCVMCapabilityRequired(), item.getChipCVMCapabilityNotRequired(), item.getSecurityCapability(),
                                item.getMSTRIPECVMCapabilityRequired(), item.getMSTRIPECVMCapabilityNotRequired(), item.getReaderContactlessFloorLimit(),
                                item.getContactlessLimitNoDCV(), item.getContactlessLimitWithDcv(), item.getCVMLimt(),
                                item.getKernelConfiguration(), item.getTornTransaction(),
                                item.getTACOnline(), item.getTACDefault(), item.getContactlesstacdenial(),
                                item.getMSTRIPEApplicationVersionNumber(), item.getUDOL(), item.getCMVTerminalRiskManagement()};


                        mEMVOptV2.setTlvList(AidlConstantsV2.EMV.TLVOpCode.OP_PAYPASS, tagsPayPass, valuesPayPass);

                    }
                }
            }
           /* if (tag9F06Value.equals("A0000000043060") || tag9F06Value.equals("A0000000043060C123456789")) {
                terminalRiskMgmt = "4C70800000000000";
                tacOnline = "F45004800C";
                tacDefault = "F45004800C";
                tacDenial = "0000800000";
            } else {
                terminalRiskMgmt = "6C70800000000000";
                tacOnline = "F45084800C";
                tacDefault = "F45084800C";
                tacDenial = "0000000000";
            }

            String[] tagsPayPass = {"DF8117", "DF8118", "DF8119", "DF811F", "DF811E", "DF812C",
                    "DF8123", "DF8124", "DF8125", "DF8126",
                    "DF811B", "DF811D",
                    "DF8122", "DF8120", "DF8121",
                    "9F6D", "DF811A", "9F1D"};
            String[] valuesPayPass = {"E0", "60", "08", "C8", "20", "00",
                    "000000000000", "000099999999", "000099999999", "000000050000",
                    "B0", "02",
                    tacOnline, tacDefault, tacDenial,
                    "0001", "9F6A04", terminalRiskMgmt};

            mEMVOptV2.setTlvList(AidlConstantsV2.EMV.TLVOpCode.OP_PAYPASS, tagsPayPass, valuesPayPass);*/

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start check card
     */
    private void checkCard() {
        try {
            int cardType = AidlConstantsV2.CardType.NFC.getValue() | AidlConstantsV2.CardType.IC.getValue() | AidlConstantsV2.CardType.MAGNETIC.getValue();
            mReadCardOptV2.checkCard(cardType, mCheckCardCallback, 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void swipeCard() {
        try {
            int cardType = AidlConstantsV2.CardType.MAGNETIC.getValue();
            mReadCardOptV2.checkCard(cardType, mCheckCardCallback, 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Check card callback
     */
    private final CheckCardCallbackV2 mCheckCardCallback = new CheckCardCallbackV2Wrapper() {

        @Override
        public void findMagCard(Bundle bundle) throws RemoteException {
            LogUtil.e(Constant.TAG, "findMagCard");
            mCardType = AidlConstantsV2.CardType.MAGNETIC.getValue();
            sharedPreferenceUtil.setTransactionType(mCardType);
            track1 = UtilityClass.null2String(bundle.getString("TRACK1"));
            magTrack2Data = UtilityClass.null2String(bundle.getString("TRACK2"));
            String track3 = UtilityClass.null2String(bundle.getString("TRACK3"));
            CardInfo cardInfo = parseTrack2(magTrack2Data);
            expiryDate2 = cardInfo.expireDate;
            serviceCode2 = cardInfo.serviceCode;

            String value = "track1:" + track1 + "\ntrack2:" + magTrack2Data + "\ntrack3:" + track3;
            LogUtil.e(Constant.TAG, "findMagCard: trackData" + value);

            if (!TextUtils.isEmpty(magTrack2Data)) {
                int index = magTrack2Data.indexOf("=");
                if (index != -1) {
                    mCardNo = magTrack2Data.substring(0, index);
                }
            }
            if (!TextUtils.isEmpty(mCardNo)) {
                getKSN();
                mHandler.obtainMessage(EMV_SHOW_PIN_PAD).sendToTarget();
            } else {
                dismissLoadingDialog();
                runOnUiThread(() -> {
                    try {
                        //  cancelCheckCard();
                        if (fallBack) {
                            //sharedPreferenceUtil.setTransactionType(3);
                            swipeCard();
                        } else {
                            checkCard();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //showToast("Get Card info error");
                });

            }
        }

        @Override
        public void findICCard(String atr) throws RemoteException {
            LogUtil.e(Constant.TAG, "findICCard:" + atr);
            //IC card Beep buzzer when check card success
            MyApplication.app.basicOptV2.buzzerOnDevice(1, 2750, 200, 0);
            mCardType = AidlConstantsV2.CardType.IC.getValue();
            transactProcess();
        }

        @Override
        public void findRFCard(String uuid) throws RemoteException {
            LogUtil.e(Constant.TAG, "findRFCard:" + uuid);
            mCardType = AidlConstantsV2.CardType.NFC.getValue();
            transactProcess();
        }

        @Override
        public void onError(int code, String message) throws RemoteException {
            String error = "onError:" + message + " -- " + code;
            LogUtil.e(Constant.TAG, error);
            if (SPErrorCode.SMC_HAL_ERR_PARITY.getCode() == code || SPErrorCode.SMC_HAL_ERR_TIMEOUT.getCode() == code) {
                // Fallback
                fallBack = true;
                runOnUiThread(() -> {
                    showToast("Please Swipe the Card");
                    try {
                        //  cancelCheckCard();
                        swipeCard();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } else {
                mHandler.obtainMessage(CARD_READ_FAILED, code, code, message).sendToTarget();
            }
        }
    };

    /**
     * Start emv transact process
     */
    private void transactProcess() {
        LogUtil.e(Constant.TAG, "transactProcess");
        try {
            sharedPreferenceUtil.setTransactionType(mCardType);
            long limit = MoneyUtil.stringMoney2LongCent(mEditAmount.getText().toString());

            EMVTransDataV2 emvTransData = new EMVTransDataV2();
            // flow type，0x01:Standard flow；0x02:Simple flow；0x03 qPass
            emvTransData.flowType = 1;
            // Card type, 2:IC 4:NFC
            emvTransData.cardType = mCardType;
            //  emvTransData.transType = "20";
            // Transaction amount(unit: cent)，must have parameter，cannot be null or "" , when amount="0" means check the balance.
            emvTransData.amount = String.valueOf(limit);
            // Start EMV process
            mEMVOptV2.transactProcess(emvTransData, mEMVListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * EMV process callback
     */
    private final EMVListenerV2 mEMVListener = new EMVListenerV2.Stub() {
        /**
         * Notify client to do multi App selection, this method may called when card have more than one Application
         * <br/> For Contactless and flowType set as AidlConstants.FlowType.TYPE_NFC_SPEEDUP, this
         * method will not be called
         *
         * @param appNameList   The App list for selection
         * @param isFirstSelect is first time selection
         */
        @Override
        public void onWaitAppSelect(List<EMVCandidateV2> appNameList, boolean isFirstSelect) throws RemoteException {
            LogUtil.e(Constant.TAG, "onWaitAppSelect isFirstSelect:" + isFirstSelect);
            mProcessStep = EMV_APP_SELECT;
            String[] candidateNames = getCandidateNames(appNameList);
            mHandler.obtainMessage(EMV_APP_SELECT, candidateNames).sendToTarget();
        }

        /**
         * Notify client the final selected Application
         * <br/> For Contactless and flowType set as AidlConstants.FlowType.TYPE_NFC_SPEEDUP, this
         * method will not be called
         *
         * @param tag9F06Value The final selected Application id
         */
        @Override
        public void onAppFinalSelect(String tag9F06Value) throws RemoteException {
            LogUtil.e(Constant.TAG, "onAppFinalSelect tag9F06Value:" + tag9F06Value);
            if (tag9F06Value != null && tag9F06Value.length() > 0) {
                boolean isVisa = tag9F06Value.startsWith("A000000003");
                boolean isMaster = tag9F06Value.startsWith("A000000004")
                        || tag9F06Value.startsWith("A000000005");

                mTag9F06Value = tag9F06Value;

                String terminalCapability;
                if (mCardType == AidlConstantsV2.CardType.NFC.getValue()) {
                    terminalCapability = sharedPreferenceUtil.getTapCapability();//"E068C8";
                } else {
                    terminalCapability = sharedPreferenceUtil.getChipCapability();//"E0F8C8";
                }
                String[] tags = {"5F2A", "5F36", "9F33", "9F66"};
                //  String[] values = {"0784", "00", terminalCapability, "36204000"};sharedPreferenceUtil.getCurrencyCode()
                String[] values = {"0784", sharedPreferenceUtil.getCurrencyExponent(), terminalCapability, sharedPreferenceUtil.getTTQ()};
                mEMVOptV2.setTlvList(AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL, tags, values);


                if (isVisa) {

                    paymentType = "Visa";
                    mAppSelect = 1;
                } else if (isMaster) {

                    initEmvTlvData(tag9F06Value);

                    paymentType = "MasterCard";
                    mAppSelect = 2;
                }
                LogUtil.e(Constant.TAG, "detect " + paymentType + " card");
            }
            mProcessStep = EMV_FINAL_APP_SELECT;
            mHandler.obtainMessage(EMV_FINAL_APP_SELECT, tag9F06Value).sendToTarget();
        }

        /**
         * Notify client to confirm card number
         * <br/> For Contactless and flowType set as AidlConstants.FlowType.TYPE_NFC_SPEEDUP, this
         * method will not be called
         *
         * @param cardNo The card number
         */
        @Override
        public void onConfirmCardNo(String cardNo) throws RemoteException {
            LogUtil.e(Constant.TAG, "onConfirmCardNo cardNo:" + cardNo);
            mCardNo = cardNo;
            mProcessStep = EMV_CONFIRM_CARD_NO;
            mHandler.obtainMessage(EMV_CONFIRM_CARD_NO).sendToTarget();
        }

        /**
         * Notify client to input PIN
         *
         * @param pinType    The PIN type, 0-online PIN，1-offline PIN
         * @param remainTime The the remain retry times of offline PIN, for online PIN, this param
         *                   value is always -1, and if this is the first time to input PIN, value
         *                   is -1 too.
         */
        @Override
        public void onRequestShowPinPad(int pinType, int remainTime) throws RemoteException {
            LogUtil.e(Constant.TAG, "onRequestShowPinPad pinType:" + pinType + " remainTime:" + remainTime);
            mPinType = pinType;
            if (mCardNo == null) {
                mCardNo = getCardNo();
            }
            mProcessStep = EMV_SHOW_PIN_PAD;
            getKSN();
            mHandler.obtainMessage(EMV_SHOW_PIN_PAD).sendToTarget();
        }

        /**
         * Notify  client to do signature
         */
        @Override
        public void onRequestSignature() throws RemoteException {
            LogUtil.e(Constant.TAG, "onRequestSignature");
            mProcessStep = EMV_SIGNATURE;
            signatureStatus = true;
            mHandler.obtainMessage(EMV_SIGNATURE).sendToTarget();
        }

        /**
         * Notify client to do certificate verification
         *
         * @param certType The certificate type, refer to AidlConstants.CertType
         * @param certInfo The certificate info
         */
        @Override
        public void onCertVerify(int certType, String certInfo) throws RemoteException {
            LogUtil.e(Constant.TAG, "onCertVerify certType:" + certType + " certInfo:" + certInfo);
            mProcessStep = EMV_CERT_VERIFY;
            mHandler.obtainMessage(EMV_CERT_VERIFY).sendToTarget();
        }

        /**
         * Notify client to do online process
         */
        @Override
        public void onOnlineProc() throws RemoteException {
            onlineProc = true;
            LogUtil.e(Constant.TAG, "onOnlineProcess");
            mProcessStep = EMV_ONLINE_PROCESS;
            //here contact and contactless - make purchase api - for every testcase
            mHandler.obtainMessage(EMV_ONLINE_PROCESS).sendToTarget();
        }

        /**
         * Notify client EMV kernel and card data exchange finished, client can remove card
         */
        @Override
        public void onCardDataExchangeComplete() throws RemoteException {
            LogUtil.e(Constant.TAG, "onCardDataExchangeComplete");
            if (mCardType == AidlConstantsV2.CardType.NFC.getValue()) {
                //NFC card Beep buzzer to notify remove card
                MyApplication.app.basicOptV2.buzzerOnDevice(1, 2750, 200, 0);
            }
        }

        /**
         * Notify client EMV process ended
         *
         * @param code The transaction result code, 0-success, 1-offline approval, 2-offline denial,
         *             4-try again, other value-error code
         * @param desc The corresponding message of this code
         */
        @Override
        public void onTransResult(int code, String desc) throws RemoteException {
            if (mCardNo == null) {
                mCardNo = getCardNo();
            }
            LogUtil.e(Constant.TAG, "onTransResult code:" + code + " desc:" + desc);
            LogUtil.e(Constant.TAG, "***************************************************************");
            LogUtil.e(Constant.TAG, "****************************End Process************************");
            LogUtil.e(Constant.TAG, "***************************************************************");

            // VISA SEE PHONE
            boolean bool = code == -4003 && mTag9F06Value.startsWith("A000000003") && AidlConstants.CardType.NFC.getValue() == 4;

            //dismissLoadingDialog();
            if (onlineDecline) {
                onlineDeclined();
            } else {
                if (bool) {
                    seePhoneProcess();
                } else if (code == 0) {
                    mHandler.obtainMessage(EMV_TRANS_SUCCESS, code, code, desc).sendToTarget();
                } else if (code == 4) {
                    tryAgain();
                } else if (code == 5) {
                    mHandler.obtainMessage(EMV_TRANS_SUCCESS, code, code, desc).sendToTarget();
                } else {
                    mHandler.obtainMessage(EMV_TRANS_FAIL, code, code, desc).sendToTarget();
                }
            }
        }

        /**
         * Notify client the confirmation code verified(See phone)
         */
        @Override
        public void onConfirmationCodeVerified() throws RemoteException {
            LogUtil.e(Constant.TAG, "onConfirmationCodeVerified");

            byte[] outData = new byte[512];
            int len = mEMVOptV2.getTlv(AidlConstantsV2.EMV.TLVOpCode.OP_PAYPASS, "DF8129", outData);
            if (len > 0) {
                byte[] data = new byte[len];
                System.arraycopy(outData, 0, data, 0, len);
                String hexStr = ByteUtil.bytes2HexStr(data);
                LogUtil.e(Constant.TAG, "DF8129: " + hexStr);
            }
            seePhoneProcess();
        }

        /**
         * Notify client to exchange data
         * <br/> This method only used for Russia MIR
         *
         * @param cardNo The card number
         */
        @Override
        public void onRequestDataExchange(String cardNo) throws RemoteException {
            LogUtil.e(Constant.TAG, "onRequestDataExchange,cardNo:" + cardNo);
            mEMVOptV2.importDataExchangeStatus(0);
        }

        @Override
        public void onTermRiskManagement() throws RemoteException {
            LogUtil.e(Constant.TAG, "onTermRiskManagement");
            mEMVOptV2.importTermRiskManagementStatus(0);
        }

        @Override
        public void onPreFirstGenAC() throws RemoteException {
            LogUtil.e(Constant.TAG, "onPreFirstGenAC");
            mEMVOptV2.importPreFirstGenACStatus(0);
        }

        @Override
        public void onDataStorageProc(String[] containerID, String[] containerContent) throws RemoteException {
            LogUtil.e(Constant.TAG, "onDataStorageProc,");
            String[] tags = new String[0];
            String[] values = new String[0];
            mEMVOptV2.importDataStorage(tags, values);
        }

    };

    private void onlineDeclined() {
        runOnUiThread(() -> {
            mProcessStep = 0;
            dismissLoadingDialog();
            dismissAppSelectDialog();
            Intent intent = new Intent(this, PaymentConfirmationActivity.class);

            intent.putExtra("TYPE", "TAPTOPAY");
            intent.putExtra("CARDNO", declineOrderRequest.getOrderRequest().getCardNumber());
            intent.putExtra("INVOICENO", declineOrderRequest.getOrderRequest().getOrderNumber());
            // intent.putExtra("hostRefNO", declineOrderRequest.getOrderRequest().getHostReference());
            intent.putExtra("vasRefNO", declineOrderRequest.getOrderRequest().getVasReference());
            intent.putExtra("status", declineOrderRequest.getOrderRequest().getCheckoutStatus());
            intent.putExtra("paymentType", declineOrderRequest.getChannel());
            intent.putExtra("bankTransferURL", declineOrderRequest.getBankTransferURL());
            intent.putExtra("orderRequest", (Serializable) declineOrderRequest.getOrderRequest());
            intent.putExtra("isPinBlock", pinBlockStatus);
            intent.putExtra("SignatureStatus", declineOrderRequest.getSignatureStatus());
            intent.putExtra("payRowVATAmount", declineOrderRequest.getOrderRequest().getVatAmount());
            intent.putExtra("payRowVATStatus", declineOrderRequest.getOrderRequest().getVatStatus());
            intent.putExtra("totalAmount", Objects.requireNonNull(declineOrderRequest.getOrderRequest().getAmount()).toString());
            intent.putExtra("payRowDigitFee", Objects.requireNonNull(declineOrderRequest.getOrderRequest().getPayRowDigitialFee()).toString());

            startActivity(intent);
            finish();
        });
    }

    /**
     * getCard number
     */
    private String getCardNo() {
        LogUtil.e(Constant.TAG, "getCardNo");
        try {
            String[] tagList = {"57", "5A"};
            byte[] outData = new byte[256];
            int len = mEMVOptV2.getTlvList(AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL, tagList, outData);
            if (len <= 0) {
                LogUtil.e(Constant.TAG, "getCardNo error,code:" + len);
                return "";
            }
            byte[] bytes = Arrays.copyOf(outData, len);
            Map<String, TLV> tlvMap = TLVUtil.buildTLVMap(bytes);
            if (!TextUtils.isEmpty(Objects.requireNonNull(tlvMap.get("57")).getValue())) {
                TLV tlv57 = tlvMap.get("57");
                CardInfo cardInfo = parseTrack2(tlv57.getValue());
                expiryDate2 = cardInfo.expireDate;
                serviceCode2 = cardInfo.serviceCode;
                return cardInfo.cardNo;
            }

            if (!TextUtils.isEmpty(Objects.requireNonNull(tlvMap.get("5A")).getValue())) {
                return Objects.requireNonNull(tlvMap.get("5A")).getValue();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Parse track2 data
     */
    public static CardInfo parseTrack2(String track2) {
        LogUtil.e(Constant.TAG, "track2:" + track2);
        String track_2 = stringFilter(track2);
        int index = track_2.indexOf("=");
        if (index == -1) {
            index = track_2.indexOf("D");
        }
        CardInfo cardInfo = new CardInfo();
        if (index == -1) {
            return cardInfo;
        }
        String cardNumber = "";
        if (track_2.length() > index) {
            cardNumber = track_2.substring(0, index);
        }
        String expiryDate = "";
        if (track_2.length() > index + 5) {
            expiryDate = track_2.substring(index + 1, index + 5);
        }
        String serviceCode = "";
        if (track_2.length() > index + 8) {
            serviceCode = track_2.substring(index + 5, index + 8);
        }
        LogUtil.e(Constant.TAG, "cardNumber:" + cardNumber + " expireDate:" + expiryDate + " serviceCode:" + serviceCode);
        cardInfo.cardNo = cardNumber;
        cardInfo.expireDate = expiryDate;
        cardInfo.serviceCode = serviceCode;
        return cardInfo;
    }

    /**
     * remove characters not number,=,D
     */
    static String stringFilter(String str) {
        String regEx = "[^0-9=D]";
        Pattern p = Pattern.compile(regEx);
        Matcher matcher = p.matcher(str);
        return matcher.replaceAll("").trim();
    }

    /**
     * Start show PinPad
     */
    private void initPinPad() {
        LogUtil.e(Constant.TAG, "initPinPad");
        try {
            PinPadConfigV2 pinPadConfig = new PinPadConfigV2();
            pinPadConfig.setPinPadType(0);
            pinPadConfig.setPinType(mPinType);
            pinPadConfig.setOrderNumKey(false);
            if (mCardNo == null || mCardNo.length() < 13) {
                byte[] panBlock = new byte[12];
                pinPadConfig.setPan(panBlock);
            } else {
                int length = mCardNo.length();
                byte[] panBlock = mCardNo.substring(length - 13, length - 1).getBytes(StandardCharsets.US_ASCII);
                pinPadConfig.setPan(panBlock);
            }

            pinPadConfig.setTimeout(60 * 1000); // input password timeout
            pinPadConfig.setPinKeyIndex(1);    // pik index
            pinPadConfig.setMaxInput(12);
            pinPadConfig.setMinInput(0);
            pinPadConfig.setKeySystem(1); // 0-MKSK
            pinPadConfig.setAlgorithmType(0); // 3DS
            mPinPadOptV2.initPinPad(pinPadConfig, mPinPadListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Input pin callback
     */
    private final PinPadListenerV2 mPinPadListener = new PinPadListenerV2.Stub() {

        @Override
        public void onPinLength(int len) {
            LogUtil.e(Constant.TAG, "onPinLength:" + len);
            mHandler.obtainMessage(PIN_CLICK_NUMBER, len).sendToTarget();
        }

        @Override
        public void onConfirm(int i, byte[] pinBlock) {
            if (pinBlock != null) {
                String hexStr = ByteUtil.bytes2HexStr(pinBlock);
                LogUtil.e(Constant.TAG, "onConfirm pin block:" + hexStr);
                pinblockStr = hexStr;
                mHandler.obtainMessage(PIN_CLICK_PIN, pinBlock).sendToTarget();
            } else {
                mHandler.obtainMessage(PIN_CLICK_CONFIRM).sendToTarget();
            }
        }

        @Override
        public void onCancel() {
            LogUtil.e(Constant.TAG, "onCancel");
            mHandler.obtainMessage(PIN_CLICK_CANCEL).sendToTarget();
        }

        @Override
        public void onError(int code) {
            LogUtil.e(Constant.TAG, "onError:" + code);
            String msg = AidlErrorCodeV2.valueOf(code).getMsg();
            mHandler.obtainMessage(PIN_ERROR, code, code, msg).sendToTarget();
        }
    };

    /**
     * Notify emv process the Application select result
     *
     * @param selectIndex the index of selected App, start from 0
     */
    private void importAppSelect(int selectIndex) {
        LogUtil.e(Constant.TAG, "importAppSelect selectIndex:" + selectIndex);
        try {
            mEMVOptV2.importAppSelect(selectIndex);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Notify emv process the final Application select result
     *
     * @param status 0:success, other value:failed
     */
    private void importFinalAppSelectStatus(int status) {
        try {
            LogUtil.e(Constant.TAG, "importFinalAppSelectStatus status:" + status);
            mEMVOptV2.importAppFinalSelectStatus(status);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Notify emv process the card number confirm status
     *
     * @param status 0:success, other value:failed
     */
    private void importCardNoStatus(int status) {
        LogUtil.e(Constant.TAG, "importCardNoStatus status:" + status);
        try {
            mEMVOptV2.importCardNoStatus(status);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Notify emv process the certification verify status
     *
     * @param status 0:success, other value:failed
     */
    private void importCertStatus(int status) {
        LogUtil.e(Constant.TAG, "importCertStatus status:" + status);
        try {
            mEMVOptV2.importCertStatus(status);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Notify emv process the PIN input result
     *
     * @param inputResult 0:success,1:input PIN canceled,2:input PIN skipped,3:PINPAD problem,4:input PIN timeout
     */
    private void importPinInputStatus(int inputResult) {
        LogUtil.e(Constant.TAG, "importPinInputStatus:" + inputResult);
        try {
            mEMVOptV2.importPinInputStatus(mPinType, inputResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Import signature result to emv process
     *
     * @param status 0:success, other value:failed
     */
    private void importSignatureStatus(int status) {
        LogUtil.e(Constant.TAG, "importSignatureStatus status:" + status);
        try {
            mEMVOptV2.importSignatureStatus(status);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Import online process result data(eg: field 55 ) to emv process.
     * if no date to import, set param tags and values as empty array
     *
     * @param status          0:online approval, 1:online denial, 2:online failed
     * @param jsonObject
     * @param authorizationId
     * @param responseCode
     */
    private void importOnlineProcessStatus(int status, JSONObject jsonObject, String authorizationId, String responseCode) {
        LogUtil.e(Constant.TAG, "importOnlineProcessStatus status:" + status);
        try {

            if (jsonObject != null) {
                JSONObject iccData = jsonObject.getJSONObject("IccData");
                JSONObject iccResponse = iccData.getJSONObject("IccResponse");
                if (iccResponse.has("IssuerScriptTemplate1")) {
                    iccValues[0] = iccResponse.getString("IssuerScriptTemplate1");
                }

                if (iccResponse.has("IssuerScriptTemplate2")) {
                    iccValues[1] = iccResponse.getString("IssuerScriptTemplate2");
                }

                if (iccResponse.has("IssuerAuthenticationData")) {
                    iccValues[2] = iccResponse.getString("IssuerAuthenticationData");
                }
            }

            if (responseCode != null) {
                iccValues[3] = UtilityClass.stringToHex(responseCode);
            }
            if (authorizationId != null) {
                iccValues[4] = authorizationId;
            }

            Log.v(Constant.TAG, Arrays.toString(iccValues));
            String[] tags = {"71", "72", "91", "8A", "89"};

            byte[] out = new byte[1024];
            int len = mEMVOptV2.importOnlineProcStatus(status, tags, iccValues, out);
            if (len < 0) {
                LogUtil.e(Constant.TAG, "importOnlineProcessStatus error,code:" + len);
            } else {
                byte[] bytes = Arrays.copyOf(out, len);
                String hexStr = ByteUtil.bytes2HexStr(bytes);
                LogUtil.e(Constant.TAG, "importOnlineProcessStatus outData:" + hexStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Mock a POSP to do some data exchange(online process), we don't have a really POSP,
     * client should connect to a really POSP at this step.
     */
    private void mockRequestToServer() {
        new Thread(() -> {
            try {
                showLoadingDialog("Requesting");
                if (AidlConstantsV2.CardType.MAGNETIC.getValue() != mCardType) {
                    getTlvData();
                }
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
                importOnlineProcessStatus(-1, null, null, null);
            } finally {
                dismissLoadingDialog();
            }
        }).start();

    }

    /**
     * Read we interested tlv data
     */
    private void getTlvData() {
        try {
            String[] tagList = {"9F6E",
                    "DF02", "5F34", "9F06", "FF30", "FF31", "95", "9B", "9F36", "9F26",
                    "9F27", "DF31", "5A", "57", "5F24", "9F1A", "9F33", "9F35", "9F40",
                    "9F03", "9F10", "9F37", "9C", "9A", "9F02", "5F2A", "82", "9F34", "9F1E",
                    "84", "4F", "9F66", "9F6C", "9F09", "9F41", "9F63", "5F20", "9F12", "50", "9F1F", "9F20"
            };
            byte[] outData = new byte[2048];
            Map<String, TLV> map = new TreeMap<>();
            int tlvOpCode;
            if (AidlConstantsV2.CardType.NFC.getValue() == mCardType) {
                if (mAppSelect == 2) {
                    tlvOpCode = AidlConstantsV2.EMV.TLVOpCode.OP_PAYPASS;
                } else if (mAppSelect == 1) {
                    tlvOpCode = AidlConstantsV2.EMV.TLVOpCode.OP_PAYWAVE;
                } else {
                    tlvOpCode = AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL;
                }
            } else {
                tlvOpCode = AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL;
            }
            int len = mEMVOptV2.getTlvList(tlvOpCode, tagList, outData);
            if (len > 0) {
                byte[] bytes = Arrays.copyOf(outData, len);
                String hexStr = ByteUtil.bytes2HexStr(bytes);
                Map<String, TLV> tlvMap = TLVUtil.buildTLVMap(hexStr);
                map.putAll(tlvMap);
                field55Data.putAll(tlvMap);
            }

            // payPassTags
            String[] payPassTags = {
                    "DF811E", "DF812C", "DF8118", "DF8119", "DF811F", "DF8117", "DF8124",
                    "DF8125", "9F6D", "DF811B", "9F53", "DF810C", "9F1D", "DF8130", "DF812D",
                    "DF811C", "DF811D", "9F7C",
            };
            len = mEMVOptV2.getTlvList(AidlConstantsV2.EMV.TLVOpCode.OP_PAYPASS, payPassTags, outData);
            if (len > 0) {
                byte[] bytes = Arrays.copyOf(outData, len);
                String hexStr = ByteUtil.bytes2HexStr(bytes);
                Map<String, TLV> tlvMap = TLVUtil.buildTLVMap(hexStr);
                field55Data.putAll(tlvMap);
                map.putAll(tlvMap);
            }

            final StringBuilder sb = new StringBuilder();
            Set<String> keySet = map.keySet();
            for (String key : keySet) {
                TLV tlv = map.get(key);
                sb.append(key);
                sb.append(":");
                if (tlv != null) {
                    if (key.equals("5F24")) {
                        String value = tlv.getValue();
                        if (!value.equals("")) {
                            if (value.length() > 4) {
                                expiryDate2 = value.substring(0, 4);
                            } else {
                                expiryDate2 = value;
                            }
                            Log.v("expiryDate:", expiryDate2);
                        }
                    }

                    if (key.equals("57")) {
                        if (tlv.getValue() != null) {
                            track2Data = tlv.getValue();
                            CardInfo cardInfo = parseTrack2(tlv.getValue());
                            mCardNo = cardInfo.cardNo;
                            expiryDate2 = cardInfo.expireDate;
                            serviceCode2 = cardInfo.serviceCode;
                        }
                    }

                    if (key.equals("84")) {
                        if (tlv.getValue() != null) {
                            appIdentifier = tlv.getValue();
                            sharedPreferenceUtil.setAID(tlv.getValue());
                        }
                    }

                    if (key.equals("9F26")) {
                        if (tlv.getValue() != null) {
                            sharedPreferenceUtil.setAC(tlv.getValue());
                        }
                    }

                    if (key.equals("9F27")) {
                        if (tlv.getValue() != null) {
                            sharedPreferenceUtil.setACInfo(tlv.getValue());
                        }
                    }

                    if (key.equals("95")) {
                        if (tlv.getValue() != null) {
                            sharedPreferenceUtil.setTVR(tlv.getValue());
                        }
                    }

                    if (key.equals("9F1F")) {
                        if (!tlv.getValue().equals("")) {
                            track1 = tlv.getValue();
                        }
                    }

                    if (key.equals("5F34")) {
                        if (!tlv.getValue().equals("")) {
                            panSequenceNumber = tlv.getValue();
                        } else {
                            panSequenceNumber = "00";
                        }
                    }

                    String value = tlv.getValue();
                    sb.append(value);
                }
                sb.append("\n");
            }
            runOnUiThread(
                    () -> {
                        onlineRequest();
                        Log.v("sb", String.valueOf(sb));
                    }//mTvShowInfo.setText(sb)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void seePhoneProcess() {
        try {
            // card off
            // mReadCardOptV2.cardOff(mCardType);
            runOnUiThread(() -> new AlertDialog.Builder(ICProcessActivity.this)
                    .setTitle("See Phone")
                    .setMessage("Please see phone")
                    .setPositiveButton("OK", (dia, which) -> {
                                dia.dismiss();
                                // Restart transaction procedure.
                                try {
                                    cancelCheckCard();
                                    mEMVOptV2.abortTransactProcess();
                                    //  setOnlineParam();
                                    mEMVOptV2.initEmvProcess();
                                    checkCard();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                    ).show()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetUI(boolean success, String message, int code) {
        runOnUiThread(() -> {
                    mProcessStep = 0;
                    dismissLoadingDialog();
                    dismissAppSelectDialog();
                    if (success) {

                        if (Objects.equals(resultRequestClass.getOrderRequest().getResponseCode(), "10")) {
                            cardPaymentViewModel.callPartialActivity(resultRequestClass.getSignatureStatus(), pinBlockStatus, this, resultRequestClass.getOrderRequest(), resultRequestClass.getCardNumber()
                                    , resultRequestClass.getOrderNumber(), resultRequestClass.getHostRefNo(), resultRequestClass.getVasRefNo(),
                                    resultRequestClass.getStatus(), resultRequestClass.getChannel(), resultRequestClass.getBankTransferURL(),
                                    resultRequestClass.getAuthorizationId(), resultRequestClass.getAppPANSeqNo());

                        } else {
                            cardPaymentViewModel.callConfirmationActivity(resultRequestClass.getSignatureStatus(), pinBlockStatus, this, resultRequestClass.getOrderRequest(), resultRequestClass.getCardNumber()
                                    , resultRequestClass.getOrderNumber(), resultRequestClass.getHostRefNo(), resultRequestClass.getVasRefNo(),
                                    resultRequestClass.getStatus(), resultRequestClass.getChannel(), resultRequestClass.getBankTransferURL(),
                                    resultRequestClass.getAuthorizationId(), resultRequestClass.getAppPANSeqNo());
                        }
                    } else {
                        if (resultRequestClass != null) {
                            String finalStatus;
                            if (Objects.equals(resultRequestClass.getOrderRequest().getResponseCode(), "00") ||
                                    Objects.equals(resultRequestClass.getOrderRequest().getResponseCode(), "10")) {
                                finalStatus = "Terminal Declined";
                                resultRequestClass.getOrderRequest().setCheckoutStatus(finalStatus);
                                resultRequestClass.getOrderRequest().setOrderStatus(finalStatus);
                            } else {
                                finalStatus = resultRequestClass.getStatus();
                            }
                            cardPaymentViewModel.callConfirmationActivity(resultRequestClass.getSignatureStatus(), pinBlockStatus, this, resultRequestClass.getOrderRequest(), resultRequestClass.getCardNumber()
                                    , resultRequestClass.getOrderNumber(), resultRequestClass.getHostRefNo(), resultRequestClass.getVasRefNo(),
                                    finalStatus, resultRequestClass.getChannel(), resultRequestClass.getBankTransferURL(),
                                    resultRequestClass.getAuthorizationId(), resultRequestClass.getAppPANSeqNo());
                        } else {
                          //  cardPaymentViewModel.cardDeclineUpdate(orderNumber, payRowDigitFee, amount, payRowVATStatus, payRowVATAmount, signatureStatus, isPinBlock, mEditAmount.getText().toString(), paymentTypeChan, /*purchaseDetails,*/ this, sharedPreferenceUtil, message, code);
                        }
                    }
                }
        );
    }

    private void dismissAppSelectDialog() {
        runOnUiThread(() -> {
                    if (mAppSelectDialog != null) {
                        try {
                            mAppSelectDialog.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mAppSelectDialog = null;
                    }
                }
        );
    }

    /**
     * Create Candidate names
     */
    private String[] getCandidateNames(List<EMVCandidateV2> candiList) {
        if (candiList == null || candiList.size() == 0) return new String[0];
        String[] result = new String[candiList.size()];
        for (int i = 0; i < candiList.size(); i++) {
            EMVCandidateV2 candi = candiList.get(i);
            String name = candi.appPreName;
            name = TextUtils.isEmpty(name) ? candi.appLabel : name;
            name = TextUtils.isEmpty(name) ? candi.appName : name;
            name = TextUtils.isEmpty(name) ? "" : name;
            result[i] = name;
            LogUtil.e(Constant.TAG, "EMVCandidateV2: " + name);
        }
        return result;
    }

    @Override
    protected void onStop() {
        super.onStop();
        cancelTimer();
        closeProgressDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelCheckCard();
        SettingUtil.setBuzzerEnable(true);
    }

    private void cancelCheckCard() {
        try {
            mReadCardOptV2.cardOff(AidlConstantsV2.CardType.NFC.getValue());
            mReadCardOptV2.cancelCheckCard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tryAgain() {
        try {
            runOnUiThread(() -> new AlertDialog.Builder(this)
                    .setTitle("Try again")
                    .setMessage("Please read the card again")
                    .setPositiveButton("OK", (dia, which) -> {
                                dia.dismiss();

                                try {
                                    cancelCheckCard();
                                    mEMVOptV2.abortTransactProcess();
                                    // setOnlineParam();
                                    mEMVOptV2.initEmvProcess();
                                    //  initEmvTlvData();
                                    checkCard();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                    )
                    .show()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Check and notify remove card
     */
    private void checkAndRemoveCard() {
        try {
            int status = mReadCardOptV2.getCardExistStatus(mCardType);
            if (status < 0) {
                LogUtil.e(Constant.TAG, "getCardExistStatus error, code:" + status);
                dismissLoadingDialog();
                return;
            }
            if (status == AidlConstantsV2.CardExistStatus.CARD_ABSENT) {
                dismissLoadingDialog();
            } else if (status == AidlConstantsV2.CardExistStatus.CARD_PRESENT) {
                showLoadingDialog("Remove EMV Card");
                MyApplication.app.basicOptV2.buzzerOnDevice(1, 2750, 200, 0);
                mHandler.sendEmptyMessageDelayed(REMOVE_CARD, 1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected void showLoadingDialog(int resId) {
        runOnUiThread(() -> _showLoadingDialog(getString(resId)));
    }

    protected void showLoadingDialog(final String msg) {
        runOnUiThread(() -> _showLoadingDialog(msg));
    }

    /**
     * This method should be called in UI thread
     */
    private void _showLoadingDialog(final String msg) {
        if (loadDialog == null) {
            loadDialog = new LoadingDialog(this, msg);
        } else {
            loadDialog.setMessage(msg);
        }
        if (!loadDialog.isShowing()) {

            loadDialog.show();
        }
    }

    protected void dismissLoadingDialog() {
        runOnUiThread(
                () -> {
                    if (loadDialog != null && loadDialog.isShowing()) {
                        loadDialog.dismiss();
                    }
                    dlgHandler.removeCallbacksAndMessages(null);
                }
        );
    }


    private void getKSN() {
        try {
            int result = MyApplication.app.securityOptV2.dukptIncreaseKSN(Constant.indexDuKpt);
            LogUtil.e(Constant.TAG, "increaseKSN result:" + result);

            byte[] dataOut = new byte[10];
            result = MyApplication.app.securityOptV2.dukptCurrentKSN(Constant.indexDuKpt, dataOut);
            if (result == 0) {
                ksnStr = ByteUtil.bytes2HexStr(dataOut);
                LogUtil.e(Constant.TAG, "ksnStr Before:" + ksnStr);
                // int length = ksnStr.length();
                // ksnStr = ksnStr.substring(length - 16, length);
                LogUtil.e(Constant.TAG, "ksnStr:" + ksnStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cancelTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void onlineRequest() {
        PurchaseRequest purchaseRequest = new PurchaseRequest(null);
        purchaseRequest.setCardNumber(mCardNo);

        if (track2Data.contains("F")) {
            String track2Str = track2Data.replace("F", "");
            purchaseRequest.setTrack2Data(track2Str.trim());
        } else {
            purchaseRequest.setTrack2Data(track2Data);
        }

        if (mCardType == AidlConstantsV2.CardType.NFC.getValue() || mCardType == AidlConstantsV2.CardType.IC.getValue()) {
            purchaseRequest.setAppPANSeqNo("0" + panSequenceNumber);
            String field55Str = KernelDataUtil.readKernelData(field55Data);
            purchaseRequest.setFiftyFiveData(field55Str);
        }
        if (mCardType == AidlConstantsV2.CardType.IC.getValue()) {
            purchaseRequest.setTrack1Data(track1);
        }

        if (pinblockStr != null) {
            pinAvailable = true;
        }

        if (pinblockStr != null && !pinblockStr.equals("00")) {
            purchaseRequest.setPinData(pinblockStr);
            purchaseRequest.setKSNNumber(ksnStr);
            isPinBlock = true;
        }

        cancelTimer();
        purchaseRequest = cardPaymentViewModel.constructPurchaseRequest(isPinBlock, purchaseRequest, sharedPreferenceUtil, mEditAmount.getText().toString());

        //  showLoadingDialog("Please Wait");
        cardPaymentViewModel.initiateTransaction(orderNumber, feeResponseData, payRowDigitFee, payRowVATStatus, payRowVATAmount, pinAvailable, isPinBlock, onlineProc, this, signatureStatus, mPinType, mCardNo, mEditAmount.getText().toString(), expiryDate2, paymentTypeChan, amount, purchaseRequest, this, sharedPreferenceUtil);
    }

    @Override
    public void onTransactionStatus(ResultRequestClass resultRequestClass, Boolean pinBlockStatus) {
        this.resultRequestClass = resultRequestClass;
        this.pinBlockStatus = pinBlockStatus;

        int secondGenStatus;
        if (Objects.equals(resultRequestClass.getOrderRequest().getResponseCode(), "00") || Objects.equals(resultRequestClass.getOrderRequest().getResponseCode(), "10")) {
            secondGenStatus = 0;
        } else {
            secondGenStatus = 1;
        }

        if (resultRequestClass.getIccData() != null) {
            XmlToJson xmlToJson = new XmlToJson.Builder(resultRequestClass.getIccData()).build();
            JSONObject jsonObject = xmlToJson.toJson();
            importOnlineProcessStatus(secondGenStatus, jsonObject, resultRequestClass.getAuthorizationId(), resultRequestClass.getOrderRequest().getResponseCode());
        } else {
            importOnlineProcessStatus(secondGenStatus, null, resultRequestClass.getAuthorizationId(), resultRequestClass.getOrderRequest().getResponseCode());
        }
    }

    @Override
    public void onTransactionFailed(FailedRequstClass failedRequstClass, Boolean pinBlockStatus) {
        onlineDecline = true;
        declineOrderRequest = failedRequstClass;
        this.pinBlockStatus = pinBlockStatus;
        if (Boolean.TRUE.equals(failedRequstClass.getOnlineStatus())) {
            importOnlineProcessStatus(1, null, null, null);
        } else {
            onlineDeclined();
        }
    }

    @Override
    public void singleTapPinRequest(SingleTapPinRequest singleTapPinRequest) {
        dismissLoadingDialog();
        this.singleTapPinRequest = singleTapPinRequest;
        singleTap = true;
        getKSN();
        mHandler.obtainMessage(EMV_SHOW_PIN_PAD).sendToTarget();
    }
}
