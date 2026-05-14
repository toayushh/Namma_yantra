package com.example.namma_yantra

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

object FirebaseRepository {
    private const val MACHINES = "machines"
    private const val BOOKINGS = "bookings"
    private const val USERS = "users"

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    fun saveUserProfile(
        profile: UserProfile,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val data = hashMapOf(
            "uid" to profile.uid,
            "name" to profile.name,
            "email" to profile.email,
            "role" to profile.role,
            "createdAt" to Timestamp.now()
        )

        db.collection(USERS)
            .document(profile.uid)
            .set(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun getUserProfile(
        uid: String,
        onSuccess: (UserProfile?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection(USERS)
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    onSuccess(null)
                    return@addOnSuccessListener
                }

                onSuccess(
                    UserProfile(
                        uid = document.getString("uid") ?: uid,
                        name = document.getString("name") ?: "App user",
                        email = document.getString("email") ?: "",
                        role = document.getString("role") ?: "Renter"
                    )
                )
            }
            .addOnFailureListener { onError(it) }
    }

    fun listenToMachines(
        onResult: (List<Machine>) -> Unit,
        onError: (Exception) -> Unit
    ) = db.collection(MACHINES)
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                onError(error)
                return@addSnapshotListener
            }

            val machines = snapshot?.documents
                ?.mapNotNull { document ->
                    val name = document.getString("name") ?: return@mapNotNull null
                    Machine(
                        id = document.id,
                        name = name,
                        pricePerHour = document.getLong("pricePerHour")?.toInt() ?: 0,
                        isAvailable = document.getBoolean("isAvailable") ?: true,
                        conditionRating = document.getDouble("conditionRating")?.toFloat() ?: 4.5f,
                        lastServiceDate = document.getString("lastServiceDate") ?: "Recently serviced",
                        distanceKm = document.getDouble("distanceKm") ?: 2.5,
                        ownerName = document.getString("ownerName") ?: "Local owner",
                        ownerId = document.getString("ownerId") ?: "",
                        ownerEmail = document.getString("ownerEmail") ?: "",
                        pickupLocation = document.getString("pickupLocation") ?: "Farm pickup point",
                        latitude = document.getDouble("latitude") ?: 12.9716,
                        longitude = document.getDouble("longitude") ?: 77.5946,
                        deliveryRadiusKm = document.getLong("deliveryRadiusKm")?.toInt() ?: 10
                    )
                }
                .orEmpty()

