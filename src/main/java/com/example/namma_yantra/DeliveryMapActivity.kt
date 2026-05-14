package com.example.namma_yantra

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

class DeliveryMapActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var pickupPoint: GeoPoint
    private lateinit var machineName: String
    private lateinit var ownerName: String
    private lateinit var pickupLocation: String
    private var deliveryRadiusKm: Int = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = packageName

        setContentView(R.layout.activity_delivery_map)

        machineName = intent.getStringExtra("machine_name") ?: "Machine"
        ownerName = intent.getStringExtra("machine_owner") ?: "Local owner"
        pickupLocation = intent.getStringExtra("machine_pickup") ?: "Farm pickup point"
        val latitude = intent.getDoubleExtra("machine_latitude", 12.9716)
        val longitude = intent.getDoubleExtra("machine_longitude", 77.5946)
        deliveryRadiusKm = intent.getIntExtra("machine_delivery_radius", 10)
        pickupPoint = GeoPoint(latitude, longitude)

        findViewById<TextView>(R.id.mapTitle).text = "$machineName Pickup"
        findViewById<TextView>(R.id.mapSubtitle).text = "$pickupLocation | Owner: $ownerName"
        findViewById<TextView>(R.id.mapDeliveryInfo).text = "Delivery available within $deliveryRadiusKm km from pickup point"

        mapView = findViewById(R.id.mapView)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(11.0)
        mapView.controller.setCenter(pickupPoint)

        val marker = Marker(mapView).apply {
            position = pickupPoint
            title = machineName
            snippet = pickupLocation
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }

        val circle = Polygon().apply {
            points = Polygon.pointsAsCircle(pickupPoint, deliveryRadiusKm.toDouble() * 1000.0)
            fillPaint.color = 0x223A7D44
            outlinePaint.color = 0xAA2E7D32.toInt()
            outlinePaint.strokeWidth = 4f
        }

        mapView.overlays.add(circle)
        mapView.overlays.add(marker)
        mapView.invalidate()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }
}
