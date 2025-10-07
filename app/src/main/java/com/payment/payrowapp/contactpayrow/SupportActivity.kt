package com.payment.payrowapp.contactpayrow

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import com.payment.payrowapp.R
import com.payment.payrowapp.databinding.ActivitySoftwareProductsBinding
import com.payment.payrowapp.databinding.ActivitySupportBinding
import com.payment.payrowapp.utils.BaseActivity
import com.payment.payrowapp.dataclass.Complaints
import com.payment.payrowapp.observeOnce
import com.payment.payrowapp.sharepref.SharedPreferenceUtil
import com.payment.payrowapp.utils.ContextUtils

class SupportActivity : BaseActivity(), AdapterView.OnItemSelectedListener, IDialog {
    lateinit var supportViewModel: SupportViewModel
    var ring: MediaPlayer? = null
    var complaintStatus: String? = null
    var selectedPosition = 0

    private lateinit var binding: ActivitySupportBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_support)
        binding = ActivitySupportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //   setSupportActionBar(myToolbar)
        setupToolbar()

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        getSupportActionBar()?.setDisplayShowHomeEnabled(true)

        getSupportActionBar()?.title = "Support"

        ring = MediaPlayer.create(this, R.raw.sound_button_click)

        val sharedPreferenceUtil = SharedPreferenceUtil(this)

        binding.tvAccountManager2.text = "MID : " + sharedPreferenceUtil.getMerchantID()
        binding.tvTelephoneNum2.text = " " + sharedPreferenceUtil.getTerminalID()
        binding.tvFaxMail2.text = " " + ContextUtils.getVersionName(this)
        binding.tvTelephoneNum3.text = " +" + sharedPreferenceUtil.getMerchantMobileNumber()

//        spinnerStatus.onItemSelectedListener = this
        // spinnerNewComplaints!!.setOnItemSelectedListener(this)

        supportViewModel =
            ViewModelProvider(this, SupportViewModelFactory(this)).get(SupportViewModel::class.java)

        val newComplaintsArray = resources.getStringArray(R.array.complaints)
        // Create an ArrayAdapter using a simple spinner layout and languages array
        val newComplaintsAdapter = ArrayAdapter(
            this, R.layout.my_spinner_item,
            newComplaintsArray
        )
        // Set layout to use when the list of choices appear
        newComplaintsAdapter.setDropDownViewResource(R.layout.my_spinner_dropdown_item)
        // Set Adapter to Spinner
        // spinnerNewComplaints!!.setAdapter(newComplaintsAdapter)

        binding.btnRegisteredCom.setOnClickListener {
            ring?.start()
            startActivity(Intent(this, RegisteredComplaintsActivity::class.java))
        }


        binding.btnNewCom.setOnClickListener {
            ring?.start()
            startActivity(Intent(this, RaiseComplaintsActivity::class.java))
        }

        supportViewModel.getComplaintsCount(this)
        supportViewModel.getComplaintCountsData().observeOnce(this) {
            if (it?.data != null && it.data.count > 0) {
                binding.countTV.text = it.data.count.toString()
            }
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        selectedPosition = p0!!.selectedItemPosition
        complaintStatus = p0.getItemAtPosition(p2).toString()
    }

    override fun setText(selectedComplaintType: String) {
        //  binding.tvComplaintSelected.text = selectedComplaintType
    }

    override fun onRestart() {
        super.onRestart()

        supportViewModel.getComplaintsCount(this)
        supportViewModel.getComplaintCountsData().observeOnce(this) {
            if (it?.data != null && it.data.count > 0) {
                binding.countTV.text = it.data.count.toString()
            }
        }
        /*supportViewModel.getComplaints(this)
        supportViewModel.getComplaintData().observeOnce(this) {
            if (it.complaints.size > 0) {
                val closeItems = it.complaints.filter {
                    it.status == "Open"
                } as ArrayList<Complaints>
                if (closeItems.size > 0) {
                    binding.countTV.text = closeItems.size.toString()
                }
            }
        }*/
    }
}