package com.payment.payrowapp.contactpayrow

import android.media.MediaPlayer
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.payment.payrowapp.R
import com.payment.payrowapp.utils.BaseActivity
import com.payment.payrowapp.adapters.ComplaintsAdapter
import com.payment.payrowapp.dataclass.Complaints
import com.payment.payrowapp.observeOnce
//import io.realm.Realm

class RegisteredComplaintsActivity : BaseActivity() {
    var ring: MediaPlayer? = null
    var complaintsList = ArrayList<Complaints>()
    //  var realm: Realm? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registered_complaints)

        setupToolbar()
        //setSupportActionBar(myToolbar)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        getSupportActionBar()?.setDisplayShowHomeEnabled(true)

        getSupportActionBar()?.title = "Registered Complaints"
        ring = MediaPlayer.create(this, R.raw.sound_button_click)

        val registeredComplaintsViewModel =
            ViewModelProvider(
                this,
                RegisteredComplaintsViewModelFactory(this)
            ).get(RegisteredComplaintsViewModel::class.java)

        val rvComplaintsList: RecyclerView = findViewById(R.id.rvComplaintsList)

        rvComplaintsList.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        registeredComplaintsViewModel.getComplaints(this)
        registeredComplaintsViewModel.getData().observeOnce(this) {
            if (it.complaints.size > 0) {
                val closeItems = it.complaints.filter {
                    it.status == "Open"
                } as ArrayList<Complaints>

                /*val closeItems: ArrayList<Complaints> = ArrayList()
                for (item in it.complaints) {
                    if (item.status == "Open") {
                        closeItems.add(item)
                    }
                }*/
                if (closeItems.size > 0) {
                    showToast("Complaints Loaded Successfully!")

                    val adapter = ComplaintsAdapter(ring,
                        this,
                        closeItems, registeredComplaintsViewModel, this
                    )
                    rvComplaintsList.adapter = adapter
                } else {
                    Toast.makeText(
                        this,
                        "No Open Complaints Available",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    this,
                    "No Complaints Available",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

    }
}