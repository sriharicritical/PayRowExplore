package com.payment.payrowapp.refundandreversal;

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
import com.payment.payrowapp.dashboard.DashboardActivity;
import com.payment.payrowapp.dataclass.ContactlessConfiguration;
import com.payment.payrowapp.dataclass.PurchaseRequest;
import com.payment.payrowapp.login.AuthViewModelFactory;
import com.payment.payrowapp.login.AuthenticationViewModel;
import com.payment.payrowapp.newpayment.TinyDB;
import com.payment.payrowapp.sharepref.SharedPreferenceUtil;
import com.payment.payrowapp.sunmipay.ByteUtil;
import com.payment.payrowapp.sunmipay.CheckCardCallbackV2Wrapper;
import com.payment.payrowapp.sunmipay.Constant;
import com.payment.payrowapp.sunmipay.LoadingDialog;
import com.payment.payrowapp.sunmipay.LogUtil;
import com.payment.payrowapp.sunmipay.SettingUtil;
import com.payment.payrowapp.utils.BaseActivity;
import com.payment.payrowapp.utils.Constants;
import com.payment.payrowapp.utils.MoneyUtil;
import com.sunmi.pay.hardware.aidl.AidlConstants;
import com.sunmi.pay.hardware.aidl.SPErrorCode;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;
import com.sunmi.pay.hardware.aidlv2.bean.EMVCandidateV2;
import com.sunmi.pay.hardware.aidlv2.bean.EMVTransDataV2;
import com.sunmi.pay.hardware.aidlv2.emv.EMVListenerV2;
import com.sunmi.pay.hardware.aidlv2.emv.EMVOptV2;
import com.sunmi.pay.hardware.aidlv2.pinpad.PinPadOptV2;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;
import com.sunmi.pay.hardware.aidlv2.readcard.ReadCardOptV2;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * This page show the emv procedure.
 * Any transaction which should do emv process can refer
 * to this page.
 */
public class RefundPaymentActivity extends BaseActivity implements View.OnClickListener {

    private EMVOptV2 mEMVOptV2;
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
    private static final int REMOVE_CARD = 1000;

    private LoadingDialog loadDialog;

    private final Handler dlgHandler = new Handler();
    String amount;
    Button cancelBtn;
    TextView tvTimer;

