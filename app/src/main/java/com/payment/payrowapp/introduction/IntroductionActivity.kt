package com.payment.payrowapp.introduction


import android.Manifest
import android.Manifest.permission
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.payment.payrowapp.R
import com.payment.payrowapp.dashboard.DashboardActivity
import com.payment.payrowapp.databinding.ActivityEnterTidactivityBinding
import com.payment.payrowapp.databinding.ActivityIntroductionBinding
import com.payment.payrowapp.login.LoginActivity
import com.payment.payrowapp.mastercloud.CPOCConnectActivity
import com.payment.payrowapp.sharepref.SharedPreferenceUtil


class IntroductionActivity : AppCompatActivity() {

    private val LOCATION_PERMISSION_REQUEST_CODE = 102
    val REQUEST_AUDIO_PERMISSION_CODE = 101
    private lateinit var binding: ActivityIntroductionBinding
    var ring: MediaPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      //  setContentView(R.layout.activity_introduction)
        binding = ActivityIntroductionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ring = MediaPlayer.create(this, R.raw.sound_button_click)

        //ReversalDialog(this).show()

        // fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.ivProceed.setOnClickListener {
            ring?.start()
             val sharedPreferenceUtil = SharedPreferenceUtil(this)
             val loginStatus =
                 sharedPreferenceUtil.getISLogin()//preference.getBoolean(Constants.IS_LOGIN, false)
             if (loginStatus) {
                 val lastLoginTime = sharedPreferenceUtil.getLastLogin()
                 val twelveHoursMillis = 12 * 60 * 60 * 1000 // 12 hours in ms
                 if (System.currentTimeMillis() - lastLoginTime > twelveHoursMillis) {
                     val intent =
                         Intent(baseContext, LoginActivity::class.java).putExtra(
                             "heading",
                             "Enter PIN"
                         )
                             .putExtra("TYPE", "Login")
                     startActivity(intent)
                 } else {
                     val intent = Intent(baseContext, DashboardActivity::class.java)
                     startActivity(intent)
                 }
             } else {
                 val intent = Intent(baseContext, EnterTIDActivity::class.java)
                 startActivity(intent)
             }
        }

      //  EncryptionExample.encryption(baseContext)
      //  checkLocationPermission()
        getLocation()
    }

    private fun getLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestBluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            } else {
                requestBluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH)
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestBluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            } else {
                requestBluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH)
            }
        } else {
            // Handle permission denied case
            Toast.makeText(
                this@IntroductionActivity,
                getString(R.string.we_need_location_permission_to_proceed_further),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private val requestBluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if(isGranted) {
            requestAudioPermissions()
        } else {
            Toast.makeText(
                this@IntroductionActivity,
                getString(R.string.bluetooth_permission_denied_error_msg),
                Toast.LENGTH_LONG
            ).show()
        }

    }

    private fun requestAudioPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            REQUEST_AUDIO_PERMISSION_CODE
        )
    }

}