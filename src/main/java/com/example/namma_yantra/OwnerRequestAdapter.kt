package com.example.namma_yantra

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OwnerRequestAdapter(
    private val requests: List<Booking>,
    private val onStatusChange: (Booking, String) -> Unit
) : RecyclerView.Adapter<OwnerRequestAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val machineName: TextView = itemView.findViewById(R.id.requestMachineName)
        val requestMeta: TextView = itemView.findViewById(R.id.requestMeta)
        val renterText: TextView = itemView.findViewById(R.id.requestRenter)
        val amountText: TextView = itemView.findViewById(R.id.requestAmount)
        val statusText: TextView = itemView.findViewById(R.id.requestStatus)
        val acceptBtn: Button = itemView.findViewById(R.id.acceptBtn)
        val declineBtn: Button = itemView.findViewById(R.id.declineBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_owner_request, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = requests.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = requests[position]
        val isPending = request.status == "Pending"

        holder.machineName.text = request.machineName
        holder.requestMeta.text = "${request.date} at ${request.time} | ${request.durationHours} hours"
        holder.renterText.text = "Renter: ${request.renterEmail.ifBlank { "App user" }}\nDelivery: ${request.deliveryAddress.ifBlank { "Not provided" }}"
        holder.amountText.text = "Rs ${request.totalPrice}"
        holder.statusText.text = request.status

        holder.statusText.setTextColor(
            when (request.status) {
                "Accepted" -> Color.parseColor("#1B5E20")
                "Declined" -> Color.parseColor("#C62828")
                else -> Color.parseColor("#A66A00")
            }
        )

        holder.acceptBtn.isEnabled = isPending
        holder.declineBtn.isEnabled = isPending
        holder.acceptBtn.alpha = if (isPending) 1f else 0.45f
        holder.declineBtn.alpha = if (isPending) 1f else 0.45f

        holder.acceptBtn.setOnClickListener { onStatusChange(request, "Accepted") }
        holder.declineBtn.setOnClickListener { onStatusChange(request, "Declined") }
    }
}
