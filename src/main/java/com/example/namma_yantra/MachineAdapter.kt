package com.example.namma_yantra

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class MachineAdapter(
    private val list: List<Machine>,
    private var canBookMachines: Boolean = false
) :
    RecyclerView.Adapter<MachineAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.machineIcon)
        val name: TextView = itemView.findViewById(R.id.machineName)
        val price: TextView = itemView.findViewById(R.id.machinePrice)
        val status: TextView = itemView.findViewById(R.id.machineStatus)
        val condition: TextView = itemView.findViewById(R.id.machineCondition)
        val meta: TextView = itemView.findViewById(R.id.machineMeta)
        val owner: TextView = itemView.findViewById(R.id.machineOwner)
        val bookBtn: Button = itemView.findViewById(R.id.bookBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_machine, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        bindCard(holder.itemView, list[position], canBookMachines, animate = true)
    }

    fun setCanBookMachines(canBook: Boolean) {
        canBookMachines = canBook
        notifyDataSetChanged()
    }

    companion object {
        fun bindCard(itemView: View, machine: Machine, canBookMachines: Boolean, animate: Boolean) {
            val holder = ViewHolder(itemView)

            if (animate) {
                val animation = AnimationUtils.loadAnimation(itemView.context, R.anim.item_anim)
                itemView.startAnimation(animation)
            } else {
                itemView.clearAnimation()
            }

            holder.icon.setImageResource(iconFor(machine.name))
            holder.name.text = machine.name
            holder.price.text = "Rs ${machine.pricePerHour}/hr"
            holder.status.text = if (machine.isAvailable) "Available" else "Not available"
            holder.condition.text = String.format(Locale.getDefault(), "%.1f condition", machine.conditionRating)
            holder.meta.text = String.format(
                Locale.getDefault(),
                "Pickup: %s | %.1f km away | %d km delivery",
                machine.pickupLocation,
                machine.distanceKm,
                machine.deliveryRadiusKm
            )
            holder.owner.text = "Owner: ${machine.ownerName}"

            if (machine.isAvailable && canBookMachines) {
                holder.status.setTextColor(Color.parseColor("#2E7D32"))
                holder.bookBtn.isEnabled = true
                holder.bookBtn.alpha = 1f
                holder.bookBtn.text = "Book"
            } else if (machine.isAvailable) {
                holder.status.setTextColor(Color.parseColor("#2E7D32"))
                holder.bookBtn.isEnabled = false
                holder.bookBtn.alpha = 0.55f
                holder.bookBtn.text = "Owner"
            } else {
                holder.status.setTextColor(Color.parseColor("#C62828"))
                holder.bookBtn.isEnabled = false
                holder.bookBtn.alpha = 0.55f
                holder.bookBtn.text = "Closed"
            }

            itemView.setOnClickListener(null)

            holder.bookBtn.setOnClickListener {
                holder.bookBtn.animate()
                    .scaleX(0.94f)
                    .scaleY(0.94f)
                    .setDuration(90)
                    .withEndAction {
                        holder.bookBtn.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(90)
                            .start()
                    }
                    .start()

                if (!machine.isAvailable) {
                    Toast.makeText(itemView.context, "Not available", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (!canBookMachines) {
                    Toast.makeText(itemView.context, "Only renters can book machines", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val intent = Intent(itemView.context, BookingActivity::class.java)
                intent.putExtra("machine_name", machine.name)
                intent.putExtra("machine_price", machine.pricePerHour)
                intent.putExtra("machine_owner", machine.ownerName)
                intent.putExtra("machine_owner_id", machine.ownerId)
                intent.putExtra("machine_condition", machine.conditionRating)
                intent.putExtra("machine_service", machine.lastServiceDate)
                intent.putExtra("machine_distance", machine.distanceKm)
                intent.putExtra("machine_pickup", machine.pickupLocation)
                intent.putExtra("machine_latitude", machine.latitude)
                intent.putExtra("machine_longitude", machine.longitude)
                intent.putExtra("machine_delivery_radius", machine.deliveryRadiusKm)
                itemView.context.startActivity(intent)
            }
        }

        private fun iconFor(name: String): Int {
            val type = name.lowercase(Locale.getDefault())
            return when {
                "harvest" in type -> R.drawable.ic_harvester
                "spray" in type -> R.drawable.ic_sprayer
                "rotavator" in type -> R.drawable.ic_rotavator
                "tiller" in type -> R.drawable.ic_tiller
                "irrig" in type -> R.drawable.ic_irrigation
                "thresh" in type -> R.drawable.ic_thresher
                else -> R.drawable.ic_tractor
            }
        }
    }
}
