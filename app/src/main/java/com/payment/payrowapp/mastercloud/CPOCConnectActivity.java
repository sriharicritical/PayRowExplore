package com.payment.payrowapp.mastercloud;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.mastercard.cpos.facade.CPosSdk;
import com.mastercard.cpos.facade.PaymentManager;
import com.mastercard.cpos.facade.PosManager;
import com.mastercard.cpos.facade.TransactionManager;
import com.mastercard.cpos.facade.exception.PaymentException;
import com.mastercard.cpos.facade.exception.PosException;
import com.mastercard.cpos.facade.model.InitPaymentRequest;
import com.mastercard.cpos.facade.model.MerchantSettingsData;
import com.mastercard.cpos.facade.model.PaymentOutcomeResponse;
import com.mastercard.cpos.facade.model.TaxDetail;
import com.mastercard.cpos.nfc.listener.NfcEventListener;
import com.payment.payrowapp.R;
import com.payment.payrowapp.cardpayment.CardPaymentViewModel;
import com.payment.payrowapp.cardpayment.CardPaymentViewModelFactory;
import com.payment.payrowapp.dashboard.DashboardActivity;
import com.payment.payrowapp.dataclass.FeeResponseData;
import com.payment.payrowapp.newpayment.PaymentConfirmationActivity;
import com.payment.payrowapp.sharepref.SharedPreferenceUtil;
import com.payment.payrowapp.sunmipay.LoadingDialog;
import com.payment.payrowapp.utils.BaseActivity;
import com.payment.payrowapp.utils.Constants;
import com.payment.payrowapp.utils.ContextUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.observers.DisposableObserver;

public class CPOCConnectActivity extends BaseActivity implements View.OnClickListener, NfcEventListener {
    TextView textView, textViewSecondsLeft;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    SchedulerFacade schedulerFacade = new SchedulerProvider();
    public static final PosManager posManager = CPosSdk.sINSTANCE.posManager();

    public static final PaymentManager paymentManager = CPosSdk.sINSTANCE.paymentManager();
    public static final TransactionManager transactionManager = CPosSdk.sINSTANCE.transactionManager();

    PaymentDataModel paymentDataModel;
    public static final String TAX_TYPE_SALES = "sales";
    public static final String TAX_TYPE_FEDERAL = "federal";
    private String mTransactionId = "";

    private boolean isTransactionSuccess = false;

