package com.example.namma_yantra

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar
import java.util.Locale

class BookingActivity : AppCompatActivity() {

    private var pricePerHour = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        val machineName = findViewById<TextView>(R.id.machineName)
        val machineDetails = findViewById<TextView>(R.id.machineDetails)
        val dateInput = findViewById<EditText>(R.id.dateInput)
        val timeInput = findViewById<EditText>(R.id.timeInput)
        val durationInput = findViewById<EditText>(R.id.durationInput)
        val deliveryAddressInput = findViewById<EditText>(R.id.deliveryAddressInput)
        val totalPrice = findViewById<TextView>(R.id.totalPrice)
        val mapBtn = findViewById<Button>(R.id.mapBtn)
        val confirmBtn = findViewById<Button>(R.id.confirmBtn)

        val name = intent.getStringExtra("machine_name") ?: "Machine"
        pricePerHour = intent.getIntExtra("machine_price", 0)
        val owner = intent.getStringExtra("machine_owner") ?: "Local owner"
        val ownerId = intent.getStringExtra("machine_owner_id") ?: ""
        val condition = intent.getFloatExtra("machine_condition", 4.5f)
        val service = intent.getStringExtra("machine_service") ?: "Recently serviced"
        val distance = intent.getDoubleExtra("machine_distance", 2.5)
        val pickup = intent.getStringExtra("machine_pickup") ?: "Farm pickup point"
        val latitude = intent.getDoubleExtra("machine_latitude", 12.9716)
        val longitude = intent.getDoubleExtra("machine_longitude", 77.5946)
        val deliveryRadius = intent.getIntExtra("machine_delivery_radius", 10)

        machineName.text = name
        machineDetails.text = String.format(
            Locale.getDefault(),
            "Owner: %s | Pickup: %s | %.1f condition | Serviced %s | %.1f km away",
            owner,
            pickup,
            condition,
            service,
            distance
        )

        val calendar = Calendar.getInstance()

        dateInput.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    dateInput.setText(String.format(Locale.getDefault(), "%02d/%02d/%d", day, month + 1, year))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        timeInput.setOnClickListener {
            TimePickerDialog(
                this,
                { _, hour, minute ->
                    timeInput.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute))
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        durationInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateTotal(durationInput, totalPrice)
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })
        updateTotal(durationInput, totalPrice)

        mapBtn.setOnClickListener {
            val mapIntent = Intent(this, DeliveryMapActivity::class.java)
            mapIntent.putExtra("machine_name", name)
            mapIntent.putExtra("machine_owner", owner)
            mapIntent.putExtra("machine_pickup", pickup)
            mapIntent.putExtra("machine_latitude", latitude)
            mapIntent.putExtra("machine_longitude", longitude)
            mapIntent.putExtra("machine_delivery_radius", deliveryRadius)
            startActivity(mapIntent)
        }

        confirmBtn.setOnClickListener {
            val date = dateInput.text.toString()
            val time = timeInput.text.toString()
            val duration = durationInput.text.toString().toIntOrNull() ?: 0
            val deliveryAddress = deliveryAddressInput.text.toString().trim()

            if (date.isEmpty() || time.isEmpty() || duration <= 0 || deliveryAddress.isEmpty()) {
                Toast.makeText(this, "Select slot and enter delivery address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val total = pricePerHour * duration
            val booking = Booking(
                machineName = name,
                date = date,
                time = time,
                durationHours = duration,
                totalPrice = total,
                status = "Pending",
                deliveryAddress = deliveryAddress
            )

            confirmBtn.isEnabled = false
            confirmBtn.text = "Sending..."

            FirebaseRepository.addBooking(
                booking = booking,
                ownerName = owner,
                ownerId = ownerId,
                onSuccess = {
                    Toast.makeText(this, "Request sent to owner", Toast.LENGTH_SHORT).show()
                    finish()
                },
                onError = { error ->
                    confirmBtn.isEnabled = true
                    confirmBtn.text = "Send Request"
                    Toast.makeText(this, "Booking failed: ${error.message}", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun updateTotal(durationInput: EditText, totalPrice: TextView) {
        val duration = durationInput.text.toString().toIntOrNull() ?: 0
        val total = pricePerHour * duration
        totalPrice.text = if (duration > 0) "Rs $total" else "Rs 0"
    }
}
