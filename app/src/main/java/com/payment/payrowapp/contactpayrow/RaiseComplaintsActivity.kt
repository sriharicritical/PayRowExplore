package com.payment.payrowapp.contactpayrow

import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Bundle
import android.view.WindowManager
import androidx.lifecycle.ViewModelProvider
import com.payment.payrowapp.R
import com.payment.payrowapp.databinding.ActivityProductDetailsBinding
import com.payment.payrowapp.databinding.ActivityRaiseComplaintsBinding
import com.payment.payrowapp.dataclass.ComplaintRequest
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.BaseActivity


class RaiseComplaintsActivity : BaseActivity() {

   private var selectReason: String? = null
private lateinit var binding:ActivityRaiseComplaintsBinding
    var ring: MediaPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRaiseComplaintsBinding.inflate(layoutInflater)
        setContentView(binding.root)
       // setContentView(R.layout.activity_raise_complaints)

       // setSupportActionBar(myToolbar)
        setupToolbar()
        ring = MediaPlayer.create(this, R.raw.sound_button_click)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        getSupportActionBar()?.setDisplayShowHomeEnabled(true)

        getSupportActionBar()?.title = "New Complaints"

        val supportViewModel =
            ViewModelProvider(this, SupportViewModelFactory(this)).get(SupportViewModel::class.java)

        binding.btnPayNotWork.setOnClickListener {
            ring?.start()
            val img: Drawable = baseContext.resources.getDrawable(R.drawable.ic_vector_2x, null)
            selectReason = binding.btnPayNotWork.text.toString()

            binding.btnPayNotWork.setTextColor(resources.getColor(R.color.thick_gray))
            binding.btnAppSlow.setTextColor(resources.getColor(R.color.color_text_80))
            binding.btnNeedHelp.setTextColor(resources.getColor(R.color.color_text_80))
            binding.btnAppCrash.setTextColor(resources.getColor(R.color.color_text_80))

            binding.btnAppSlow.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            binding.btnPayNotWork.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null)
            binding.btnNeedHelp.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            binding.btnAppCrash.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)

            binding.btnPayNotWork.setBackgroundResource(R.drawable.button_round_gray_fill)
            binding.btnAppSlow.setBackgroundResource(R.drawable.button_round_gray_border_thin)
            binding.btnNeedHelp.setBackgroundResource(R.drawable.button_round_gray_border_thin)
            binding.btnAppCrash.setBackgroundResource(R.drawable.button_round_gray_border_thin)
        }

        binding.btnAppSlow.setOnClickListener {
            ring?.start()
            val img: Drawable = baseContext.resources.getDrawable(R.drawable.ic_vector_2x, null)
            selectReason = binding.btnAppSlow.text.toString()
            binding.btnPayNotWork.setTextColor(resources.getColor(R.color.color_text_80))
            binding.btnNeedHelp.setTextColor(resources.getColor(R.color.color_text_80))
            binding.btnAppCrash.setTextColor(resources.getColor(R.color.color_text_80))
            binding.btnAppSlow.setTextColor(resources.getColor(R.color.thick_gray))

            binding.btnAppCrash.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            binding.btnPayNotWork.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            binding.btnNeedHelp.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            binding.btnAppSlow.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null)

            binding.btnAppSlow.setBackgroundResource(R.drawable.button_round_gray_fill)
            binding.btnPayNotWork.setBackgroundResource(R.drawable.button_round_gray_border_thin)
            binding.btnNeedHelp.setBackgroundResource(R.drawable.button_round_gray_border_thin)
            binding.btnAppCrash.setBackgroundResource(R.drawable.button_round_gray_border_thin)
        }

        binding.btnNeedHelp.setOnClickListener {
            ring?.start()
            val img: Drawable = baseContext.resources.getDrawable(R.drawable.ic_vector_2x, null)
            selectReason = binding.btnNeedHelp.text.toString()
            binding.btnAppCrash.setTextColor(resources.getColor(R.color.color_text_80))
            binding.btnNeedHelp.setTextColor(resources.getColor(R.color.thick_gray))
            binding.btnPayNotWork.setTextColor(resources.getColor(R.color.color_text_80))
            binding.btnAppSlow.setTextColor(resources.getColor(R.color.color_text_80))

            binding.btnAppCrash.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            binding.btnNeedHelp.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null)
            binding.btnPayNotWork.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            binding.btnAppSlow.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)

            binding.btnNeedHelp.setBackgroundResource(R.drawable.button_round_gray_fill)
            binding.btnAppCrash.setBackgroundResource(R.drawable.button_round_gray_border_thin)
            binding.btnPayNotWork.setBackgroundResource(R.drawable.button_round_gray_border_thin)
            binding.btnAppSlow.setBackgroundResource(R.drawable.button_round_gray_border_thin)
        }

        binding.btnAppCrash.setOnClickListener {
            ring?.start()
            val img: Drawable = baseContext.resources.getDrawable(R.drawable.ic_vector_2x, null)
            selectReason = binding.btnAppCrash.text.toString()
            binding.btnAppSlow.setTextColor(resources.getColor(R.color.color_text_80))
            binding.btnPayNotWork.setTextColor(resources.getColor(R.color.color_text_80))
            binding.btnAppCrash.setTextColor(resources.getColor(R.color.thick_gray))
            binding.btnNeedHelp.setTextColor(resources.getColor(R.color.color_text_80))

            binding.btnPayNotWork.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            binding.btnAppSlow.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            binding.btnAppCrash.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null)
            binding.btnNeedHelp.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)

            binding.btnAppCrash.setBackgroundResource(R.drawable.button_round_gray_fill)
            binding.btnPayNotWork.setBackgroundResource(R.drawable.button_round_gray_border_thin)
            binding.btnAppSlow.setBackgroundResource(R.drawable.button_round_gray_border_thin)
            binding.btnNeedHelp.setBackgroundResource(R.drawable.button_round_gray_border_thin)
        }

        binding.btnSubmit.setOnClickListener {
            ring?.start()
            if (!selectReason.isNullOrEmpty()) {
                if (!binding.etComplaintDesc.text.toString().isNullOrEmpty()) {
                    val sharedPreferenceUtil = SharedPreferenceUtil(this)
                    val complaintRequest =
                        ComplaintRequest(selectReason, binding.etComplaintDesc.text.toString(), sharedPreferenceUtil.getSaleID(),
                        sharedPreferenceUtil.getMerchantID(),sharedPreferenceUtil.getTerminalID())
                    supportViewModel.postComplaint(complaintRequest)
                    supportViewModel.getData().observe(this) {
                        showToast("Complaint posted successfully!")
                        finish()
                    }
                } else {
                    showToast("Please enter complaint description")
                }
            } else {
                showToast("Please select the reason and proceed")
            }
        }
    }
}
