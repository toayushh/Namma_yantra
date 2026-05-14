package com.example.namma_yantra

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    companion object {
        private const val MOUSE_WHEEL_SCROLL_STEP = 96
    }

    private val allMachines = mutableListOf<Machine>()
    private val visibleMachines = mutableListOf<Machine>()
    private lateinit var mainScrollView: ScrollView
    private lateinit var machineListContainer: LinearLayout
    private lateinit var emptyMachineText: TextView
    private lateinit var searchInput: EditText
    private lateinit var filterBtn: Button
    private lateinit var summaryText: TextView
    private lateinit var roleText: TextView
    private lateinit var roleHintText: TextView
    private lateinit var typeChipGroup: ChipGroup
    private lateinit var historyBtn: Button
    private lateinit var ownerRequestsBtn: Button
    private lateinit var addMachineBtn: Button
    private lateinit var fabAdd: FloatingActionButton
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private var showAvailableOnly = false
    private var selectedTypeFilter = "All"
    private var userRole = "Loading"
    private var canBookMachines = false
    private var touchDownX = 0f
    private var touchDownY = 0f
    private var lastTouchY = 0f
    private var forceScrolling = false
    private var machineListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (auth.currentUser == null) {
            openAuth()
            return
        }

        setContentView(R.layout.activity_main)

        fabAdd = findViewById(R.id.fabAdd)
        mainScrollView = findViewById(R.id.mainScrollView)
        machineListContainer = findViewById(R.id.machineListContainer)
        emptyMachineText = findViewById(R.id.emptyMachineText)
        bindHeader()

        loadCurrentUserProfile()
        listenToMachines()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (::mainScrollView.isInitialized &&
            (mainScrollView.canScrollVertically(1) || mainScrollView.canScrollVertically(-1))
        ) {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    touchDownX = event.x
                    touchDownY = event.y
                    lastTouchY = event.y
                    forceScrolling = false
                }
                MotionEvent.ACTION_MOVE -> {
                    val totalDx = event.x - touchDownX
                    val totalDy = event.y - touchDownY
                    val stepDy = lastTouchY - event.y

                    if (forceScrolling || abs(totalDy) > 10f && abs(totalDy) > abs(totalDx)) {
                        forceScrolling = true
                        mainScrollView.requestDisallowInterceptTouchEvent(false)
                        mainScrollView.scrollBy(0, stepDy.toInt())
                        lastTouchY = event.y
                        return true
                    }
                }
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    if (forceScrolling) {
                        forceScrolling = false
                        return true
                    }
                }
            }
        }

        return super.dispatchTouchEvent(event)
    }

    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
        if (::mainScrollView.isInitialized && event.action == MotionEvent.ACTION_SCROLL) {
            val wheelDelta = event.getAxisValue(MotionEvent.AXIS_VSCROLL)
            if (wheelDelta != 0f) {
                mainScrollView.scrollBy(0, (-wheelDelta * MOUSE_WHEEL_SCROLL_STEP).toInt())
                return true
            }
        }

        return super.dispatchGenericMotionEvent(event)
    }

    private fun bindHeader() {
        searchInput = findViewById(R.id.searchInput)
        filterBtn = findViewById(R.id.filterBtn)
        summaryText = findViewById(R.id.summaryText)
        roleText = findViewById(R.id.roleText)
        roleHintText = findViewById(R.id.roleHintText)
        typeChipGroup = findViewById(R.id.typeChipGroup)
        historyBtn = findViewById(R.id.historyBtn)
        ownerRequestsBtn = findViewById(R.id.ownerRequestsBtn)
        addMachineBtn = findViewById(R.id.addMachineBtn)

        historyBtn.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        ownerRequestsBtn.setOnClickListener {
            startActivity(Intent(this, OwnerRequestsActivity::class.java))
        }

        addMachineBtn.setOnClickListener {
            startActivity(Intent(this, AddMachineActivity::class.java))
        }

        fabAdd.setOnClickListener {
            startActivity(Intent(this, AddMachineActivity::class.java))
        }

        findViewById<Button>(R.id.signOutBtn).setOnClickListener {
            auth.signOut()
            openAuth()
        }

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        filterBtn.setOnClickListener {
            showAvailableOnly = !showAvailableOnly
            filterBtn.text = if (showAvailableOnly) "Available only" else "All machines"
            applyFilters()
        }

        typeChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            selectedTypeFilter = when (checkedIds.firstOrNull()) {
                R.id.chipTractor -> "tractor"
                R.id.chipHarvester -> "harvester"
                R.id.chipSprayer -> "sprayer"
                R.id.chipOthers -> "others"
                else -> "all"
            }
            applyFilters()
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        machineListener?.remove()
        super.onDestroy()
    }

    private fun listenToMachines() {
        summaryText.text = "Loading machines from Firestore"
        machineListener?.remove()
        machineListener = FirebaseRepository.listenToMachines(
            onResult = { machines ->
                allMachines.clear()
                allMachines.addAll(if (machines.isEmpty()) demoMachines() else machines)
                applyFilters()
            },
            onError = { error ->
                allMachines.clear()
                allMachines.addAll(demoMachines())
                applyFilters()
                Toast.makeText(this, "Firestore load failed: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun loadCurrentUserProfile() {
        val user = auth.currentUser
        if (user == null) {
            openAuth()
            return
        }

        roleText.text = "Loading profile"
        FirebaseRepository.getUserProfile(
            uid = user.uid,
            onSuccess = { profile ->
                userRole = profile?.role ?: "Renter"
                configureRoleUi(profile?.name ?: "App user", userRole)
            },
            onError = { error ->
                userRole = "Renter"
                configureRoleUi("App user", userRole)
                Toast.makeText(this, "Profile load failed: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun configureRoleUi(name: String, role: String) {
        val isOwner = role == "Owner"
        val isRenter = role == "Renter"

        roleText.text = "$role: $name"
        roleHintText.text = if (isOwner) {
            "Manage your listings and respond to rental requests"
        } else {
            "Browse nearby machines and send booking requests"
        }

        canBookMachines = isRenter
        ownerRequestsBtn.visibility = if (isOwner) View.VISIBLE else View.GONE
        addMachineBtn.visibility = if (isOwner) View.VISIBLE else View.GONE
        fabAdd.visibility = if (isOwner) View.VISIBLE else View.GONE
        historyBtn.visibility = if (isRenter) View.VISIBLE else View.GONE
        renderMachines()
    }

    private fun demoMachines(): List<Machine> = listOf(
        Machine(name = "Tractor", pricePerHour = 500, isAvailable = true, conditionRating = 4.8f, lastServiceDate = "15 Apr 2026", distanceKm = 1.8, ownerName = "Ravi Gowda", pickupLocation = "Mandya Main Road", latitude = 12.5218, longitude = 76.8951, deliveryRadiusKm = 15),
        Machine(name = "Harvester", pricePerHour = 800, isAvailable = false, conditionRating = 4.4f, lastServiceDate = "02 Mar 2026", distanceKm = 5.2, ownerName = "Lakshmi Farms", pickupLocation = "Hassan Farm Depot", latitude = 13.0068, longitude = 76.0996, deliveryRadiusKm = 20),
        Machine(name = "Sprayer", pricePerHour = 300, isAvailable = true, conditionRating = 4.6f, lastServiceDate = "28 Apr 2026", distanceKm = 3.1, ownerName = "Kiran Agro", pickupLocation = "Nelamangala Village", latitude = 13.1020, longitude = 77.3900, deliveryRadiusKm = 8),
        Machine(name = "Rotavator", pricePerHour = 400, isAvailable = true, conditionRating = 4.7f, lastServiceDate = "21 Apr 2026", distanceKm = 2.4, ownerName = "Manjunath", pickupLocation = "Kanakapura Road", latitude = 12.5462, longitude = 77.4211, deliveryRadiusKm = 12),
        Machine(name = "Power Tiller", pricePerHour = 450, isAvailable = true, conditionRating = 4.5f, lastServiceDate = "10 Apr 2026", distanceKm = 4.6, ownerName = "Green Field Co-op", pickupLocation = "Tumakuru Co-op Yard", latitude = 13.3379, longitude = 77.1173, deliveryRadiusKm = 18)
    )

    private fun applyFilters() {
        val query = searchInput.text.toString().trim().lowercase()

        visibleMachines.clear()
        visibleMachines.addAll(
            allMachines.filter { machine ->
                val matchesSearch = query.isEmpty() ||
                    machine.name.lowercase().contains(query) ||
                    machine.ownerName.lowercase().contains(query)
                val matchesAvailability = !showAvailableOnly || machine.isAvailable
                val matchesType = when (selectedTypeFilter) {
                    "tractor" -> "tractor" in machine.type
                    "harvester" -> "harvest" in machine.type
                    "sprayer" -> "spray" in machine.type
                    "others" -> listOf("tractor", "harvest", "spray").none { it in machine.type }
                    else -> true
                }
                matchesSearch && matchesAvailability && matchesType
            }.sortedWith(compareByDescending<Machine> { it.isAvailable }.thenBy { it.distanceKm })
        )

        val availableCount = allMachines.count { it.isAvailable }
        val mode = if (userRole == "Owner") "owner view" else "renter view"
        summaryText.text = "$availableCount available nearby | ${visibleMachines.size} shown | $mode"
        renderMachines()
    }

    private fun renderMachines() {
        if (!::machineListContainer.isInitialized) return

        machineListContainer.removeAllViews()
        emptyMachineText.visibility = if (visibleMachines.isEmpty()) View.VISIBLE else View.GONE
        visibleMachines.forEach { machine ->
            val itemView = layoutInflater.inflate(R.layout.item_machine, machineListContainer, false)
            MachineAdapter.bindCard(itemView, machine, canBookMachines, animate = false)
            machineListContainer.addView(itemView)
        }
    }

    private fun openAuth() {
        startActivity(Intent(this, AuthActivity::class.java))
        finish()
    }
}
