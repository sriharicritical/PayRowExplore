package com.payment.payrowapp.utils

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.payment.payrowapp.R
import com.payment.payrowapp.introduction.IntroductionActivity
import java.util.*

open class BaseActivity : AppCompatActivity() {

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    lateinit var progressDialog: Dialog
    private val logoutTime = (10 * 60 * 1000).toLong()

    override fun attachBaseContext(newBase: Context) {
// get chosen language from shared preferences
        val preference =
            newBase.getSharedPreferences(Constants.MY_PREFERENCES, Context.MODE_PRIVATE)

        val name = preference.getString("language", "en")
        val localeToSwitchTo = Locale(name)
        val localeUpdatedContext: ContextWrapper =
            ContextUtils.updateLocale(newBase, localeToSwitchTo)

        super.attachBaseContext(localeUpdatedContext)
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
//    fun startScreenActivity(activityName: Class<T>) {
//        startActivity(Intent(this@BaseActivity,activityName))
//    }

    fun showAnimatedProgressDialog() {
        progressDialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        progressDialog.setCancelable(true)
        progressDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        progressDialog.setContentView(R.layout.preview_loader_layout)
        progressDialog.show()
    }

    fun closeProgressDialog() {
        if (progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // resetDisconnectTimer()

        startUserSession()
    }

    override fun onUserInteraction() {
        //  resetDisconnectTimer()
        startUserSession()
    }

    private fun startUserSession() {
        cancelTimer()
        timer = object : CountDownTimer(logoutTime, 5000) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                showToast(getString(R.string.session_timeout))
                val intent = Intent(
                    this@BaseActivity,
                    IntroductionActivity::class.java
                )
                intent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TASK
                        or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
        }
        timer?.start()
    }

    private fun cancelTimer() {
        if (timer != null)
            timer?.cancel()
    }

    // Function to show loading dialog
    fun showLoadingAlerDialog(context: Context): AlertDialog {
        // Create a ProgressBar programmatically
        val progressBar = ProgressBar(context)

        // Use AlertDialog.Builder to create a dialog
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Please wait...")
        builder.setView(progressBar)  // Set the ProgressBar inside the dialog
        builder.setCancelable(false)  // Prevent the user from dismissing the dialog

        // Create and show the dialog
        val dialog = builder.create()
        dialog.show()

        return dialog
    }

    // Function to dismiss the loading dialog
    fun dismissLoadingAlertDialog(dialog: AlertDialog) {
        dialog.dismiss()
    }

    companion object {
        private var timer: CountDownTimer? = null
    }

    protected fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.myToolbar)
        setSupportActionBar(toolbar)
    }
}