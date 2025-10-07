package com.payment.payrowapp.selectingcategoryandlanguage

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.payment.payrowapp.R
import com.payment.payrowapp.introduction.IntroductionActivity
import com.payment.payrowapp.introduction.IntroductionFactory
import com.payment.payrowapp.introduction.IntroductionViewModel
import com.payment.payrowapp.observeOnce
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.ContextUtils
import com.scottyab.rootbeer.RootBeer


class SplashActivity : AppCompatActivity() {
    // var sharedPreferenceUtil: SharedPreferenceUtil? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_landing_page)

        val sharedPreferenceUtil = SharedPreferenceUtil(this)

        val introductionViewModel =
            ViewModelProvider(
                this,
                IntroductionFactory(this)
            ).get(IntroductionViewModel::class.java)

        Log.v("onTime", ContextUtils.getCurrentTime())
        val rootBeer = RootBeer(this)
        if (rootBeer.isRooted) {
            Toast.makeText(this, "The device is Rooted", Toast.LENGTH_LONG).show()
            // showToast("The device is Rooted")
            finish()
            //we found indication of root
        } else {
            //we didn't find indication of root
            Handler(Looper.getMainLooper()).postDelayed({
                if (sharedPreferenceUtil.getISLogin()) {
                    introductionViewModel.checkIMEIStatusResponse()
                    introductionViewModel.getData().observeOnce(this@SplashActivity) {
                        startActivity(Intent(this, IntroductionActivity::class.java))
                        finish()
                    }
                } else {

                    startActivity(Intent(this, IntroductionActivity::class.java))
                    finish()
                    //ring.stop()
                }
            }, 3000)
        }
    }
}