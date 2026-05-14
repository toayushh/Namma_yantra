package com.example.namma_yantra

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BookingHistoryAdapter(
    private val bookings: List<Booking>
) : RecyclerView.Adapter<BookingHistoryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val machineName: TextView = itemView.findViewById(R.id.historyMachineName)
        val scheduleText: TextView = itemView.findViewById(R.id.historySchedule)
        val deliveryText: TextView = itemView.findViewById(R.id.historyDelivery)
        val ownerText: TextView = itemView.findViewById(R.id.historyOwner)
        val amountText: TextView = itemView.findViewById(R.id.historyAmount)
        val statusText: TextView = itemView.findViewById(R.id.historyStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_booking_history, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = bookings.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val booking = bookings[position]
        holder.machineName.text = booking.machineName
        holder.scheduleText.text = "${booking.date} at ${booking.time} | ${booking.durationHours} hours"
        holder.deliveryText.text = "Delivery: ${booking.deliveryAddress.ifBlank { "Not provided" }}"
        holder.ownerText.text = "Owner: ${booking.ownerName.ifBlank { "Local owner" }}"
        holder.amountText.text = "Rs ${booking.totalPrice}"
        holder.statusText.text = booking.status
        holder.statusText.setTextColor(
            when (booking.status) {
                "Accepted" -> Color.parseColor("#1B5E20")
                "Declined" -> Color.parseColor("#C62828")
                else -> Color.parseColor("#A66A00")
            }
        )
    }
}
