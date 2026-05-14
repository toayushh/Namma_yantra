package com.example.namma_yantra

data class Booking(
    val id: String = "",
    val machineName: String,
    val date: String,
    val time: String,
    val durationHours: Int,
    val totalPrice: Int,
    val status: String = "Pending",
    val ownerId: String = "",
    val ownerName: String = "",
    val renterId: String = "",
    val renterEmail: String = "",
    val deliveryAddress: String = ""
)
