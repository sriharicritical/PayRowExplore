package com.payment.payrowapp.selectingcategoryandlanguage

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.payment.payrowapp.R
import com.payment.payrowapp.dashboard.DashboardActivity
import com.payment.payrowapp.login.AuthenticationActivity
import com.payment.payrowapp.utils.BaseActivity
import com.payment.payrowapp.utils.Constants


class ChooseLanguage : BaseActivity(), View.OnClickListener {
    private var bundle: Bundle? = null
    private var selectedLanguage: String = ""
    var ring: MediaPlayer? = null
    private lateinit var ivProceed: Button
    private lateinit var btnArabic: Button
    private lateinit var btnEnglish: Button
    private lateinit var btnHindi: Button

    private lateinit var btnBangladeshi: Button
    private lateinit var btnUrdu: Button
    private lateinit var btnMalayalam: Button
    private lateinit var btnChinese: Button
    private lateinit var btnFrench: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_language)
        ivProceed = findViewById(R.id.ivProceed)
        btnArabic = findViewById(R.id.btnArabic)
        btnEnglish = findViewById(R.id.btnEnglish)
        btnHindi = findViewById(R.id.btnHindi)
        btnBangladeshi = findViewById(R.id.btnBangladeshi)
        btnUrdu = findViewById(R.id.btnUrdu)
        btnMalayalam = findViewById(R.id.btnMalayalam)
        btnChinese = findViewById(R.id.btnChinese)
        btnFrench = findViewById(R.id.btnFrench)

        ring = MediaPlayer.create(this, R.raw.sound_button_click)


        ivProceed.setOnClickListener(this)
        btnArabic.setOnClickListener(this)
        btnEnglish.setOnClickListener(this)
        btnHindi.setOnClickListener(this)
        btnBangladeshi.setOnClickListener(this)
        btnUrdu.setOnClickListener(this)
        btnMalayalam.setOnClickListener(this)
        btnChinese.setOnClickListener(this)
        btnFrench.setOnClickListener(this)

        bundle = intent.extras


    }

    override fun onClick(v: View?) {
        val id: Int = v!!.id
        val preference = this.getSharedPreferences(Constants.MY_PREFERENCES, Context.MODE_PRIVATE)
        val editor = preference.edit()
        val img: Drawable = baseContext.resources.getDrawable(R.drawable.ic_vector_2x, null)
        when (id) {
            btnArabic.id -> {
                ring?.start()
                selectedLanguage = "Arabic"
                editor.putString("language", "ar")
                editor.apply()
                btnArabic.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null)
                btnEnglish.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnBangladeshi.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnHindi.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnUrdu.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnMalayalam.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnChinese.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnFrench.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)

                btnArabic.setBackgroundResource(R.drawable.button_round_gray_fill)
                btnEnglish.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnBangladeshi.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnHindi.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnUrdu.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnMalayalam.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnChinese.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnFrench.setBackgroundResource(R.drawable.button_round_gray_border_thin)
            }
            btnEnglish.id -> {
                ring?.start()
                selectedLanguage = "English"
                editor.putString("language", "en")
                editor.apply()
                btnArabic.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnEnglish.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null)
                btnBangladeshi.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnHindi.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnUrdu.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnMalayalam.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnChinese.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnFrench.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)

                btnEnglish.setBackgroundResource(R.drawable.button_round_gray_fill)
                btnArabic.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnBangladeshi.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnHindi.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnUrdu.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnMalayalam.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnChinese.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnFrench.setBackgroundResource(R.drawable.button_round_gray_border_thin)
            }
            btnFrench.id -> {
                ring?.start()
                selectedLanguage = "French"
                editor.putString("language", "fr")
                editor.apply()
                btnArabic.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnEnglish.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnBangladeshi.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnHindi.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnUrdu.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnMalayalam.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnChinese.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnFrench.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null)

                btnFrench.setBackgroundResource(R.drawable.button_round_gray_fill)
                btnArabic.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnBangladeshi.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnHindi.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnUrdu.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnMalayalam.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnChinese.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnEnglish.setBackgroundResource(R.drawable.button_round_gray_border_thin)
            }
            btnBangladeshi.id -> {
                ring?.start()
                selectedLanguage = "Bangladeshi"
                editor.putString("language", "bn")
                editor.apply()
                btnArabic.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnEnglish.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnBangladeshi.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null)
                btnHindi.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnUrdu.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnMalayalam.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnChinese.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnFrench.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)

                btnBangladeshi.setBackgroundResource(R.drawable.button_round_gray_fill)
                btnEnglish.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnArabic.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnHindi.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnUrdu.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnMalayalam.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnChinese.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnFrench.setBackgroundResource(R.drawable.button_round_gray_border_thin)
            }
            btnHindi.id -> {
                ring?.start()
                selectedLanguage = "Hindi"
                editor.putString("language", "hi")
                editor.apply()
                btnArabic.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnEnglish.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnBangladeshi.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnHindi.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null)
                btnUrdu.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnMalayalam.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnChinese.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnFrench.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)

                btnHindi.setBackgroundResource(R.drawable.button_round_gray_fill)
                btnEnglish.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnBangladeshi.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnArabic.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnUrdu.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnMalayalam.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnChinese.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnFrench.setBackgroundResource(R.drawable.button_round_gray_border_thin)
            }
            btnUrdu.id -> {
                ring?.start()
                selectedLanguage = "Urdu"
                editor.putString("language", "ur")
                editor.apply()
                btnArabic.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnEnglish.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnBangladeshi.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnHindi.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnUrdu.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null)
                btnMalayalam.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnChinese.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnFrench.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)

                btnUrdu.setBackgroundResource(R.drawable.button_round_gray_fill)
                btnEnglish.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnBangladeshi.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnHindi.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnArabic.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnMalayalam.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnChinese.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnFrench.setBackgroundResource(R.drawable.button_round_gray_border_thin)
            }
            btnMalayalam.id -> {
                ring?.start()
                selectedLanguage = "English"
                editor.putString("language", "en")
                editor.apply()
                btnArabic.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnEnglish.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnBangladeshi.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnHindi.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnUrdu.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnMalayalam.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null)
                btnChinese.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnFrench.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)

                btnMalayalam.setBackgroundResource(R.drawable.button_round_gray_fill)
                btnEnglish.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnBangladeshi.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnHindi.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnUrdu.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnArabic.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnChinese.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnFrench.setBackgroundResource(R.drawable.button_round_gray_border_thin)
            }

            btnChinese.id -> {
                ring?.start()
                selectedLanguage = "Chinese"
                editor.putString("language", "zh-CN")
                editor.apply()
                btnArabic.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnEnglish.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnBangladeshi.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnHindi.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnUrdu.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnMalayalam.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                btnChinese.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null)
                btnFrench.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)

                btnChinese.setBackgroundResource(R.drawable.button_round_gray_fill)
                btnEnglish.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnBangladeshi.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnHindi.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnUrdu.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnArabic.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnMalayalam.setBackgroundResource(R.drawable.button_round_gray_border_thin)
                btnFrench.setBackgroundResource(R.drawable.button_round_gray_border_thin)
            }
            ivProceed.id -> {
                ring?.start()
                if (selectedLanguage.length > 0) {
                    if (bundle?.getString("TYPE").equals("DASHBOARD")) {
                        Toast.makeText(this, "Language changed successfully", Toast.LENGTH_SHORT)
                            .show()
                        startActivity(
                            Intent(
                                this,
                                DashboardActivity::class.java
                            ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        )
                    } else {
                        startActivity(Intent(this, AuthenticationActivity::class.java))
                    }
                    finish()
                } else {
                    Toast.makeText(this, "Please select a language to proceed", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
}
