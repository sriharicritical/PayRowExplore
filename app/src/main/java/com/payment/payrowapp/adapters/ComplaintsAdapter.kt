package com.payment.payrowapp.adapters

import android.content.Context
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.payment.payrowapp.R
import com.payment.payrowapp.contactpayrow.RegisteredComplaintsActivity
import com.payment.payrowapp.contactpayrow.RegisteredComplaintsViewModel
import com.payment.payrowapp.dataclass.ComplaintStatus
import com.payment.payrowapp.dataclass.Complaints
import com.payment.payrowapp.dialogs.ComplaintsTypeDialog
import com.payment.payrowapp.utils.ComplaintRemoveListener
import java.util.*

class ComplaintsAdapter(private val ring: MediaPlayer?,
                        context: Context,
                        private var complaintsListResponse: ArrayList<Complaints>,
                        var registeredComplaintsViewModel: RegisteredComplaintsViewModel,
                        var complaintsActivity: RegisteredComplaintsActivity
) : RecyclerView.Adapter<ComplaintsAdapter.MyViewHolder>(), ComplaintRemoveListener {
    private val context = context
    var complaintStatus: String? = null

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvComplaintName: TextView = view.findViewById(R.id.tvComplaintName)
        var tvComplaintDate: TextView = view.findViewById(R.id.tvComplaintDate)
        var tvComplaintDesc: TextView = view.findViewById(R.id.tvComplaintDesc)
        var spStatus: Spinner = view.findViewById(R.id.spStatus)
        var btnResolve: Button = view.findViewById(R.id.btnResolve)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_complaints, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val complaintsListItem = complaintsListResponse[position]
        holder.tvComplaintDate.text = complaintsListItem.complaintDate
        holder.tvComplaintName.text = complaintsListItem.typeOfComplaint
        if (complaintsListItem.briefCompliant!=null) {
            holder.tvComplaintDesc.text = complaintsListItem.briefCompliant
        }

        holder.itemView.setOnClickListener {
            //context.startActivity(Intent(context, MiscellaneousReceipt::class.java))
        }

        holder.btnResolve.setOnClickListener {
          ring?.start()
            val complaintStatus = ComplaintStatus("Close")
            ComplaintsTypeDialog(
                this,
                context,
                registeredComplaintsViewModel,
                complaintsListResponse[position]._id,
                complaintStatus,
                position, complaintsActivity
            ).show()
            /* registeredComplaintsViewModel.updateComplaintStatusResponse(
                 context,
                 complaintsListResponse[position]._id,
                 complaintStatus
             )*/
        }
    }

    override fun getItemCount(): Int {
        return complaintsListResponse.size
    }

    private fun removeAt(position: Int) {
        complaintsListResponse.removeAt(position)
        notifyItemRemoved(position)
        //  notifyItemRangeChanged(position, complaintsListResponse.size)
        notifyDataSetChanged()
    }

    override fun removeListener(position: Int) {
        removeAt(position)
    }
}