    ProgressBar mPbProgressBar;
    EditText amountEditText;
    FeeResponseData feeResponseData;
    String orderNumber;
    SharedPreferenceUtil sharedPreferenceUtil;
    CardPaymentViewModel cardPaymentViewModel;
    String paymentTypeChan;
    String payRowDigitFee;
    Boolean payRowVATStatus;
    Float payRowVATAmount;
    boolean signatureStatus = false, isPinBlock = false;
    String amount;
    PaymentOutcomeResponse paymentOutcomeResponse;
    TextView tvTimer;
    CountDownTimer countDownTimer;
    public static final long READ_PAYCARD_VIBE_TIME = 200;
    private LoadingDialog loadDialog;
    String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IjBFMzMwQjVBN0EwQzA2RkQifQ.eyJpc3MiOiJQYXlSb3ciLCJkZXZpY2" +
            "VfaWQiOiI5MWZjOGI1YmRmNjMtNGVhZS04MWRiLTYzZWRmYTRiZWNlOCIsImp0aSI6IjE3MjI0Mjg3ODUzMTkiLCJzdWIiOiJQYXlSb3ciLC" +
            "JhdWQiOiJjbG91ZHBvcyIsIm5iZiI6MTc0MjM3MjcyMiwiZXhwIjoxNzczOTE1NTI1LCJpYXQiOjE3NDIzNzI3MjJ9.VEaTZckl9gLQKqMYW" +
            "B3K5b2clmmJJBT6e32hVhifk4xmCAY3wyh3hgk1qpPHu7xkc6zfzJzdkWo3gY5fXYx48UvOUFncoIYmVh5t0vf2A2ZdVgxvVM10IpJPSERNZ" +
            "by2uVt92qakQR0voli9La97yyqS-WuiwrYv0cmqV_Kyh899Xg-RFJBINZnx7zY7DUA5i5nqhw_Uii5_xJR82Qy3-1kC_ualFTmQCkgEbGbcn" +
            "gZ6o-zetunpJuhvzDQPwq1b-T183jEScHDMv3cq5_lq2c2V5Lkx3QUrh3y5k-WBrzVf0KD-OvI2B8O7RU_0b69DWIfKum9g-YcUhFS-Ioxha" +
            "w";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cpocconnect);
        textView = findViewById(R.id.textViewTransactionRef);

        textViewSecondsLeft = findViewById(R.id.textViewSecondsLeft);
        mPbProgressBar = (ProgressBar) findViewById(R.id.pbProgress);
        amountEditText = findViewById(R.id.edtAmount);
        findViewById(R.id.buttonCancel).setOnClickListener(this);
        tvTimer = findViewById(R.id.textViewTimeoutSeconds);

        sharedPreferenceUtil = new SharedPreferenceUtil(getBaseContext());

        Bundle bundle = getIntent().getExtras();
        // String amount = bundle.getString("AMOUNT");
        feeResponseData = bundle.getParcelable("feeResponseData");
        orderNumber = bundle.getString("OrderNumber");

        paymentTypeChan = bundle.getString("PAYMENT TYPE");
        payRowDigitFee = bundle.getString("PayRowDigFee");

        payRowVATAmount = bundle.getFloat("payRowVATAmount");
        payRowVATStatus = bundle.getBoolean("payRowVATStatus");
        amountEditText.setText(feeResponseData.getAmount());
        // Activate the POS
        //  textView.setText("Activating POS..");
        // textViewSecondsLeft.setText("Activating POS..");

        cardPaymentViewModel = new ViewModelProvider(CPOCConnectActivity.this, (ViewModelProvider.Factory) new CardPaymentViewModelFactory()).get(CardPaymentViewModel.class);
        String transAmount = ContextUtils.Companion.getTransAmount(feeResponseData.getAmount());

        //dubai
        paymentDataModel = getPaymentDataObject("0784", transAmount, "AE");

        //india
        //paymentDataModel = getPaymentDataObject("0784", transAmount, "IN");

        // portugal
        // paymentDataModel = getPaymentDataObject("AED", transAmount, "PT");

        posActivation();
    }

    private void posActivation() {
        try {
            //  posManager.grantAuthorizationToken(token);
            // posManager.saveHashOfKeyStore(sharedPreferenceUtil.getHashKey());
            MerchantSettingsData merchantSettingsData = new MerchantSettingsData();
            //merchantSettingsData.setMerchantCountryCode("IN");
            merchantSettingsData.setMerchantCountryCode("AE");
            merchantSettingsData.setMerchantCurrencyCode("0784");
            merchantSettingsData.setMerchantCategoryCode("1298");

            compositeDisposable.add(posManager
                    .prepare(getApplicationContext(), sharedPreferenceUtil.getHashKey(), token, merchantSettingsData)// token is for dev - sharedPreferenceUtil.getAuthKey()
                    .subscribeOn(schedulerFacade.io())
                    .observeOn(schedulerFacade.ui())
                    .doOnSubscribe(__ -> {
                        // showProgressBar();
                        showAnimatedProgressDialog();
                        System.out.println("Loading");
                    })
                    .subscribe(
                            response -> {
                                //   hideProgressBar();
                                //   System.out.println("Activate Pos::isRecommendedUpgrade:" + response.isRecommendedUpgrade());
                                //  System.out.println("Activate Pos::isForceUpgrade:" + response.isForceUpgrade());
                                textView.setVisibility(View.VISIBLE);
                                textView.setText("Prepare SDk::response:" + new Gson().toJson(response));
                                //  setConfiguration();
                                initiatePayment();
                            },
                            throwable -> {
                                closeProgressDialog();
                                // hideProgressBar();
                                PosException exception = (PosException) throwable;
                                String error_code = exception.getErrorCode();
                                String ex_message = exception.getMessage();
                                System.out.println("Prepare SDK::errorCode:" + error_code);
                                System.out.println("Prepare SDK::errorMessage:" + ex_message);
                                textView.setVisibility(View.VISIBLE);
                                textView.setText("Prepare SDK::errorCode:" + error_code + "\nPrepare SDK::errorMessage:" + ex_message);
                                runOnUiThread(() -> {
                                    cardPaymentViewModel.cardDeclineUpdate(orderNumber, payRowDigitFee, amount, payRowVATStatus, payRowVATAmount, signatureStatus, isPinBlock, amountEditText.getText().toString(), paymentTypeChan, this, sharedPreferenceUtil, ex_message != null ? ex_message : "error", error_code);
                                });

                            }
                    )
            );
        } catch (Exception e) {
            textView.setVisibility(View.VISIBLE);
            textView.setText("Auth Exception" + e.getMessage());
            String input = e.getMessage();
            String[] parts = input.split(":");
            e.printStackTrace();
            runOnUiThread(() -> {
                cardPaymentViewModel.cardDeclineUpdate(orderNumber, payRowDigitFee, amount, payRowVATStatus, payRowVATAmount, signatureStatus, isPinBlock, amountEditText.getText().toString(), paymentTypeChan, this, sharedPreferenceUtil, "Try Again Later", parts[0]);
            });
        }
    }

    private void initiatePayment() {
        compositeDisposable.add(paymentManager
                .initiatePayment(getApplicationContext(), getPaymentDataRequestObject())
                .subscribeOn(schedulerFacade.io())
                .observeOn(schedulerFacade.ui())
                .doOnSubscribe(__ -> {
                    System.out.println("loading");
                    //    closeProgressDialog();
                    //   showProgressBar();
                })
                .doOnTerminate(
                        () -> {
                            System.out.println("done");
                            closeProgressDialog();
                            //hideProgressBar();
                        })
                .subscribe(
                        response -> {
                            closeProgressDialog();
                            startTimer();
                            //   hideProgressBar();
                            System.out.println(response.booleanValue());
                            textView.setVisibility(View.VISIBLE);
                            textView.setText("Kindly Tap your Card");
                            showToast("Tap the Card");
                            textView.setText("Initiate Payment::response:" + response);
                            initiateTransaction();
                        },
                        throwable -> {
                            closeProgressDialog();
                            //  hideProgressBar();
                            PaymentException exception = (PaymentException) throwable;
                            String error_code = exception.getErrorCode();
                            String ex_message = exception.getMessage();
                            System.out.println("Initiate Payment::errorCode:" + error_code);
                            System.out.println("Initiate Payment::errorMessage:" + ex_message);
                            textView.setVisibility(View.VISIBLE);
                            textView.setText("Initiate Payment::errorCode:" + error_code + "\nInitiate Payment::errorMessage:" + ex_message);
                            runOnUiThread(() -> {
                                cardPaymentViewModel.cardDeclineUpdate(orderNumber, payRowDigitFee, amount, payRowVATStatus, payRowVATAmount, signatureStatus, isPinBlock, amountEditText.getText().toString(), paymentTypeChan, this, sharedPreferenceUtil, ex_message != null ? ex_message : "error", error_code);
                            });
                        }
                )
        );
    }


    @Override
    public void onTagDiscovered() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
       /* if (vibrator != null) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
        }*/

        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    vibrator.vibrate(VibrationEffect.createOneShot(READ_PAYCARD_VIBE_TIME, VibrationEffect.DEFAULT_AMPLITUDE));
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println(e.toString());

                    e.printStackTrace();
                }
            } else {
                try {
                    vibrator.vibrate(READ_PAYCARD_VIBE_TIME);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println(e.toString());

                    e.printStackTrace();
                }
            }
        }
        runOnUiThread(() -> {
            //  mTvCardDetected.setText("Card Detected.Continue holding...");
        });
    }

    private void initiateTransaction() {
        compositeDisposable.add(paymentManager
                .performTransaction(this, getPaymentDataRequestObject(), CPOCConnectActivity.this)
                .observeOn(schedulerFacade.ui())
                .subscribeOn(schedulerFacade.io())
                .subscribeWith(new DisposableObserver<PaymentOutcomeResponse>() {
                    @Override
                    public void onNext(PaymentOutcomeResponse response) {
                        //   showProgressBar();
                        _showLoadingDialog("Please Wait");
                        cancelTimer();
                        paymentOutcomeResponse = response;

                        //   response.getAuthorizationResponseCode();
                        textViewSecondsLeft.setText(response.getMessage());
                        System.out.println("Perform Transaction::message:" + response.getMessage());
                        textView.setVisibility(View.VISIBLE);
                        textView.setText("Perform Transaction::response:" + new Gson().toJson(response));
                    }

                    @Override
                    public void onError(@NonNull Throwable error) {
                        cancelTimer();
                        isTransactionSuccess = false;
                        //  hideProgressBar();
                        dismissLoadingDialog();
                        if (error instanceof PaymentException) {
                            PaymentException exception = (PaymentException) error;
                            String error_code = exception.getErrorCode();
                            String ex_message = exception.getMessage();
                            System.out.println("Perform Transaction::errorCode:" + error_code);
                            textView.setVisibility(View.VISIBLE);
                            textView.setText("Perform Transaction::errorCode:" + error_code + "\nPerform Transaction::message:" + exception.getMessage());

                            if (error_code.equals("TRY_AGAIN")) {
                                tryAgain();
                            } else {
                                runOnUiThread(() -> {
                                    showToast("Failed");
                                    LoadConfirmationActivity();
                                });
                            }
                            //   cardPaymentViewModel.cardDeclineUpdate(orderNumber, payRowDigitFee, amount, payRowVATStatus, payRowVATAmount, signatureStatus, isPinBlock, amountEditText.getText().toString(), paymentTypeChan, CPOCConnectActivity.this, sharedPreferenceUtil, ex_message != null ? ex_message : "error", error_code);
                        } else {
                            textView.setVisibility(View.VISIBLE);
                            textView.setText("Generic Error");
                            runOnUiThread(() -> {
                                LoadConfirmationActivity();
                                showToast("Generic Error");
                                // cardPaymentViewModel.cardDeclineUpdate(orderNumber, payRowDigitFee, amount, payRowVATStatus, payRowVATAmount, signatureStatus, isPinBlock, amountEditText.getText().toString(), paymentTypeChan, CPOCConnectActivity.this, sharedPreferenceUtil, "Generic Error", "100");
                            });
                        }
                    }

                    @Override
                    public void onComplete() {
                        isTransactionSuccess = true;
                        //  hideProgressBar();
                        dismissLoadingDialog();
                        //  textViewSecondsLeft.setText("Transaction Successful");

                       // showToast("Transaction Successful");

                        try {
                            paymentManager.disConnectReader();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        String status;
                        if (paymentOutcomeResponse.getCvmAction() != null && (Objects.equals(paymentOutcomeResponse.getCvmAction(), "ONLINE_PIN")
                                || Objects.equals(paymentOutcomeResponse.getCvmAction(), "OFFLINE_PIN"))) {
                            isPinBlock = true;
                        }

                        if (paymentOutcomeResponse.getCvmAction() != null && Objects.equals(paymentOutcomeResponse.getCvmAction(), "SIGNATURE")) {
                            signatureStatus = true;
                        }

                        if (paymentOutcomeResponse.getAuthorizationResponseCode() != null && paymentOutcomeResponse.getAuthorizationResponseCode().equals("00")) {
                            status = "CAPTURED";
                            // showToast("Transaction Successful");
                        } else {
                            // showToast("Transaction Not Successful");
                            status = "NOT CAPTURED";
                        }
                        Intent intent = new Intent(CPOCConnectActivity.this, PaymentConfirmationActivity.class);
                        intent.putExtra("TYPE", "TAPTOPAY");

                        if (paymentOutcomeResponse.getMaskedCardNumber() != null) {
                            intent.putExtra("CARDNO", paymentOutcomeResponse.getMaskedCardNumber());
                        }// showToast("cardNo:" + paymentOutcomeResponse.getMaskedCardNumber());
                        if (paymentOutcomeResponse.getAuthorizationCode() != null) {
                            intent.putExtra("authCode", paymentOutcomeResponse.getAuthorizationCode());
                        }

                        //  showToast("AuthAmount:" + new String(paymentOutcomeResponse.getAuthorizedAmount(), StandardCharsets.UTF_8));
                        if (paymentOutcomeResponse.getCardBrandName() != null) {
                            intent.putExtra("CardBrand", paymentOutcomeResponse.getCardBrandName());
                        }
                        intent.putExtra("INVOICENO", orderNumber);
                        intent.putExtra("status", status);
                        intent.putExtra("paymentType", Constants.CARD);
                        //   bundle1.putExtra("bankTransferURL", bankTransferURL);
                        intent.putExtra("isPinBlock", isPinBlock);
                        intent.putExtra("SignatureStatus", signatureStatus);
                        intent.putExtra("payRowVATAmount", payRowVATAmount);
                        intent.putExtra("payRowVATStatus", payRowVATStatus);
                        if (paymentOutcomeResponse.getAuthorizedAmount() != null) {
                            intent.putExtra("totalAmount", new String(paymentOutcomeResponse.getAuthorizedAmount(), StandardCharsets.UTF_8));// amountEditText.getText().toString());
                        }
                        intent.putExtra("payRowDigitFee", payRowDigitFee);
                        if (paymentOutcomeResponse.getAuthorizedAmount() != null) {
                            intent.putExtra("responseCode", paymentOutcomeResponse.getAuthorizationResponseCode());
                        }
                        startActivity(intent);
                        // finish();
                    }
                }));
    }

    private void tryAgain() {
        try {
            runOnUiThread(() -> new AlertDialog.Builder(this)
                    .setTitle("Try again")
                    .setMessage("Please read the card again")
                    .setPositiveButton("OK", (dia, which) -> {
                                dia.dismiss();
                                try {
                                    posActivation();
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

    private void LoadConfirmationActivity() {
        String channel;
        if (Objects.equals(paymentTypeChan, "scan qr")) {
            channel = "Third Party QRCode";
        } else {
            channel = Constants.CARD;
        }
        Bundle bundle1 = new Bundle();
        bundle1.putString("TYPE", "TAPTOPAY");
        bundle1.putString("INVOICENO", orderNumber);

        bundle1.putString("status", "FAILED");
        bundle1.putString("paymentType", channel);
        bundle1.putBoolean("isPinBlock", isPinBlock);
        bundle1.putBoolean("SignatureStatus", signatureStatus);
        bundle1.putFloat("payRowVATAmount", payRowVATAmount);
        bundle1.putBoolean("payRowVATStatus", payRowVATStatus);
        bundle1.putString("totalAmount", amountEditText.getText().toString());
        bundle1.putString("payRowDigitFee", payRowDigitFee);
        Intent intent = new Intent(CPOCConnectActivity.this, PaymentConfirmationActivity.class);
        startActivity(intent.putExtras(bundle1));
        // finish();
    }

    @Override
    public void onTagLost() {

    }

    private InitPaymentRequest getPaymentDataRequestObject() {
        InitPaymentRequest initPaymentRequest = new InitPaymentRequest();
        initPaymentRequest.setAmountAuthorizedNumeric(paymentDataModel.getAmount());
        initPaymentRequest.setConfigData(paymentDataModel.getConfigData());
        initPaymentRequest.setOrderId(orderNumber);
        initPaymentRequest.setTransactionCurrencyCode(paymentDataModel.getTransactionCurrencyCode());

        HashMap<String, String> merchantData = new HashMap<>();

        String jsonString = new Gson().toJson(feeResponseData.getServicedata());
        merchantData.put("ServiceData", jsonString);
        merchantData.put("TID", sharedPreferenceUtil.getTerminalID());
        merchantData.put("MID", sharedPreferenceUtil.getMerchantID() + "      ");


        initPaymentRequest.setMerchantCustomData(merchantData);

        //Optional
     /*   initPaymentRequest.setSubtotal(paymentDataModel.getSubtotal());
        initPaymentRequest.setTip(paymentDataModel.getTip());
        initPaymentRequest.setDiscount(paymentDataModel.getDiscount());

        ArrayList<TaxDetail> taxDetailArrayList = new ArrayList<>();
        if (paymentDataModel.getTaxDetails() != null) {
            paymentDataModel.getTaxDetails().forEach(taxDetailModel -> {
                TaxDetail taxDetails = new TaxDetail();
                taxDetails.setType(taxDetailModel.getType());
                taxDetails.setAmount(taxDetailModel.getAmount());

            });
        }
        initPaymentRequest.setTaxDetails(taxDetailArrayList);*/

        return initPaymentRequest;
    }

    private PaymentDataModel getPaymentDataObject(String transactionCurrencyCode, String amount, String country) {
        PaymentDataModel paymentDataModel = new PaymentDataModel();
        paymentDataModel.setAmount(amount);
        paymentDataModel.setTransactionCurrencyCode(transactionCurrencyCode);
        paymentDataModel.setTransactionType("00");
        paymentDataModel.setOrderId(orderNumber);
        HashMap<String, String> configDataMap = new HashMap<>();
        configDataMap.put("country", country);
        paymentDataModel.setConfigData(configDataMap);

        //optional
        /*paymentDataModel.setSubtotal(amount);
        paymentDataModel.setTip("");
        paymentDataModel.setDiscount("");
        ArrayList<TaxDetailModel> taxDetailArrayList = new ArrayList<>();
        TaxDetailModel salesTax = new TaxDetailModel();
        salesTax.setAmount("");
        salesTax.setType(TAX_TYPE_SALES);

        TaxDetailModel federalTax = new TaxDetailModel();
        federalTax.setAmount("");
        federalTax.setType(TAX_TYPE_FEDERAL);

        taxDetailArrayList.add(salesTax);
        taxDetailArrayList.add(federalTax);
        paymentDataModel.setTaxDetails(taxDetailArrayList);*/

        return paymentDataModel;
    }

    private void showProgressBar() {
        mPbProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        mPbProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.buttonCancel) {
            try {
                paymentManager.abortTransaction();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (Objects.equals(paymentTypeChan, "scan qr")) {
                //   cardPaymentViewModel.cancelTransaction(orderNumber, this, payRowDigitFee, amount, payRowVATStatus, payRowVATAmount, signatureStatus, isPinBlock, mEditAmount.getText().toString(), paymentTypeChan, /*purchaseDetails, */this, sharedPreferenceUtil);
            } else {
                Intent intent = new Intent(this, DashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        }
    }

    void startTimer() {
        countDownTimer = new CountDownTimer(33000, 1000) {

            public void onTick(long millisUntilFinished) {
                tvTimer.setText("" + millisUntilFinished / 1000);
                // logic to set the EditText could go here
            }

            public void onFinish() {
                cancel();
                finish();
            }

        }.start();
    }

    private void cancelTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
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
                }
        );
    }


    private void setConfiguration() {

        //India
        String currencyCode = "AED";
        String countryCode = "IN";

        //dubai
       /* String currencyCode = "AED";
        String countryCode = "AE";*/

        //Portugal
      /*  String currencyCode = "AED";
        String countryCode = "PT";*/

        compositeDisposable.add(posManager
                .getConfiguration(getApplicationContext(), currencyCode, countryCode)
                .subscribeOn(schedulerFacade.io())
                .observeOn(schedulerFacade.ui())
                .doOnSubscribe(__ -> {
                    System.out.println("Loading");
                    textView.setVisibility(View.VISIBLE);
                    textView.setText("get Configuration" + "Loading");
                    //      showProgressBar();
                })
                .subscribe(
                        response -> {
                            //     hideProgressBar();
                            System.out.println("Get Configuration:" + response.getCurrency());
                            textView.setVisibility(View.VISIBLE);
                            textView.setText("get configuration response----" + new Gson().toJson(response) + "check success" + response.getCurrency());
                            initiatePayment();
                        },
                        throwable -> {
                            closeProgressDialog();
                            //    hideProgressBar();
                            PosException exception = (PosException) throwable;
                            String error_code = exception.getErrorCode();
                            String ex_message = exception.getMessage();
                            System.out.println("get config::errorCode:" + error_code);
                            System.out.println("get config::errorMessage:" + ex_message);
                            textView.setVisibility(View.VISIBLE);
                            textView.setText("get config::errorCode:" + error_code + "\nget config::errorMessage:" + ex_message);
                            runOnUiThread(() -> {
                                cardPaymentViewModel.cardDeclineUpdate(orderNumber, payRowDigitFee, amount, payRowVATStatus, payRowVATAmount, signatureStatus, isPinBlock, amountEditText.getText().toString(), paymentTypeChan, this, sharedPreferenceUtil, ex_message != null ? ex_message : "error", error_code);
                            });
                        }
                )
        );
    }
}