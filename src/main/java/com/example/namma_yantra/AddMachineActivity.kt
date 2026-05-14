package com.example.namma_yantra

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddMachineActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_machine)

        val nameInput = findViewById<EditText>(R.id.nameInput)
        val priceInput = findViewById<EditText>(R.id.priceInput)
        val conditionInput = findViewById<EditText>(R.id.conditionInput)
        val serviceInput = findViewById<EditText>(R.id.serviceInput)
        val distanceInput = findViewById<EditText>(R.id.distanceInput)
        val ownerInput = findViewById<EditText>(R.id.ownerInput)
        val pickupInput = findViewById<EditText>(R.id.pickupInput)
        val latitudeInput = findViewById<EditText>(R.id.latitudeInput)
        val longitudeInput = findViewById<EditText>(R.id.longitudeInput)
        val radiusInput = findViewById<EditText>(R.id.radiusInput)
        val availableInput = findViewById<CheckBox>(R.id.availableInput)
        val saveBtn = findViewById<Button>(R.id.saveBtn)

        saveBtn.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val price = priceInput.text.toString().toIntOrNull()
            val condition = conditionInput.text.toString().toFloatOrNull() ?: 4.5f
            val service = serviceInput.text.toString().trim().ifBlank { "Recently serviced" }
            val distance = distanceInput.text.toString().toDoubleOrNull() ?: 2.0
            val owner = ownerInput.text.toString().trim().ifBlank { "You" }
            val pickup = pickupInput.text.toString().trim().ifBlank { "Farm pickup point" }
            val latitude = latitudeInput.text.toString().toDoubleOrNull() ?: 12.9716
            val longitude = longitudeInput.text.toString().toDoubleOrNull() ?: 77.5946
            val deliveryRadius = radiusInput.text.toString().toIntOrNull() ?: 10

            if (name.isEmpty() || price == null || price <= 0) {
                Toast.makeText(this, "Enter machine name and valid hourly rate", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (condition !in 1.0f..5.0f) {
                Toast.makeText(this, "Condition must be between 1 and 5", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveBtn.isEnabled = false
            saveBtn.text = "Publishing..."

            val machine = Machine(
                name = name,
                pricePerHour = price,
                isAvailable = availableInput.isChecked,
                conditionRating = condition,
                lastServiceDate = service,
                distanceKm = distance,
                ownerName = owner,
                pickupLocation = pickup,
                latitude = latitude,
                longitude = longitude,
                deliveryRadiusKm = deliveryRadius
            )

            FirebaseRepository.addMachine(
                machine = machine,
                onSuccess = {
                    Toast.makeText(this, "Machine listed on Firestore", Toast.LENGTH_SHORT).show()
                    finish()
                },
                onError = { error ->
                    saveBtn.isEnabled = true
                    saveBtn.text = "Publish Machine"
                    Toast.makeText(this, "Listing failed: ${error.message}", Toast.LENGTH_LONG).show()
                }
            )
        }
    }
}
