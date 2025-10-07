package com.payment.payrowapp.refundandreversal

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.payment.payrowapp.R
import com.payment.payrowapp.dashboard.DashboardActivity
import com.payment.payrowapp.databinding.ActivityLoginNewBinding
import com.payment.payrowapp.databinding.ActivityRefundConfirmationBinding
import com.payment.payrowapp.dataclass.RefOrderRequest
import com.payment.payrowapp.utils.BaseActivity
import com.payment.payrowapp.utils.ContextUtils
import java.text.SimpleDateFormat
import java.util.*

class RefundConfirmationActivity : BaseActivity() {
    private var orderNumber: String? = null
    private var status: String? = null
    private var recordType: String? = null
    private var refundConfirmationViewModel: RefundConfirmationViewModel? = null
    private var authCode: String? = null
    private var reference48: String? = null
    private var errorTracking: String? = null
    private lateinit var binding: ActivityRefundConfirmationBinding
    var ring: MediaPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
     //   setContentView(R.layout.activity_refund_confirmation)
        binding = ActivityRefundConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        refundConfirmationViewModel =
            ViewModelProvider(
                this,
                RefundConViewModelFactory(this)
            ).get(RefundConfirmationViewModel::class.java)

        ring = MediaPlayer.create(this, R.raw.sound_button_click)

        val bundle = intent.extras
        if (bundle != null) {
            orderNumber = bundle.getString("orderNumber")
            status = bundle.getString("status")
            recordType = bundle.getString("mode")

            val responseCode = bundle.getString("responseCode")
            if (status == "NOT REFUNDED" || status == "NOT VOIDED") {
                val responseMessage = ContextUtils.responseMessage(responseCode!!)
                binding.tvConfirmation.text = "Failed"
                binding.tvYouHaveMadePayment.visibility = View.GONE
                binding.ivPaymentSuccess.setImageResource(R.drawable.failed_icon)
                binding.tvPaymentSuccessful.text = getString(responseMessage ?: R.string.failed)
                errorTracking = recordType + status + responseCode
                updateOrderApi(recordType, bundle.getString("hostRefNO")!!, null)
                /*val refOrderRequest = RefOrderRequest(
                    orderNumber!!, null, null, null, null, null,
                    null, null, null, null, recordType + status + responseCode
                )
                refundConfirmationViewModel!!.addOrder(
                    this@RefundConfirmationActivity,
                    refOrderRequest
                )*/
                //  if (status == "NOT REFUNDED") {
                updateOrderApi(recordType, null, responseCode)
                //  }
            } else if (status == "Cancelled") {
                binding.tvConfirmation.text = "Failed"
                binding.tvYouHaveMadePayment.visibility = View.GONE
                binding.ivPaymentSuccess.setImageResource(R.drawable.failed_icon)
                binding.tvPaymentSuccessful.text = "Host Timeout"
                binding.btnPaymentDetails.visibility = View.GONE
                if (recordType == "Refund") {
                    status = "NOT REFUNDED"
                    updateOrderApi(recordType, null, null)
                } else {
                    status = "NOT VOIDED"
                    updateOrderApi(recordType, null, null)
                }
            } else {
                //  updateOrderApi(bundle.getString("hostRefNO")!!, responseCode!!,bundle.getString("ICCData"))
                if (bundle?.getString("authCode") != null) {
                    authCode = bundle?.getString("authCode")
                }

                if (bundle?.getString("reference48") != null) {
                    reference48 = bundle?.getString("reference48")
                }

                updateOrderApi(recordType, bundle.getString("hostRefNO")!!, responseCode!!)
                binding.tvYouHaveMadePayment.visibility = View.VISIBLE
                if (recordType == "Refund") {
                    binding.tvYouHaveMadePayment.text = getString(R.string.you_have_refund_success)
                } else {
                    binding.tvYouHaveMadePayment.text = getString(R.string.you_made_void_success)
                }
            }
        }

        binding.btnHome.setOnClickListener {
            ring?.start()
            startActivity(
                Intent(
                    this,
                    DashboardActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
            finish()
        }

        binding.btnPaymentDetails.setOnClickListener {
            ring?.start()
            val sendBundle = Bundle()
            if (bundle?.getString("authCode") != null) {
                sendBundle.putString("authCode", bundle.getString("authCode"))
            }
            sendBundle?.putString("CARDNO", bundle?.getString("CARDNO"))
            sendBundle?.putString("INVOICENO", orderNumber)
            sendBundle.putString("hostRefNO", bundle?.getString("hostRefNO"))
            sendBundle.putString("vasRefNO", bundle?.getString("vasRefNO"))
            sendBundle.putString("status", status)
            sendBundle.putString("mode", recordType)
            sendBundle.putString("cardBrand", bundle?.getString("cardBrand"))
            sendBundle.putString("totalAmount", bundle?.getString("totalAmount"))

            startActivity(
                Intent(
                    this@RefundConfirmationActivity,
                    VoidRFReceiptActivity::class.java
                ).putExtras(sendBundle)
            )
            finish()
        }
    }

    private fun updateOrderApi(recordType: String?, hostRefNo: String?, responseCode: String?) {

        val calendar = Calendar.getInstance().time
        val sdf =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val currentDate = sdf.format(calendar)


        val refOrderRequest = RefOrderRequest(
            orderNumber!!, status!!, recordType!!, hostRefNo, responseCode, authCode,
            status!!, null, null, reference48, errorTracking
        )
        refundConfirmationViewModel!!.addOrder(
            this@RefundConfirmationActivity,
            refOrderRequest
        )
    }
}