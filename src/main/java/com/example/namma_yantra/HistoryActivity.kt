package com.example.namma_yantra

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration

class HistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: TextView
    private lateinit var summaryText: TextView
    private val bookings = mutableListOf<Booking>()
    private lateinit var adapter: BookingHistoryAdapter
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private var bookingListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        recyclerView = findViewById(R.id.historyRecyclerView)
        emptyText = findViewById(R.id.emptyText)
        summaryText = findViewById(R.id.historySummary)
        val clearBtn = findViewById<Button>(R.id.clearBtn)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = BookingHistoryAdapter(bookings)
        recyclerView.adapter = adapter

        listenToBookings()

        clearBtn.setOnClickListener {
            val renterId = auth.currentUser?.uid
            if (renterId.isNullOrBlank()) {
                Toast.makeText(this, "Login again to clear bookings", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            clearBtn.isEnabled = false
            clearBtn.text = "Clearing..."
            FirebaseRepository.clearRenterBookings(
                renterId = renterId,
                onSuccess = {
                    clearBtn.isEnabled = true
                    clearBtn.text = "Clear History"
                    Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show()
                },
                onError = { error ->
                    clearBtn.isEnabled = true
                    clearBtn.text = "Clear History"
                    Toast.makeText(this, "Clear failed: ${error.message}", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    override fun onDestroy() {
        bookingListener?.remove()
        super.onDestroy()
    }

    private fun listenToBookings() {
        val renterId = auth.currentUser?.uid
        if (renterId.isNullOrBlank()) {
            emptyText.text = "Login again to view your bookings"
            return
        }

        emptyText.text = "Loading your booking requests"
        bookingListener?.remove()
        bookingListener = FirebaseRepository.listenToRenterBookings(
            renterId = renterId,
            onResult = { bookings ->
                this.bookings.clear()
                this.bookings.addAll(bookings)
                emptyText.text = if (bookings.isEmpty()) "No booking requests yet" else ""
                summaryText.text = "${bookings.size} requests tracked for your account"
                adapter.notifyDataSetChanged()
            },
            onError = { error ->
                emptyText.text = "Could not load booking requests"
                Toast.makeText(this, "Firestore load failed: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )
    }
}
