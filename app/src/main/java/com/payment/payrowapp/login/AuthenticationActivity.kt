package com.payment.payrowapp.login

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.os.RemoteException
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.payment.payrowapp.R
import com.payment.payrowapp.app.MyApplication
import com.payment.payrowapp.databinding.ActivityAuthenticationBinding
import com.payment.payrowapp.databinding.ActivityInvoicesListBinding
import com.payment.payrowapp.dataclass.AuthCodeRequest
import com.payment.payrowapp.dataclass.LoginOTPRequest
import com.payment.payrowapp.dataclass.OTPRequest
import com.payment.payrowapp.dataclass.VerifyOTPRequest
import com.payment.payrowapp.invoicerecall.InvoicesListActivity
import com.payment.payrowapp.observeOnce
import com.payment.payrowapp.paymenthistory.DailyReportActivity
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.Constants
import com.payment.payrowapp.utils.ContextUtils
import com.sunmi.keyinject.binder.CommonCallback
import com.sunmi.keyinject.binder.QueryKeyCallback
import com.sunmi.keyinject.http.bean.KeyPropertyRspEntity
import java.util.concurrent.TimeUnit


class AuthenticationActivity : AppCompatActivity() {
    // var preference: SharedPreferences? = null
    private lateinit var authenticationViewModel: AuthenticationViewModel
    lateinit var timer: CountDownTimer
    lateinit var sharedPreferenceUtil: SharedPreferenceUtil

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private lateinit var binding: ActivityAuthenticationBinding
    var ring: MediaPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_authentication)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar.myToolbar)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        ring = MediaPlayer.create(this, R.raw.sound_button_click)

        val bundle = intent.extras
        if (bundle?.getString("Type").equals("InvoiceRecall") || bundle?.getString("FROM")
                .equals("DAILY") || bundle?.getString("FROM")
                .equals("MONTHLY")
        ) {
            supportActionBar?.title = "Payment History"
        } else {
            supportActionBar?.title = "Get Started"
        }

        sharedPreferenceUtil = SharedPreferenceUtil(this)

        /*preference = getSharedPreferences(
            resources.getString(R.string.app_name),
            Context.MODE_PRIVATE
        )*/

        authenticationViewModel =
            ViewModelProvider(
                this,
                AuthViewModelFactory(this)
            )[AuthenticationViewModel::class.java]


        binding.btnSignIn.setOnClickListener {
            ring?.start()
            if (validateLogin()) {
                val myBundle = Bundle()

                if (bundle?.getString("Type").equals("InvoiceRecall")) {
                    myBundle.putString(Constants.FROM, bundle?.getString(Constants.FROM))
                    myBundle.putString(Constants.TO, bundle?.getString(Constants.TO))
                    myBundle.putString(Constants.FROM_DATE, bundle?.getString(Constants.FROM_DATE))
                    myBundle.putString(Constants.TO_DATE, bundle?.getString(Constants.TO_DATE))
                    val verifyOTPRequest = VerifyOTPRequest(
                        binding.pinview.value,
                        sharedPreferenceUtil.getTerminalID(),
                        null,
                        ContextUtils.randomValue().toString()
                    ) //preference!!.getString(Constants.MERCHANT_ID, "")
                    authenticationViewModel.verifyOTPResponse(verifyOTPRequest)
                    authenticationViewModel.getVerifyOTPData()
                        .observeOnce(this@AuthenticationActivity) {
                            startActivity(
                                Intent(this, InvoicesListActivity::class.java).putExtras(
                                    myBundle
                                )
                            )
                            finish()
                        }
                } else if (bundle?.getString("FROM").equals("DAILY") || bundle?.getString("FROM")
                        .equals("MONTHLY")
                ) {
                    myBundle.putString("FROM", bundle?.getString("FROM"))
                    val verifyOTPRequest = VerifyOTPRequest(
                        binding.pinview.value,
                        sharedPreferenceUtil.getTerminalID(),
                        null,
                        ContextUtils.randomValue().toString()
                    ) //preference!!.getString(Constants.MERCHANT_ID, "")
                    authenticationViewModel.verifyOTPResponse(verifyOTPRequest)
                    authenticationViewModel.getVerifyOTPData()
                        .observeOnce(this@AuthenticationActivity) {
                            startActivity(
                                Intent(this, DailyReportActivity::class.java).putExtras(
                                    myBundle
                                )
                            )
                            finish()
                        }
                } else if (bundle?.getString("AUTH").equals("TID")) {
                    // val sunmiStatus = bundle?.getString("sunmiStatus")
                    val authCodeRequest = AuthCodeRequest(
                        binding.pinview.value,
                        bundle?.getString("TID")!!,
                        ContextUtils.getDeviceId(this),
                        ContextUtils.randomValue().toString(),
                        false,
                        null
                    )
                    authenticationViewModel.verifyAuthCodeMutableLiveData(authCodeRequest,sharedPreferenceUtil)
                    authenticationViewModel.getVerifyAuthData()
                        .observeOnce(this@AuthenticationActivity) {

                            sharedPreferenceUtil.setISLogin(true)
                            sharedPreferenceUtil.setTerminalID(bundle.getString("TID")!!)

                            val intent = Intent(
                                this,
                                CreatePinActivity::class.java
                            ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(
                                intent
                            )
                            finish()
                        }
                } else if (bundle?.getString("TYPE").equals("Forgot PIN")) {
                    val authCodeRequest = AuthCodeRequest(
                        binding.pinview.value,
                        sharedPreferenceUtil.getTerminalID(),
                        ContextUtils.getDeviceId(this),
                        ContextUtils.randomValue().toString(),
                        false,
                        null
                    )
                    authenticationViewModel.verifyAuthCodeMutableLiveData(authCodeRequest,sharedPreferenceUtil)
                    authenticationViewModel.getVerifyAuthData()
                        .observeOnce(this@AuthenticationActivity) {
                            val intent = Intent(this, CreatePinActivity::class.java)
                            startActivity(
                                intent
                            )
                            finish()
                        }
                } else if (bundle?.getString("TYPE").equals("Logout")) {
                    //  val sunmiStatus = bundle?.getString("sunmiStatus")
                    val authCodeRequest = AuthCodeRequest(
                        binding.pinview.value,
                        bundle?.getString("TID")!!,
                        ContextUtils.getDeviceId(this),
                        ContextUtils.randomValue().toString(),
                        false,
                        null
                    )

                    authenticationViewModel.verifyAuthCodeMutableLiveData(authCodeRequest,sharedPreferenceUtil)
                    authenticationViewModel.getVerifyAuthData()
                        .observeOnce(this@AuthenticationActivity) {

                            sharedPreferenceUtil.setISLogin(true)
                            sharedPreferenceUtil.setTerminalID(bundle.getString("TID")!!)

                            val intent = Intent(
                                this,
                                CreatePinActivity::class.java
                            ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(
                                intent
                            )
                            finish()
                        }
                }
                //
            } else {
                showToast("Please enter a valid authentication code to sign in")
            }
        }

        // startTimer()
        binding.btnResendCode.setOnClickListener {
            ring?.start()
            if (this::timer.isInitialized) {
                timer.cancel()
            }
            startTimer()

            if (bundle?.getString("Type").equals("InvoiceRecall")) {
                binding.tvResendCode.visibility = View.VISIBLE
                binding.tvResendCode.text = getString(R.string.resend_otp)
                val tid = sharedPreferenceUtil.getTerminalID()
                val otpRequest = OTPRequest(
                    tid, null
                )
                getOTP(otpRequest)
            } else if (bundle?.getString("FROM").equals("DAILY") || bundle?.getString("FROM")
                    .equals("MONTHLY")
            ) {
                binding.tvResendCode.visibility = View.VISIBLE
                binding.tvResendCode.text = getString(R.string.resend_otp)

                val tid = sharedPreferenceUtil.getTerminalID()
                val otpRequest = OTPRequest(
                    tid, null
                )
                getOTP(otpRequest)
            } else if (bundle?.getString("TYPE").equals("Forgot PIN")) {
                binding.tvResendCode.visibility = View.VISIBLE
                binding.tvResendCode.text = getString(R.string.resend_otp)
                val terminalID =
                    sharedPreferenceUtil.getTerminalID()//preference!!.getString(Constants.TERMINAL_ID, "")
                val otpRequest = LoginOTPRequest(
                    terminalID
                )
                authenticationViewModel.getLoginOTPResponse(otpRequest,sharedPreferenceUtil)
                //  getOTP(otpRequest)
            } else if (bundle?.getString("AUTH").equals("TID") || bundle?.getString("TYPE")
                    .equals("Logout")
            ) {
                binding.tvResendCode.visibility = View.VISIBLE
                val otpRequest = LoginOTPRequest(
                    bundle?.getString("TID")!!
                )
                authenticationViewModel.getLoginOTPResponse(otpRequest,sharedPreferenceUtil)
                // getOTP(otpRequest)
            }
        }

        // set the values
        if (bundle?.getString("FROM").equals("DAILY") || bundle?.getString("FROM")
                .equals("MONTHLY")
        ) {
            binding.tvSignIn.text = getString(R.string.enter_otp)
            binding.tvByUsingSmartId.text = getString(R.string.otp_sent_via_email)
            binding.btnResendCode.text = getString(R.string.send_otp)
            /* val merchantID = preference!!.getString(Constants.MERCHANT_ID, "")
             val otpRequest = merchantID?.let {
                 OTPRequest(
                     null, it
                 )
             }
             getOTP(otpRequest)*/
        } else if (bundle?.getString("TYPE").equals("Forgot PIN")) {
            binding.tvSignIn.text = getString(R.string.enter_otp)
            binding.tvByUsingSmartId.text = getString(R.string.otp_sent_via_email)
            binding.btnResendCode.text = getString(R.string.send_otp)
            /*  val terminalID = preference!!.getString(Constants.TERMINAL_ID, "")
              val otpRequest = OTPRequest(
                  terminalID, null
              )
              getOTP(otpRequest)*/
        } /*else if (bundle?.getString("TYPE").equals("Logout")) {
            val otpRequest = OTPRequest(
                bundle?.getString("TID")!!, null
            )
            getOTP(otpRequest)
        }*/
    }

    private fun startTimer() {
        // timer running
        timer = object : CountDownTimer(120 * 1000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                try {
                    val secsLeft: Int = (millisUntilFinished / 1000).toInt()
                  //  binding.btnResendCode.text = "00:$secsLeft"
                    binding.btnResendCode.text = String.format(
                        "0%d:%d",
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(
                                    TimeUnit.MILLISECONDS.toMinutes(
                                        millisUntilFinished
                                    )
                                )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFinish() {
//                showToast("Timeout! Please try again")
                cancel()
                finish()
            }
        }
        timer.start()
    }

    private fun validateLogin(): Boolean {
        if (binding.pinview.value.isNotEmpty() && binding.pinview.value.length == 4) {
            return true
        }
        return false
    }

    private fun getOTP(otpRequest: OTPRequest?) {
        if (otpRequest != null) {
            authenticationViewModel.getOTPResponse(otpRequest)
        }
        authenticationViewModel.getData().observeOnce(this@AuthenticationActivity) {

        }
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun queryKeyList() {
        try {
            if (MyApplication.app.mKeyInjectOpt != null) {
                MyApplication.app.mKeyInjectOpt!!.queryKeyList(0, object : QueryKeyCallback.Stub() {
                    @Throws(RemoteException::class)
                    override fun onResponse(list: List<KeyPropertyRspEntity>) {
                        val temp = StringBuilder()
                        for (keyPropertyRspEntity in list) {
                            temp.append(keyPropertyRspEntity.toString())
                            temp.append("\n")
                            Log.e("KeyPropertyRspEntity", keyPropertyRspEntity.toString())
                        }
                    }

                    @Throws(RemoteException::class)
                    override fun onFailure(i: Int) {
                        // ToastUtils.toast(this@MainActivity, "onFailure")
                    }
                })
            } else {
                Toast.makeText(this, "Please connect RKI SDK first!", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun downloadKey(tid: String) {
        try {
            if (MyApplication.app.mKeyInjectOpt != null) {
                MyApplication.app.mKeyInjectOpt!!.downloadKey(0, object : CommonCallback.Stub() {
                    @Throws(RemoteException::class)
                    override fun onSuccess() {
                        Log.e("download key", "onSuccess")
                        runOnUiThread {
                            sharedPreferenceUtil.setISLogin(true)
                            sharedPreferenceUtil.setTerminalID(tid)

                            val intent = Intent(
                                this@AuthenticationActivity,
                                CreatePinActivity::class.java
                            ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(
                                intent
                            )
                            finish()
                            Toast.makeText(
                                this@AuthenticationActivity,
                                "Key Downloaded successfully",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    }

                    @Throws(RemoteException::class)
                    override fun onFailure(i: Int) {
                        Log.e("download key", "onFailure: $i")
                        runOnUiThread {
                            try {
                                sharedPreferenceUtil.clearPreferences()
                            } catch (e: IllegalArgumentException) {

                            }
                            sharedPreferenceUtil.setISLogin(false)
                            Toast.makeText(
                                this@AuthenticationActivity,
                                "Key Downloading failed.Please register the device again. ",
                                Toast.LENGTH_LONG
                            ).show()
                            finish()
                        }
                    }
                })
            } else {
                Toast.makeText(
                    this@AuthenticationActivity,
                    "Please connect RKI SDK first!",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

}