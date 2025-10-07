package com.payment.payrowapp.app;

import com.mastercard.cpos.facade.CposApplication;

/*public class PayRowCPOCApplication extends CposApplication<CposApplication.SDKInitializationCallback>
        implements CposApplication.SDKInitializationCallback {

    @Override
    public void onCreate() {
        super.onCreate();
        CPosSdk.sINSTANCE.posManager().grantAuthorizationToken("");
        CPosSdk.sINSTANCE.posManager().saveHashOfKeyStore("");
    }

    @Override
    public void onSDKInitializationFailed(String s, String s1) {

    }
}*/

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Choreographer;
import android.view.WindowManager;
import android.widget.Toast;

import com.payment.payrowapp.sunmipay.Constant;
import com.payment.payrowapp.sunmipay.LogUtil;
import com.payment.payrowapp.sunmipay.ThreadPoolUtil;
import com.sunmi.keyinject.binder.KeyInjectOpt;
import com.sunmi.pay.hardware.aidlv2.emv.EMVOptV2;
import com.sunmi.pay.hardware.aidlv2.etc.ETCOptV2;
import com.sunmi.pay.hardware.aidlv2.pinpad.PinPadOptV2;
import com.sunmi.pay.hardware.aidlv2.print.PrinterOptV2;
import com.sunmi.pay.hardware.aidlv2.readcard.ReadCardOptV2;
import com.sunmi.pay.hardware.aidlv2.security.DevCertManagerV2;
import com.sunmi.pay.hardware.aidlv2.security.SecurityOptV2;
import com.sunmi.pay.hardware.aidlv2.system.BasicOptV2;
import com.sunmi.pay.hardware.aidlv2.tax.TaxOptV2;
import com.sunmi.pay.hardware.aidlv2.test.TestOptV2;
import com.sunmi.rki.SunmiRKIKernel;

import java.io.File;
import java.io.IOException;

import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.sentry.ITransaction;
import io.sentry.SpanStatus;
import io.sentry.android.core.SentryAndroid;
import io.sentry.protocol.TransactionNameSource;
import sunmi.paylib.SunmiPayKernel;
import sunmi.paylib.SunmiPayKernel.ConnectCallback;

import android.os.SystemClock;
import android.view.Window;
import io.sentry.Sentry;

