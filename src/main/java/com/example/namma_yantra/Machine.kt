package com.example.namma_yantra

data class Machine(
    val id: String = "",
    val name: String,
    val pricePerHour: Int,
    val isAvailable: Boolean,
    val conditionRating: Float = 4.5f,
    val lastServiceDate: String = "Recently serviced",
    val distanceKm: Double = 2.5,
    val ownerName: String = "Local owner",
    val ownerId: String = "",
    val ownerEmail: String = "",
    val pickupLocation: String = "Farm pickup point",
    val latitude: Double = 12.9716,
    val longitude: Double = 77.5946,
    val deliveryRadiusKm: Int = 10
) {
    val type: String
        get() = name.trim().lowercase()
}
