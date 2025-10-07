package com.payment.payrowapp.generateqrcode

import android.app.ProgressDialog
import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.payment.payrowapp.R
import com.payment.payrowapp.dashboard.DashboardActivity
import com.payment.payrowapp.databinding.ActivityQrcodeConfirmationBinding
import com.payment.payrowapp.databinding.ActivityQrcodeReceipt2Binding
import com.payment.payrowapp.observeOnce
import com.payment.payrowapp.refundandreversal.RefundViewModel
import com.payment.payrowapp.refundandreversal.RefundViewModelFactory


class QRCodeConfirmationActivity : AppCompatActivity() {
    var cardNumber: String? = null
    var cardBrand: String? = null
    var payRowCharge: String? = null
    var other: String? = null
    var bankFee: String? = null
    var totalAmount: String? = null

    private var payRowVATStatus: Boolean? = null
    private var payRowVATAmount: Float? = null
    var auth: String? = null
    var ring: MediaPlayer? = null

    private lateinit var binding: ActivityQrcodeConfirmationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //   setContentView(R.layout.activity_qrcode_confirmation)
        binding = ActivityQrcodeConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ring = MediaPlayer.create(this, R.raw.sound_button_click)

        val bundle = intent.extras
        val orderNumber = bundle?.getString("orderNumber")
        val status = bundle?.getString("status")
        val amount = bundle?.getString("Amount")

        if (bundle?.getString("type") != null) {
            getPaymentStatus(orderNumber)
        }

        if (bundle?.getFloat("payRowVATAmount") != null) {
            payRowVATAmount = bundle.getFloat("payRowVATAmount")
            payRowVATStatus = bundle.getBoolean("payRowVATStatus")
        }

        if (status.equals(
                "REFUNDED",
                ignoreCase = true
            ) || status.equals(
                "VOIDED",
                ignoreCase = true
            ) || status.equals("CAPTURED", ignoreCase = true)
        ) {
            binding.tvConfirmation.text = getString(R.string.confirmation)
            //tvYouHaveMadePayment.visibility = View.GONE
            binding.tvPaymentSuccessful.text = getString(R.string.payment_done)
            binding.tvYouHaveMadePayment.text = getString(R.string.you_have_made_the_payment_successfully)
            binding.ivPaymentSuccess.setImageResource(R.drawable.ic_icon_success)
        } else if (status.equals(
                "NOT VOIDED",
                ignoreCase = true
            ) || status.equals("DENIED BY RISK", ignoreCase = true) || status.equals(
                "HOST TIMEOUT",
                ignoreCase = true
            ) || status.equals("NOT APPROVED", ignoreCase = true) || status.equals(
                "NOT CAPTURED",
                ignoreCase = true
            ) || status.equals("CLOSED", ignoreCase = true) || status.equals(
                "CANCELED",
                ignoreCase = true
            )
        ) {
            binding.tvConfirmation.text = getString(R.string.failed)
            //tvYouHaveMadePayment.visibility = View.GONE
            binding.tvPaymentSuccessful.text = getString(R.string.payment_failed)
            binding.tvYouHaveMadePayment.text = "Customer Payment Transaction is Declined"
            binding.ivPaymentSuccess.setImageResource(R.drawable.ic_frame_failure)
        } else if (status.equals("Pending", ignoreCase = true)) {
            binding.tvConfirmation.text = getString(R.string.failed)
            //   tvYouHaveMadePayment.visibility = View.GONE
            binding.tvPaymentSuccessful.text = getString(R.string.payment_failed)
            binding.tvYouHaveMadePayment.text = "Customer Payment Transaction is Declined"
            binding.ivPaymentSuccess.setImageResource(R.drawable.ic_frame_failure)
        } else {
            binding.tvConfirmation.text = getString(R.string.failed)
            //   tvYouHaveMadePayment.visibility = View.GONE
            binding.tvPaymentSuccessful.text = getString(R.string.payment_failed)
            binding.tvYouHaveMadePayment.text = "Customer Payment Transaction is Declined"
            binding.ivPaymentSuccess.setImageResource(R.drawable.ic_frame_failure)
        }

        binding.btnPay.setOnClickListener {
            ring?.start()
            startActivity(
                Intent(this, DashboardActivity::class.java).addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                )
            )
        }

        binding.btnPaymentDetails.setOnClickListener {
            ring?.start()
            val sendBundle = Bundle()

            sendBundle.putString("Amount", amount)
            sendBundle.putString("orderNumber", orderNumber)
            sendBundle.putString("status", status)

            if (bundle?.getString("auth") != null) {
                sendBundle.putString("auth", bundle?.getString("auth"))
            } else {
                if (auth != null) {
                    sendBundle.putString("auth", auth)
                }
            }

            if (bundle?.getString("channel") != null) {
                sendBundle.putString("channel", bundle.getString("channel"))
            }
            payRowVATStatus?.let { it1 -> sendBundle.putBoolean("payRowVATStatus", it1) }
            payRowVATAmount?.let { it1 -> sendBundle.putFloat("payRowVATAmount", it1) }

            if (bundle?.getString("type") != null) {
                if (cardNumber != null) {
                    sendBundle.putString("cardNumber", cardNumber)
                    sendBundle.putString("cardBrand", cardBrand)
                }
                /* sendBundle.putString("PayRowCharge", payRowCharge)
                 sendBundle.putString("other", other)
                 sendBundle.putString("bankFee", bankFee)*/
                sendBundle.putString("totalAmount", totalAmount)
            } else {
                if (bundle?.getString("cardNumber") != null) {
                    sendBundle.putString("cardNumber", bundle?.getString("cardNumber"))
                    sendBundle.putString("cardBrand", bundle?.getString("cardBrand"))
                }
                //    sendBundle.putString("PayRowCharge", bundle?.getString("PayRowCharge"))
                //    sendBundle.putString("other", bundle?.getString("other"))
                //    sendBundle.putString("bankFee", bundle?.getString("bankFee"))
                sendBundle.putString("totalAmount", bundle?.getString("totalAmount"))
            }

            if (bundle?.getString("mode") != null) {
                sendBundle.putString("mode", bundle?.getString("mode"))
                startActivity(Intent(this, ECommVOIDRFActivity::class.java).putExtras(sendBundle))
                finish()
            } else {
                startActivity(
                    Intent(this, GenerateQRCodeReceiptActivity::class.java).putExtras(
                        sendBundle
                    )
                )
                finish()
            }
        }
    }

    private fun getPaymentStatus(orderNumber: String?) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please wait...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        val refundViewModel =
            ViewModelProvider(
                this,
                RefundViewModelFactory(this)
            ).get(RefundViewModel::class.java)
        refundViewModel.getQrCodeInvoice(orderNumber!!)
        refundViewModel.getQrCodeInvoiceLiveData()
            .observeOnce(this@QRCodeConfirmationActivity) {
                progressDialog.dismiss()
                if (it.data.get(0).purchaseBreakdown.service.size > 0) {
                    cardNumber = it.data.get(0).cardNumber
                    cardBrand = it.data.get(0).cardBrand
                    totalAmount = it.data.get(0).amount
                    payRowVATStatus = it.data.get(0).vatStatus
                    payRowVATAmount = it.data.get(0).vatAmount
                    auth = it.data.get(0).auth
                }
                refundViewModel.reSetPurchaseResp()
            }
    }
}