            onResult(machines)
        }

    fun addMachine(
        machine: Machine,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection(MACHINES)
            .add(machine.toFirestoreMap())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun addBooking(
        booking: Booking,
        ownerName: String,
        ownerId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val data = hashMapOf(
            "machineName" to booking.machineName,
            "date" to booking.date,
            "time" to booking.time,
            "durationHours" to booking.durationHours,
            "totalPrice" to booking.totalPrice,
            "status" to booking.status,
            "ownerName" to ownerName,
            "ownerId" to ownerId,
            "renterId" to (auth.currentUser?.uid ?: ""),
            "renterEmail" to (auth.currentUser?.email ?: ""),
            "deliveryAddress" to booking.deliveryAddress,
            "createdAt" to Timestamp.now()
        )

        db.collection(BOOKINGS)
            .add(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun listenToBookings(
        onResult: (List<Booking>) -> Unit,
        onError: (Exception) -> Unit
    ) = db.collection(BOOKINGS)
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                onError(error)
                return@addSnapshotListener
            }

            val bookings = snapshot?.documents
                ?.mapNotNull { document ->
                    val machineName = document.getString("machineName") ?: return@mapNotNull null
                    Booking(
                        id = document.id,
                        machineName = machineName,
                        date = document.getString("date") ?: "",
                        time = document.getString("time") ?: "",
                        durationHours = document.getLong("durationHours")?.toInt() ?: 0,
                        totalPrice = document.getLong("totalPrice")?.toInt() ?: 0,
                        status = document.getString("status") ?: "Pending",
                        ownerId = document.getString("ownerId") ?: "",
                        ownerName = document.getString("ownerName") ?: "",
                        renterId = document.getString("renterId") ?: "",
                        renterEmail = document.getString("renterEmail") ?: "",
                        deliveryAddress = document.getString("deliveryAddress") ?: ""
                    )
                }
                .orEmpty()

            onResult(bookings)
        }

    fun listenToRenterBookings(
        renterId: String,
        onResult: (List<Booking>) -> Unit,
        onError: (Exception) -> Unit
    ) = db.collection(BOOKINGS)
        .whereEqualTo("renterId", renterId)
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                onError(error)
                return@addSnapshotListener
            }

            val bookings = snapshot?.documents
                ?.mapNotNull { document ->
                    val machineName = document.getString("machineName") ?: return@mapNotNull null
                    Booking(
                        id = document.id,
                        machineName = machineName,
                        date = document.getString("date") ?: "",
                        time = document.getString("time") ?: "",
                        durationHours = document.getLong("durationHours")?.toInt() ?: 0,
                        totalPrice = document.getLong("totalPrice")?.toInt() ?: 0,
                        status = document.getString("status") ?: "Pending",
                        ownerId = document.getString("ownerId") ?: "",
                        ownerName = document.getString("ownerName") ?: "",
                        renterId = document.getString("renterId") ?: "",
                        renterEmail = document.getString("renterEmail") ?: "",
                        deliveryAddress = document.getString("deliveryAddress") ?: ""
                    )
                }
                .orEmpty()
                .sortedByDescending { it.status == "Pending" }

            onResult(bookings)
        }

    fun listenToOwnerBookings(
        ownerId: String,
        onResult: (List<Booking>) -> Unit,
        onError: (Exception) -> Unit
    ) = db.collection(BOOKINGS)
        .whereEqualTo("ownerId", ownerId)
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                onError(error)
                return@addSnapshotListener
            }

            val bookings = snapshot?.documents
                ?.mapNotNull { document ->
                    val machineName = document.getString("machineName") ?: return@mapNotNull null
                    Booking(
                        id = document.id,
                        machineName = machineName,
                        date = document.getString("date") ?: "",
                        time = document.getString("time") ?: "",
                        durationHours = document.getLong("durationHours")?.toInt() ?: 0,
                        totalPrice = document.getLong("totalPrice")?.toInt() ?: 0,
                        status = document.getString("status") ?: "Pending",
                        ownerId = document.getString("ownerId") ?: "",
                        ownerName = document.getString("ownerName") ?: "",
                        renterId = document.getString("renterId") ?: "",
                        renterEmail = document.getString("renterEmail") ?: "",
                        deliveryAddress = document.getString("deliveryAddress") ?: ""
                    )
                }
                .orEmpty()
                .sortedByDescending { it.status == "Pending" }

            onResult(bookings)
        }

    fun updateBookingStatus(
        bookingId: String,
        status: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection(BOOKINGS)
            .document(bookingId)
            .update(
                mapOf(
                    "status" to status,
                    "updatedAt" to Timestamp.now()
                )
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun clearBookings(
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection(BOOKINGS)
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = db.batch()
                snapshot.documents.forEach { batch.delete(it.reference) }
                batch.commit()
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onError(it) }
            }
            .addOnFailureListener { onError(it) }
    }

    fun clearRenterBookings(
        renterId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection(BOOKINGS)
            .whereEqualTo("renterId", renterId)
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = db.batch()
                snapshot.documents.forEach { batch.delete(it.reference) }
                batch.commit()
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onError(it) }
            }
            .addOnFailureListener { onError(it) }
    }

    private fun Machine.toFirestoreMap(): Map<String, Any> = hashMapOf(
        "name" to name,
        "pricePerHour" to pricePerHour,
        "isAvailable" to isAvailable,
        "conditionRating" to conditionRating,
        "lastServiceDate" to lastServiceDate,
        "distanceKm" to distanceKm,
        "ownerName" to ownerName,
        "ownerId" to (auth.currentUser?.uid ?: ""),
        "ownerEmail" to (auth.currentUser?.email ?: ""),
        "pickupLocation" to pickupLocation,
        "latitude" to latitude,
        "longitude" to longitude,
        "deliveryRadiusKm" to deliveryRadiusKm,
        "createdAt" to Timestamp.now()
    )
}
