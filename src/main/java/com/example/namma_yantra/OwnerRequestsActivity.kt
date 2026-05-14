package com.example.namma_yantra

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration

class OwnerRequestsActivity : AppCompatActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val requests = mutableListOf<Booking>()
    private lateinit var adapter: OwnerRequestAdapter
    private lateinit var emptyText: TextView
    private var requestListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_requests)

        emptyText = findViewById(R.id.emptyText)
        val recyclerView = findViewById<RecyclerView>(R.id.requestsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = OwnerRequestAdapter(requests) { booking, status ->
            updateStatus(booking, status)
        }
        recyclerView.adapter = adapter

        listenToRequests()
    }

    override fun onDestroy() {
        requestListener?.remove()
        super.onDestroy()
    }

    private fun listenToRequests() {
        val ownerId = auth.currentUser?.uid
        if (ownerId.isNullOrBlank()) {
            emptyText.text = "Login again to view owner requests"
            return
        }

        emptyText.text = "Loading owner requests"
        requestListener?.remove()
        requestListener = FirebaseRepository.listenToOwnerBookings(
            ownerId = ownerId,
            onResult = { bookings ->
                requests.clear()
                requests.addAll(bookings)
                emptyText.text = if (requests.isEmpty()) "No requests for your listed machines yet" else ""
                adapter.notifyDataSetChanged()
            },
            onError = { error ->
                emptyText.text = "Could not load owner requests"
                Toast.makeText(this, "Load failed: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun updateStatus(booking: Booking, status: String) {
        if (booking.id.isBlank()) {
            Toast.makeText(this, "Missing booking id", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseRepository.updateBookingStatus(
            bookingId = booking.id,
            status = status,
            onSuccess = {
                Toast.makeText(this, "Request $status", Toast.LENGTH_SHORT).show()
            },
            onError = { error ->
                Toast.makeText(this, "Update failed: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )
    }
}