    String paymentType = "unknown";
    boolean fallBack = false;
    CountDownTimer countDownTimer;
    private String mTag9F06Value;
    boolean onlineProc = false;
    String[] iccValues = {"", "", "", "", ""};
    PurchaseRequest purchaseRequest;
    String orderNumber, mode, cardNumber, vasRefNo, ICCData, cardBrand, feeFetchStr;
    AuthenticationViewModel authenticationViewModel;
    SharedPreferenceUtil sharedPreferenceUtil;

    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case EMV_APP_SELECT:
                    dismissLoadingDialog();
                    String[] candiNames = (String[]) msg.obj;
                    mAppSelectDialog = new AlertDialog.Builder(RefundPaymentActivity.this)
                            .setTitle(R.string.emv_app_select)
                            .setNegativeButton(R.string.cancel, (dialog, which) -> {
                                        importAppSelect(-1);
                                    }
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

                case REMOVE_CARD:
                    checkAndRemoveCard();
                    break;
                case CARD_READ_FAILED:
                    cardFailed((String) msg.obj, msg.arg1);
                    showToast("error:" + "Card Reading failed");// msg.obj + " -- " + msg.arg1);
                    break;
            }
        }
    };


    private void cardFailed(String message, int code) {
        runOnUiThread(() -> {
            dismissLoadingDialog();
            finish();
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refund_payment);
        initData();
        initView();
    }

    private void initView() {
        mEMVOptV2 = MyApplication.app.emvOptV2;
        PinPadOptV2 mPinPadOptV2 = MyApplication.app.pinPadOptV2;
        mReadCardOptV2 = MyApplication.app.readCardOptV2;

        setTermParam();

        mEditAmount = findViewById(R.id.edtAmount);

        cancelBtn = findViewById(R.id.buttonCancel);
        findViewById(R.id.buttonCancel).setOnClickListener(this);
        tvTimer = findViewById(R.id.textViewTimeoutSeconds);

        Bundle bundle = getIntent().getExtras();
        amount = bundle.getString("AMOUNT");

        purchaseRequest = (PurchaseRequest) bundle.get("PurchaseRequest");
        orderNumber = bundle.getString("orderNumber");
        vasRefNo = bundle.getString("vasRefNo");
        mode = bundle.getString("mode");
        cardNumber = bundle.getString("cardNumber");
        cardBrand = bundle.getString("cardBrand");
        ICCData = bundle.getString("ICCData");
        feeFetchStr = bundle.getString("feeFetchStr");
        mEditAmount.setText(bundle.getString("AMOUNT"));

        authenticationViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) new AuthViewModelFactory(this)).get(AuthenticationViewModel.class);
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
                //  long parseLong = Long.parseLong(amount);
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
        } else if (mProcessStep == EMV_ONLINE_PROCESS) {
            importOnlineProcessStatus();
        } else if (mProcessStep == EMV_SIGNATURE) {
            importSignatureStatus(1);
        }
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.buttonCancel) {
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
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
            String tacOnline = "0000000000";
            String tacDefault = "0000000000";
            String tacDenial = "FFFFFFFFFF";


            TinyDB tinyDB = new TinyDB(RefundPaymentActivity.this);
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
                                tacOnline, tacDefault, tacDenial,
                                item.getMSTRIPEApplicationVersionNumber(), item.getUDOL(), item.getCMVTerminalRiskManagement()};

                        mEMVOptV2.setTlvList(AidlConstantsV2.EMV.TLVOpCode.OP_PAYPASS, tagsPayPass, valuesPayPass);
                    }
                }
            }
           /* if (tag9F06Value.equals("A0000000043060") || tag9F06Value.equals("A0000000043060C123456789")) {
                terminalRiskMgmt = "4C70800000000000";
            } else {
                terminalRiskMgmt = "6C70800000000000";
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
            //  showLoadingDialog(R.string.emv_swing_card_ic);
            int cardType = AidlConstantsV2.CardType.NFC.getValue() | AidlConstantsV2.CardType.IC.getValue();
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
            long limit = MoneyUtil.stringMoney2LongCent(mEditAmount.getText().toString());

            EMVTransDataV2 emvTransData = new EMVTransDataV2();
            // flow type，0x01:Standard flow；0x02:Simple flow；0x03 qPass
            emvTransData.flowType = 1;
            // Card type, 2:IC 4:NFC
            emvTransData.cardType = mCardType;
            emvTransData.transType = "20";
            // Transaction amount(unit: cent)，must have parameter，cannot be null or "" , when amount="0" means check the balance.
            emvTransData.amount = String.valueOf(limit);
            // Start EMV process
            mEMVOptV2.transactProcess(emvTransData, mEMVListener);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private final EMVListenerV2 mEMVListener = new EMVListenerV2.Stub() {

        @Override
        public void onWaitAppSelect(List<EMVCandidateV2> appNameList, boolean isFirstSelect) throws RemoteException {
            LogUtil.e(Constant.TAG, "onWaitAppSelect isFirstSelect:" + isFirstSelect);
            mProcessStep = EMV_APP_SELECT;
            String[] candidateNames = getCandidateNames(appNameList);
            mHandler.obtainMessage(EMV_APP_SELECT, candidateNames).sendToTarget();
        }


        @Override
        public void onAppFinalSelect(String tag9F06Value) throws RemoteException {
            LogUtil.e(Constant.TAG, "onAppFinalSelect tag9F06Value:" + tag9F06Value);
            if (tag9F06Value != null && tag9F06Value.length() > 0) {
                boolean isVisa = tag9F06Value.startsWith("A000000003");
                boolean isMaster = tag9F06Value.startsWith("A000000004")
                        || tag9F06Value.startsWith("A000000005");

                mTag9F06Value = tag9F06Value;

                String[] tags = {"5F2A", "5F36", "9F33", "9F66"};
                // String[] values = {"0784", "00", "E0F8C8", "36204000"};
                String[] values = {"0784", sharedPreferenceUtil.getCurrencyExponent(), sharedPreferenceUtil.getChipCapability(), sharedPreferenceUtil.getTTQ()};
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
            importFinalAppSelectStatus(0);
        }


        @Override
        public void onConfirmCardNo(String cardNo) throws RemoteException {
            LogUtil.e(Constant.TAG, "onConfirmCardNo cardNo:" + cardNo);
            mCardNo = cardNo;
            mProcessStep = EMV_CONFIRM_CARD_NO;
            importCardNoStatus(0);
        }

        @Override
        public void onRequestShowPinPad(int pinType, int remainTime) throws RemoteException {
            LogUtil.e(Constant.TAG, "onRequestShowPinPad pinType:" + pinType + " remainTime:" + remainTime);
            mPinType = pinType;
            mProcessStep = EMV_SHOW_PIN_PAD;
            importOnlineProcessStatus();
            //   importPinInputStatus(0);
        }


        @Override
        public void onRequestSignature() throws RemoteException {
            LogUtil.e(Constant.TAG, "onRequestSignature");
            mProcessStep = EMV_SIGNATURE;
            importSignatureStatus(0);
        }


        @Override
        public void onCertVerify(int certType, String certInfo) throws RemoteException {
            LogUtil.e(Constant.TAG, "onCertVerify certType:" + certType + " certInfo:" + certInfo);
            mProcessStep = EMV_CERT_VERIFY;
            importCertStatus(0);
        }

        @Override
        public void onOnlineProc() throws RemoteException {
            onlineProc = true;
            LogUtil.e(Constant.TAG, "onOnlineProcess");
            mProcessStep = EMV_ONLINE_PROCESS;
            importOnlineProcessStatus();
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

            LogUtil.e(Constant.TAG, "onTransResult code:" + code + " desc:" + desc);
            LogUtil.e(Constant.TAG, "***************************************************************");
            LogUtil.e(Constant.TAG, "****************************End Process************************");
            LogUtil.e(Constant.TAG, "***************************************************************");

            // VISA SEE PHONE
            boolean bool = code == -4003 && mTag9F06Value.startsWith("A000000003") && AidlConstants.CardType.NFC.getValue() == 4;

            if (bool) {
                seePhoneProcess();
            } else if (code == 4) {
                tryAgain();
            } else {
                // getTlvData();
                resetUI();
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
            // card off
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
            //此回调为Dpas2.0专用
            //根据需求配置tag及values
            String[] tags = new String[0];
            String[] values = new String[0];
            mEMVOptV2.importDataStorage(tags, values);
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
     */
    private void importOnlineProcessStatus() {
        LogUtil.e(Constant.TAG, "importOnlineProcessStatus status:" + 1);
        try {
            Log.v(Constant.TAG, Arrays.toString(iccValues));
            String[] tags = {"71", "72", "91", "8A", "89"};

            byte[] out = new byte[1024];
            int len = mEMVOptV2.importOnlineProcStatus(1, tags, iccValues, out);
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


    private void seePhoneProcess() {
        try {
            // card off
            // mReadCardOptV2.cardOff(mCardType);
            runOnUiThread(() -> new AlertDialog.Builder(RefundPaymentActivity.this)
                    .setTitle("See Phone")
                    .setMessage("Please see phone")
                    .setPositiveButton("OK", (dia, which) -> {
                                dia.dismiss();
                                // Restart transaction procedure.
                                try {
                                    cancelCheckCard();
                                    mEMVOptV2.abortTransactProcess();
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

    private void resetUI() {
        runOnUiThread(() -> {
                    mProcessStep = 0;
                    dismissLoadingDialog();
                    dismissAppSelectDialog();

                    cancelTimer();
                   /* ProgressDialog progressDialog = new ProgressDialog(RefundPaymentActivity.this);
                    progressDialog.setMessage("Please wait...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
*/

                    if (Objects.equals(purchaseRequest.getCardNumber(), mCardNo)) {

                        // purchaseRequest.setAmount(AuthenticationViewModel.getHexDecimalValue(amount));
                        PurchaseRequest purchaseFeeReq = authenticationViewModel.prepareFeeReq(amount, purchaseRequest, feeFetchStr);

                      /*  authenticationViewModel.initiateRefundTrans(cardNumber, vasRefNo, orderNumber, cardBrand, mode,
                                purchaseRequest, purchaseFeeReq, this);*/
                        /*authenticationViewModel.getFSSFeeResponse(true, purchaseFeeReq);

                        authenticationViewModel.getFSSFeeData().observe(this, purchaseFeeResponse -> {

                            if (purchaseFeeResponse.getPurchaseResult().getResponseCode().equals("00")) {

                                JSONObject feeResultObject = null;
                                try {
                                    feeResultObject = new JSONObject(purchaseFeeResponse.getPurchaseResult().getFssFetch());

                                    String totalChargAmount = feeResultObject.getString("totalChargableAmount");
                                    purchaseRequest.setAmount(AuthenticationViewModel.getHexDecimalValue(totalChargAmount));
                                    purchaseRequest.setOrderNo(purchaseFeeResponse.getPurchaseResult().getOrderNo());
                                    purchaseFeeReq.setOrderNo(purchaseFeeResponse.getPurchaseResult().getOrderNo());
                                    authenticationViewModel.sendPurchaseRequest(
                                            cardNumber, vasRefNo, mode, orderNumber, this, purchaseRequest
                                    );

                                    authenticationViewModel.purchaseResponse().observe(this, purchaseResponse -> {
                                        progressDialog.dismiss();

                                        String ackPurchaseRequest = AuthenticationViewModel.prepareACKRequest(purchaseFeeResponse.getPurchaseResult().getFssFetch(),
                                                purchaseResponse.getPurchaseResult().getResponseCode(),
                                                purchaseResponse.getPurchaseResult().getAuthorizationId(),
                                                purchaseFeeReq);


                                        purchaseFeeReq.setAmount(purchaseRequest.getAmount());
                                        purchaseFeeReq.setFssFetch(ackPurchaseRequest);
                                        purchaseFeeReq.setPosConditionCode("52");
                                        purchaseFeeReq.setProcessingCode("490000");
                                        purchaseFeeReq.setAckCardBrand(cardBrand);

                                        authenticationViewModel.getFSSFeeResponse(false, purchaseFeeReq);

                                        Intent intent = new Intent(this, RefundConfirmationActivity.class);

                                        if (purchaseResponse.getPurchaseResult().getResponseCode().equals("00")) {
                                            intent.putExtra("authCode", purchaseResponse.getPurchaseResult().getAuthorizationId());
                                            intent.putExtra("status", "REFUNDED");
                                            intent.putExtra("hostRefNO", purchaseResponse.getPurchaseResult().getOrderNo());
                                            intent.putExtra("reference48",purchaseFeeResponse.getPurchaseResult().getFssFetch());
                                        } else {
                                            intent.putExtra("status", "NOT REFUNDED");
                                        }


                                        intent.putExtra("vasRefNO", vasRefNo);
                                        intent.putExtra("CARDNO", cardNumber);
                                        intent.putExtra("orderNumber", orderNumber);
                                        intent.putExtra("mode", mode);
                                        intent.putExtra("responseCode", purchaseResponse.getPurchaseResult().getResponseCode());
                                        intent.putExtra("cardBrand", cardBrand);
                                        intent.putExtra("totalAmount", totalChargAmount);

                                        startActivity(intent);
                                        finish();
                                    });

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                progressDialog.dismiss();
                                Intent intent = new Intent(this, RefundConfirmationActivity.class);
                                intent.putExtra("status", "Cancelled");
                                intent.putExtra("vasRefNO", vasRefNo);
                                intent.putExtra("CARDNO", cardNumber);
                                intent.putExtra("orderNumber", orderNumber);
                                intent.putExtra("mode", mode);
                                startActivity(intent);
                            }
                        });*/

                    } else {
                        // progressDialog.dismiss();
                        showToast("Card Number is not matching. Please try with valid card.");
                        finish();
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

    private void cancelTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
