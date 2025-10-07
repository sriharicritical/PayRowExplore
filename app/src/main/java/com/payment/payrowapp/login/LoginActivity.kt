package com.payment.payrowapp.login

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationServices
import com.payment.payrowapp.R
import com.payment.payrowapp.databinding.ActivityLoginNewBinding
import com.payment.payrowapp.dataclass.CreatePINRequest
import com.payment.payrowapp.dataclass.LoginRequest
import com.payment.payrowapp.observeOnce
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.ContextUtils


class LoginActivity : AppCompatActivity() {
    var ring: MediaPlayer? = null
    var userId: String = ""
    var password: String = ""
    lateinit var preference: SharedPreferences
    lateinit var bundle: Bundle
    lateinit var sharedPreferenceUtil: SharedPreferenceUtil

    private val LOCATION_PERMISSION_REQUEST_CODE = 102
    val REQUEST_AUDIO_PERMISSION_CODE = 101


    var latitude: String = ""
    var longitude: String = ""

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private lateinit var binding: ActivityLoginNewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_login_new)
        binding = ActivityLoginNewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar.myToolbar)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Get Started"

        bundle = intent.extras!!
        if (bundle.getString("TYPE").equals("Create PIN")) {
            // timer running
            val timer = object : CountDownTimer(46000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    try {
                        val secsLeft: Int = (millisUntilFinished / 1000).toInt()
                        binding.btnForgotPin.text = "00:$secsLeft"
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
        } else {
            binding.tvSignIn.text = bundle.getString("heading")
            binding.btnForgotPin.text = getString(R.string.forgot_pin)
            binding.tvByUsingSmartId.text = resources.getString(R.string.to_process_your_login)
            checkLocationPermission()
        }

        ring = MediaPlayer.create(this, R.raw.sound_button_click)

        // etEnterPIN.transformationMethod = PasswordTransformationMethod()

        //etUserId.addTextChangedListener(PatternedTextWatcher("###-####-#######-#"))

        ContextUtils.clearPreferences(this)
        preference =
            getSharedPreferences(resources.getString(R.string.app_name), Context.MODE_PRIVATE)

        sharedPreferenceUtil = SharedPreferenceUtil(this)

        val loginViewModel =
            ViewModelProvider(this, LoginViewModelFactory(this)).get(LoginViewModel::class.java)

        //Get All files permission
        val getpermission = Intent()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getpermission.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
        }


        binding.ivLoginSmartId.setOnClickListener {

//            if (validateLogin()) {
            ring?.start()
//            startActivity(Intent(this, DashboardActivity::class.java))
            val bundle1 = Bundle()
            bundle1.putString("TYPE", "LOGIN")
            showToast("Please enter the PIN number and Sign In")
            //startActivity(Intent(this, FingerPrintActivity::class.java).putExtras(bundle1))
        }
        binding.btnSignIn.setOnClickListener {
            ring?.start()
            /* if (Build.VERSION.SDK_INT >= 30) {
                 if (!Environment.isExternalStorageManager()) {
                     showToast("Please enable file access permission!")
                     startActivity(getpermission)
                 } else {
                     doLogin(loginViewModel)
                 }
             } else {*/
            doLogin(loginViewModel)
            //}
        }

        // pinview.setPinBackground(resources.getDrawable(R.drawable.pinview_background,null))
        /*pinview.setPinViewEventListener { pinview, fromUser ->
            userId = pinview!!.value
            btnSignIn.isEnabled = true
            btnSignIn.setBackgroundResource(R.drawable.button_round_dark_gray_bg_fill)

        }*/

        binding.btnForgotPin.setOnClickListener {
            if (bundle.getString("TYPE").equals("Login")) {
                val intent = Intent(baseContext, AuthenticationActivity::class.java).putExtra(
                    "TYPE",
                    "Forgot PIN"
                )
                startActivity(intent)
            }
        }

    }

    private fun validateLogin(): Boolean {
        if (binding.pinview.value.isNotEmpty() && binding.pinview.value.length == 4) {
            return true
        }
        return false
    }

    private fun doLogin(loginViewModel: LoginViewModel) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please wait...")
        progressDialog.show()
        if (validateLogin()) {

            if (bundle.getString("TYPE").equals("Create PIN")) {
                if (bundle.getString("PIN") == binding.pinview.value) {
                    val createPinViewModelClass =
                        ViewModelProvider(
                            this,
                            CreatePINFactory(this)
                        ).get(CreatePinViewModelClass::class.java)
                    val createPINRequest = CreatePINRequest(
                        sharedPreferenceUtil.getTerminalID(),
                        binding.pinview.value,
                        "Active"
                    ) // preference.getString(Constants.TERMINAL_ID, "").toString()

                    progressDialog.cancel()
                    createPinViewModelClass.getPINResponse(
                        createPINRequest,
                        sharedPreferenceUtil,
                        this
                    )
                    createPinViewModelClass.getData().observeOnce(this@LoginActivity) {
                        val intent =
                            Intent(baseContext, LoginActivity::class.java).putExtra(
                                "heading",
                                "Enter PIN"
                            ).putExtra("TYPE", "Login").setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    progressDialog.cancel()
                    showToast("PINS are not matching, please enter the pin again")
                    startActivity(
                        Intent(this, CreatePinActivity::class.java)
                    )
                    finish()
                }
            } else {
                Log.d("Login", "Device Id: " + ContextUtils.getDeviceId(this))
                userId = binding.pinview.value
                sharedPreferenceUtil.setISLogin(true)

                getLocation(loginViewModel)
                progressDialog.cancel()
            }

        } else {
            progressDialog.cancel()
            showToast("Please enter 4 digit PIN to proceed!")
        }
    }

    public override fun onRestart() {
        super.onRestart()
        if (bundle.getString("TYPE").equals("Login")) {
            finish()
        }
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Method to check permissions and request them
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, show explanation or request permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission is already granted
            if (isGPSDisabled()) {
                Toast.makeText(
                    this,
                    "Please Enable the Location to use the application",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                if (isAudioPermissionNotGranted()) {
                    requestAudioPermissions()
                }
            }
            //  getLocation()
        }
    }

    private fun getLocation(loginViewModel: LoginViewModel) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations, this can be null.
                location?.let {
                    latitude = it.latitude.toString()
                    longitude = it.longitude.toString()
                    val loginRequest = LoginRequest(
                        ContextUtils.getDeviceId(this),
                        userId,
                        sharedPreferenceUtil.getTerminalID(),
                        latitude,
                        longitude,ContextUtils.getVersionName(this)
                    )
                    loginViewModel.getLoginResponse(loginRequest)

                    loginViewModel.getData().observeOnce(this@LoginActivity) {
                        binding.pinview.clearValue()
                    }
                    // Use the location data
                    //    Toast.makeText(this, "Lat: $latitude, Lng: $longitude", Toast.LENGTH_SHORT).show()
                } ?: run {
                    // Handle the case where location is null
                    Log.d("Location", "No last known location available")
                    val loginRequest = LoginRequest(
                        ContextUtils.getDeviceId(this),
                        userId,
                        sharedPreferenceUtil.getTerminalID(),
                        null,
                        null,ContextUtils.getVersionName(this)
                    )
                    loginViewModel.getLoginResponse(loginRequest)

                    loginViewModel.getData().observeOnce(this@LoginActivity) {
                        binding.pinview.clearValue()
                    }
                }
            }.addOnFailureListener {
                Log.e("Location", "Failed to fetch location: ${it.message}")
                val loginRequest = LoginRequest(
                    ContextUtils.getDeviceId(this),
                    userId,
                    sharedPreferenceUtil.getTerminalID(),
                    null,
                    null,ContextUtils.getVersionName(this)
                )
                loginViewModel.getLoginResponse(loginRequest)

                loginViewModel.getData().observeOnce(this@LoginActivity) {
                    binding.pinview.clearValue()
                }
            }
    }


    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission granted, proceed with location access
                    if (isGPSDisabled()) {
                        Toast.makeText(
                            this,
                            "Please Enable the Location to use the application",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        if (isAudioPermissionNotGranted()) {
                            requestAudioPermissions()
                        }
                    }

                } else {
                    // Permission denied, show an explanation
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
            REQUEST_AUDIO_PERMISSION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && permissions.size > 0 && permissions[0] == Manifest.permission.RECORD_AUDIO) {
                    //nothing to do
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Cloud Commerce needs access to your microphone to protect your transactions. You can continue using the app, but not accept contactless payments.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }

            else -> {
                // Ignore all other requests
            }
        }
    }

    private fun isGPSDisabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }


    private fun isAudioPermissionNotGranted(): Boolean {
        val audioPermissionResult = ContextCompat.checkSelfPermission(
            applicationContext, Manifest.permission.RECORD_AUDIO
        )
        return audioPermissionResult != PackageManager.PERMISSION_GRANTED
    }

    private fun requestAudioPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            REQUEST_AUDIO_PERMISSION_CODE
        )
    }
}
