package com.payment.payrowapp.refundandreversal

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.payment.payrowapp.R
import com.payment.payrowapp.contactpayrow.IDialog
import com.payment.payrowapp.databinding.ActivityLoginNewBinding
import com.payment.payrowapp.databinding.ActivityRefundBinding
import com.payment.payrowapp.observeOnce
import com.payment.payrowapp.otp.EnterOTPActivity
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.BaseActivity
import com.payment.payrowapp.utils.Constants
import com.payment.payrowapp.qrcodescan.OrderQRScanActivity

class RefundActivity : BaseActivity(), IDialog {
    var selectReason: String? = null
    var mode: String? = null
    private lateinit var sharedPreferenceUtil: SharedPreferenceUtil
    private lateinit var binding:ActivityRefundBinding
    var ring: MediaPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_refund)
        binding = ActivityRefundBinding.inflate(layoutInflater)
        setContentView(binding.root)

      //  setSupportActionBar(myToolbar)
        setupToolbar()

        ring = MediaPlayer.create(this, R.raw.sound_button_click)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        sharedPreferenceUtil = SharedPreferenceUtil(this)

        val bundle = intent.extras
        if (bundle != null) {
            supportActionBar?.title = bundle.getString("mode")
            mode = bundle.getString("mode")
        }

        val refundViewModel =
            ViewModelProvider(
                this,
                RefundViewModelFactory(this)
            ).get(RefundViewModel::class.java)

        binding.btnSelectReason.setOnClickListener {
        }

        binding.qrOrderImg.setOnClickListener {
          ring?.start()
            val intent = Intent(this, OrderQRScanActivity::class.java)
            launchQROrderScan.launch(intent)
        }

        binding.btnSubmit.setOnClickListener {
            ring?.start()
            val progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Please wait...")
            progressDialog.show()
            if (!binding.etTransactionNo.text.toString().isNullOrEmpty()) {
                if (!selectReason.isNullOrEmpty()) {
                    progressDialog.dismiss()
                    refundViewModel.getQrCodeInvoice(binding.etTransactionNo.text.toString().trim())
                    refundViewModel.getQrCodeInvoiceLiveData()
                        .observeOnce(this@RefundActivity) {
                            val intent = Intent(this, EnterOTPActivity::class.java)
                            // if (it.data.get(0).purchaseBreakdown.service.size > 0) {
                            if (it?.data != null) {
                                if (it.data.get(0).responseCode.isNotEmpty() && (it.data.get(
                                        0
                                    ).checkoutStatus == "CAPTURED" && it.data.get(0).refundStatus) || (it.data.get(0).checkoutStatus == "VOIDED" ||
                                            it.data.get(0).checkoutStatus == "REFUNDED")
                                ) {
                                    if (it.data.get(0).recordType == "Paybylink" || it.data.get(0).recordType == "generateQR" || it.data.get(
                                            0
                                        ).recordType == "Purchase" || it.data.get(0).recordType == "Refund Order" || it.data.get(
                                            0
                                        ).recordType == "Refund"
                                    ) {
                                        progressDialog.dismiss()
                                        showToast(getString(R.string.this_refund_already_processed))
                                    } else if (it.data.get(0).recordType == "Voided" || it.data.get(
                                            0
                                        ).recordType == "Void"
                                    ) {
                                        progressDialog.dismiss()
                                        showToast(getString(R.string.void_process_already_completed))
                                    }
                                } else if (it.data.get(0).responseCode.isNotEmpty() && (it.data.get(
                                        0
                                    ).recordType != "Refund Order" && it.data.get(0).checkoutStatus == "CAPTURED" || it.data.get(
                                        0
                                    ).checkoutStatus == "NOT VOIDED")
                                    || it.data.get(0).checkoutStatus == "NOT REFUNDED" || it.data.get(
                                        0
                                    ).checkoutStatus == "PARTIAL APPROVED" ||
                                    it.data.get(0).checkoutStatus == "Terminal Declined" || it.data.get(
                                        0
                                    ).checkoutStatus == "NOT REVERSAL" || (it.data.get(0).checkoutStatus == "NOT CAPTURED" && it.data.get(
                                        0
                                    ).recordType == "Refund Order")
                                ) {

                                    if (it.data.get(0).channel == Constants.CARD) {
                                        intent.putExtra(
                                            "purchaseNumber",
                                            it.data.get(0).purchaseNumber
                                        )

                                        if (it.data[0].PartialApprovedAmount != null) {
                                            intent.putExtra(
                                                "totalAmount",
                                                it.data.get(0).PartialApprovedAmount
                                            )
                                        } else {
                                            intent.putExtra(
                                                "totalAmount",
                                                it.data.get(0).amount
                                            )
                                        }


                                        intent.putExtra(
                                            "OrderNumber",
                                            it.data.get(0).orderNumber
                                        )

                                        intent.putExtra(
                                            "cardNumber",
                                            it.data.get(0).cardNumber
                                        )

                                        intent.putExtra(
                                            "refundId",
                                            it.data.get(0).refundId
                                        )


                                        intent.putExtra(
                                            "refId7",
                                            it.data.get(0).refId7
                                        )

                                        intent.putExtra(
                                            "refId11",
                                            it.data.get(0).refId11
                                        )

                                        intent.putExtra(
                                            "refId12",
                                            it.data.get(0).refId12
                                        )

                                        intent.putExtra(
                                            "refId13",
                                            it.data.get(0).refId13
                                        )

                                        intent.putExtra(
                                            "refId32",
                                            it.data.get(0).refId32
                                        )

                                        intent.putExtra(
                                            "refId33",
                                            it.data.get(0).refId33
                                        )

                                        intent.putExtra(
                                            "refId37",
                                            it.data.get(0).refId37
                                        )

                                        intent.putExtra(
                                            "responseCode",
                                            it.data.get(0).responseCode
                                        )

                                        intent.putExtra(
                                            "authorizationId",
                                            it.data.get(0).authorizationId
                                        )

                                        intent.putExtra(
                                            "base64",
                                            it.data.get(0).base64
                                        )

                                        intent.putExtra(
                                            "hostRefNO",
                                            it.data.get(0).hostReference
                                        )

                                        intent.putExtra(
                                            "vasRefNO",
                                            it.data.get(0).vasReference
                                        )

                                        intent.putExtra(
                                            "purchaseAmount",
                                            it.data.get(0).purchaseAmount
                                        )

                                        intent.putExtra(
                                            "cardType",
                                            it.data.get(0).cardType
                                        )

                                        intent.putExtra(
                                            "posEntryMode",
                                            it.data.get(0).posEntryMode
                                        )

                                        intent.putExtra(
                                            "track2Data",
                                            it.data.get(0).track2Data
                                        )

                                        intent.putExtra(
                                            "ICCData",
                                            it.data.get(0).ICCData
                                        )

                                        intent.putExtra(
                                            "TRANSACTION_TYPE",
                                            it.data.get(0).TRANSACTION_TYPE
                                        )
                                        intent.putExtra(
                                            "cardsequencenumber",
                                            it.data.get(0).cardsequencenumber
                                        )
                                        intent.putExtra("expiryDate", it.data.get(0).cardExpiryDate)
                                        intent.putExtra("mode", mode)
                                        intent.putExtra("channel", it.data.get(0).channel)
                                        intent.putExtra(
                                            "referenceId48",
                                            it.data.get(0).referenceId48
                                        )
                                        intent.putExtra(
                                            "pmtTxnRefCode",
                                            it.data.get(0).pmtTxnRefCode
                                        )
                                        startActivity(intent)
                                        finish()
                                    } else if (it.data.get(0).channel == "Paybylink" || it.data.get(
                                            0
                                        ).channel == "generateQR"
                                    ) {

                                        intent.putExtra("channel", it.data.get(0).channel)
                                        intent.putExtra("OrderNumber", it.data.get(0).orderNumber)
                                        intent.putExtra("mode", mode)
                                        startActivity(intent)
                                        finish()
                                    }
                                } else {
                                    progressDialog.dismiss()
                                    showToast("Your Unable to do this transaction")
                                }
                            } else {
                                showToast(getString(R.string.data_not_available))
                            }
                        }
                } else {
                    progressDialog.dismiss()
                    showToast("Please Select the Reason")
                }
            } else {
                progressDialog.dismiss()
                showToast("Please enter Transaction Number")
            }
        }

        binding.btnWrongPro.setOnClickListener {
            ring?.start()
            val img: Drawable = baseContext.resources.getDrawable(R.drawable.ic_vector_2x, null)
            selectReason = binding.btnWrongPro.text.toString()

            binding.btnWrongPro.setTextColor(resources.getColor(R.color.thick_gray))
            binding.btnProDamage.setTextColor(resources.getColor(R.color.color_text_80))
            binding.btnProNotNeed.setTextColor(resources.getColor(R.color.color_text_80))
            binding.btnExpireProduct.setTextColor(resources.getColor(R.color.color_text_80))

            binding.btnProDamage.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            binding.btnWrongPro.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null)
            binding.btnProNotNeed.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            binding.btnExpireProduct.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)

            binding.btnWrongPro.setBackgroundResource(R.drawable.button_round_gray_fill)
            binding.btnProDamage.setBackgroundResource(R.drawable.button_round_gray_border_thin)
            binding.btnProNotNeed.setBackgroundResource(R.drawable.button_round_gray_border_thin)
            binding.btnExpireProduct.setBackgroundResource(R.drawable.button_round_gray_border_thin)
        }

        binding.btnExpireProduct.setOnClickListener {
            ring?.start()
            val img: Drawable = baseContext.resources.getDrawable(R.drawable.ic_vector_2x, null)
            selectReason = binding.btnExpireProduct.text.toString()
            binding.btnWrongPro.setTextColor(resources.getColor(R.color.color_text_80))
            binding.btnProDamage.setTextColor(resources.getColor(R.color.color_text_80))
            binding.btnProNotNeed.setTextColor(resources.getColor(R.color.color_text_80))
            binding.btnExpireProduct.setTextColor(resources.getColor(R.color.thick_gray))

            binding.btnProDamage.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            binding.btnWrongPro.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            binding.btnProNotNeed.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            binding.btnExpireProduct.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null)

            binding.btnExpireProduct.setBackgroundResource(R.drawable.button_round_gray_fill)
            binding.btnProDamage.setBackgroundResource(R.drawable.button_round_gray_border_thin)
            binding.btnProNotNeed.setBackgroundResource(R.drawable.button_round_gray_border_thin)
            binding.btnWrongPro.setBackgroundResource(R.drawable.button_round_gray_border_thin)
        }

        binding.btnProDamage.setOnClickListener {
            ring?.start()
            val img: Drawable = baseContext.resources.getDrawable(R.drawable.ic_vector_2x, null)
            selectReason = binding.btnProDamage.text.toString()
            binding.btnWrongPro.setTextColor(resources.getColor(R.color.color_text_80))
            binding.btnProDamage.setTextColor(resources.getColor(R.color.thick_gray))
            binding.btnProNotNeed.setTextColor(resources.getColor(R.color.color_text_80))
            binding.btnExpireProduct.setTextColor(resources.getColor(R.color.color_text_80))

            binding.btnWrongPro.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            binding.btnProDamage.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null)
            binding.btnProNotNeed.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            binding.btnExpireProduct.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)

            binding.btnProDamage.setBackgroundResource(R.drawable.button_round_gray_fill)
            binding.btnWrongPro.setBackgroundResource(R.drawable.button_round_gray_border_thin)
            binding.btnProNotNeed.setBackgroundResource(R.drawable.button_round_gray_border_thin)
            binding.btnExpireProduct.setBackgroundResource(R.drawable.button_round_gray_border_thin)
        }

        binding.btnProNotNeed.setOnClickListener {
            ring?.start()
            val img: Drawable = baseContext.resources.getDrawable(R.drawable.ic_vector_2x, null)
            selectReason = binding.btnProNotNeed.text.toString()
            binding.btnWrongPro.setTextColor(resources.getColor(R.color.color_text_80))
            binding.btnProDamage.setTextColor(resources.getColor(R.color.color_text_80))
            binding.btnProNotNeed.setTextColor(resources.getColor(R.color.thick_gray))
            binding.btnExpireProduct.setTextColor(resources.getColor(R.color.color_text_80))

            binding.btnProDamage.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            binding.btnWrongPro.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            binding.btnProNotNeed.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null)
            binding.btnExpireProduct.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)

            binding.btnProNotNeed.setBackgroundResource(R.drawable.button_round_gray_fill)
            binding.btnProDamage.setBackgroundResource(R.drawable.button_round_gray_border_thin)
            binding.btnProNotNeed.setBackgroundResource(R.drawable.button_round_gray_border_thin)
            binding.btnWrongPro.setBackgroundResource(R.drawable.button_round_gray_border_thin)
        }

    }

    override fun setText(complaintType: String) {
        binding.btnSelectReason.text = complaintType
    }

    private val launchQROrderScan = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            binding.etTransactionNo.setText(result.data?.getStringExtra("orderNumber"))
        }
    }
}