public  class MyApplication extends CposApplication<CposApplication.SDKInitializationCallback>
        implements CposApplication.SDKInitializationCallback {
    private static final String TAG = "PayRowApplication";

    public EMVOptV2 emvOptV2;
    public PinPadOptV2 pinPadOptV2;
    public ReadCardOptV2 readCardOptV2;
    public BasicOptV2 basicOptV2;
    public SecurityOptV2 securityOptV2;

    public TaxOptV2 taxOptV2;
    public ETCOptV2 etcOptV2;
    public PrinterOptV2 printerOptV2;
    public TestOptV2 testOptV2;
    public DevCertManagerV2 devCertManagerV2;
    public KeyInjectOpt mKeyInjectOpt;

    private boolean connectPaySDK = false;

    public static MyApplication INSTANCE;
    public static MyApplication app;

    @Override
    public void onCreate() {
        super.onCreate();

        /*try {
            writeLogToFile(this);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        // Initialize Sentry
        SentryAndroid.init(this, options -> {

            // Capture 100% of performance transactions (adjust in production)
            options.setTracesSampleRate(1.0);

            // Enable ANR detection (default: true)
            options.setAnrEnabled(true);

            // Optional: Debug logs for development
            options.setDebug(true);

            options.getLogs().setEnabled(true);

        });
        setupActivityListener();
        startUiFreezeDetector();

        assignInstance(this);
        //  bindPaySDKService();
        //  mConnectHandler.sendEmptyMessageDelayed(1, 1000);
        RxJavaPlugins.setErrorHandler(exception -> exception.getMessage());
    }

    private void assignInstance(MyApplication myApplication) {
        INSTANCE = myApplication;
        app = myApplication;
    }

    public boolean isConnectPaySDK() {
        return connectPaySDK;
    }

    // Bind PaySDK service
    private void bindPaySDKService() {
        SunmiPayKernel payKernel = SunmiPayKernel.getInstance();
        payKernel.initPaySDK(this, new ConnectCallback() {
            @Override
            public void onConnectPaySDK() {
                LogUtil.e(Constant.TAG, "onConnectPaySDK...");
                emvOptV2 = payKernel.mEMVOptV2;
                basicOptV2 = payKernel.mBasicOptV2;
                pinPadOptV2 = payKernel.mPinPadOptV2;
                readCardOptV2 = payKernel.mReadCardOptV2;
                securityOptV2 = payKernel.mSecurityOptV2;
                taxOptV2 = payKernel.mTaxOptV2;
                etcOptV2 = payKernel.mETCOptV2;
                printerOptV2 = payKernel.mPrinterOptV2;
                testOptV2 = payKernel.mTestOptV2;
                devCertManagerV2 = payKernel.mDevCertManagerV2;
                connectPaySDK = true;

                ThreadPoolUtil.executeInCachePool(() -> ThreadPoolUtil.executeInCachePool(() -> {
                    // EmvUtil.initRidAndAid();
                }));
            }

            @Override
            public void onDisconnectPaySDK() {
                LogUtil.e(Constant.TAG, "onDisconnectPaySDK...");
                connectPaySDK = false;
                taxOptV2 = null;
                etcOptV2 = null;
                printerOptV2 = null;
                devCertManagerV2 = null;
                Toast.makeText(app, "Connection Failed", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupActivityListener() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    monitorFirstFrame(activity, 3000); // 3s threshold
                } else {
                    monitorFirstFrameLegacy(3000);
                }
                /*  activity.getWindow().setFlags(
                        WindowManager.LayoutParams.FLAG_SECURE,
                        WindowManager.LayoutParams.FLAG_SECURE
                );*/
            }

            @Override
            public void onActivityStarted(Activity activity) {
            }

            @Override
            public void onActivityResumed(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });
    }

    // Detect slow first frame (Nougat+)
    private void monitorFirstFrame(Activity activity, final long maxAllowedMs) {
        final long start = System.currentTimeMillis();

        final Window.OnFrameMetricsAvailableListener[] listenerRef = new Window.OnFrameMetricsAvailableListener[1];

        listenerRef[0] = new Window.OnFrameMetricsAvailableListener() {
            @Override
            public void onFrameMetricsAvailable(Window window, android.view.FrameMetrics frameMetrics, int dropCount) {
                long duration = System.currentTimeMillis() - start;
                if (duration > maxAllowedMs) {
                    BlackScreenException exception = new BlackScreenException(
                            "Black screen suspected in " + activity.getLocalClassName() +
                                    ", first frame = " + duration + "ms"
                    );
                    Thread mainThread = Looper.getMainLooper().getThread();
                    exception.setStackTrace(mainThread.getStackTrace());

                    Sentry.setTag("freeze_type", "black_screen");
                    Sentry.captureException(exception);

                    ITransaction tx = Sentry.startTransaction(
                            "App Startup / Black Screen",
                            "startup"
                    );
                    tx.setStatus(SpanStatus.DEADLINE_EXCEEDED);
                    tx.finish();
                }

                // ✅ remove listener safely
                try {
                    activity.getWindow().removeOnFrameMetricsAvailableListener(listenerRef[0]);
                } catch (IllegalArgumentException ignore) {
                    // already removed
                }
            }
        };

        activity.getWindow().addOnFrameMetricsAvailableListener(
                listenerRef[0],
                new Handler(activity.getMainLooper())
        );
    }



    // Detect slow first frame (legacy < N)
    private void monitorFirstFrameLegacy(final long maxAllowedMs) {
        final long start = System.currentTimeMillis();
        Choreographer.getInstance().postFrameCallback(new Choreographer.FrameCallback() {
            @Override
            public void doFrame(long frameTimeNanos) {
                long duration = System.currentTimeMillis() - start;
                if (duration > maxAllowedMs) {
                    BlackScreenException exception = new BlackScreenException(
                            "Black screen suspected (legacy), first frame = " + duration + "ms"
                    );
                    Thread mainThread = Looper.getMainLooper().getThread();
                    exception.setStackTrace(mainThread.getStackTrace());

                    Sentry.setTag("freeze_type", "black_screen");
                    Sentry.captureException(exception);

                    ITransaction tx = Sentry.startTransaction(
                            "App Startup / Black Screen (Legacy)",
                            "startup"
                    );
                    tx.setStatus(SpanStatus.DEADLINE_EXCEEDED);
                    tx.finish();
                }
            }
        });
    }

    // Detect UI freezes after startup with stack trace
    private void startUiFreezeDetector() {
        final Handler mainHandler = new Handler(Looper.getMainLooper());

        final Runnable watchdog = new Runnable() {
            private long lastTick = SystemClock.uptimeMillis();

            @Override
            public void run() {
                long now = SystemClock.uptimeMillis();
                long diff = now - lastTick;

                if (diff > 1500) { // main thread delayed by >1.5s
                    Thread mainThread = Looper.getMainLooper().getThread();
                    StackTraceElement[] stackTrace = mainThread.getStackTrace();

                    UIFreezeException exception = new UIFreezeException(
                            "UI freeze detected! Main thread blocked for " + diff + "ms"
                    );
                    exception.setStackTrace(stackTrace);

                    // Send to Errors tab
                    Sentry.setTag("freeze_type", "ui_freeze");
                    Sentry.captureException(exception);

                    // Send to Performance tab
                    ITransaction tx = Sentry.startTransaction(
                            "UI Freeze",
                            "runtime"
                    );
                    tx.setStatus(SpanStatus.DEADLINE_EXCEEDED);
                    tx.finish();
                }

                lastTick = now;
                mainHandler.postDelayed(this, 500);
            }
        };

        mainHandler.post(watchdog);
    }

    // Custom exception types for better grouping in Sentry
    public static class UIFreezeException extends RuntimeException {
        public UIFreezeException(String message) {
            super(message);
        }
    }

    public static class BlackScreenException extends RuntimeException {
        public BlackScreenException(String message) {
            super(message);
        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler mConnectHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (mKeyInjectOpt == null) {
                bindRKISDKService();
                sendEmptyMessageDelayed(0, 1000);
            } else {
                removeCallbacksAndMessages(null);
            }
        }
    };

    private final SunmiRKIKernel.ConnectRKISDKCallback connectRKISDKCallback = new SunmiRKIKernel.ConnectRKISDKCallback() {
        @Override
        public void onConnectRKISDK() {
            mKeyInjectOpt = SunmiRKIKernel.getInstance().mKeyInjectOpt;
            Log.e(TAG, "onConnectRKISDK");
        }

        @Override
        public void onDisconnectRKISDK() {
            mKeyInjectOpt = null;
            Log.e(TAG, "onDisconnectRKISDK");
            bindRKISDKService();
        }
    };

    // Bind RKI SDK Service
    private void bindRKISDKService() {
        SunmiRKIKernel.getInstance().initRKISDK(this, connectRKISDKCallback);
    }

    // Write log to file
    public void writeLogToFile(Context context) throws IOException {
        String fileName = "logcat1.txt";
        File file = new File(context.getExternalCacheDir(), fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        String command = "logcat -f " + file.getAbsolutePath();
        Runtime.getRuntime().exec(command);
    }

    @Override
    public void onSDKInitializationSuccess() {

    }

    @Override
    public void onSDKInitializationFailed(String s) {

    }

    /*@Override
    public void onSDKInitializationFailed(String s, String s1) {

    }*/

    /*@Override
    public void onSDKInitializationSuccess() {

    }

    @Override
    public void onSDKInitializationFailed(String s) {
        Toast.makeText(this, "reason" + ":" + s, Toast.LENGTH_LONG).show();
    }*/
}

