package com.chaitany.carbonview

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.database.*
import com.google.android.material.textview.MaterialTextView

class VehicleReport : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var carRef: DatabaseReference
    private lateinit var bikeRef: DatabaseReference

    private lateinit var totalKmText: MaterialTextView
    private lateinit var totalCO2Text: MaterialTextView
    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart

    private var totalCarKm = 0.0
    private var totalBikeKm = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicle_report)

        // Initialize Firebase
        database = FirebaseDatabase.getInstance()
        carRef = database.getReference("car_data")
        bikeRef = database.getReference("bike_data")

        // Initialize Views
        totalKmText = findViewById(R.id.totalKmText)
        totalCO2Text = findViewById(R.id.totalCO2Text)
        pieChart = findViewById(R.id.pieChart)
        barChart = findViewById(R.id.barChart)

        fetchVehicleData()
    }

    private fun fetchVehicleData() {
        // Fetch car data
        carRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                totalCarKm = 0.0
                for (entry in snapshot.children) {
                    val km = entry.child("car_current_km").getValue(Double::class.java) ?: 0.0
                    totalCarKm += km
                }
                fetchBikeData()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error fetching car data", error.toException())
            }
        })
    }

    private fun fetchBikeData() {
        // Fetch bike data
        bikeRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                totalBikeKm = 0.0
                for (entry in snapshot.children) {
                    val km = entry.child("bike_current_km").getValue(Double::class.java) ?: 0.0
                    totalBikeKm += km
                }
                calculateAndDisplayData()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error fetching bike data", error.toException())
            }
        })
    }

    private fun calculateAndDisplayData() {
        val carCO2 = (totalCarKm / 15) * 2.68  // Car CO2 in kg
        val bikeCO2 = (totalBikeKm / 40) * 2.32 // Bike CO2 in kg
        val totalCO2 = carCO2 + bikeCO2

        totalKmText.text = "Total Distance Driven: ${totalCarKm + totalBikeKm} km"
        totalCO2Text.text = "Total CO₂ Emissions: ${"%.2f".format(totalCO2)} kg"

        // Update Pie Chart
        val pieEntries = listOf(
            PieEntry(carCO2.toFloat(), "Car"),
            PieEntry(bikeCO2.toFloat(), "Bike")
        )
        val pieDataSet = PieDataSet(pieEntries, "CO₂ Emissions").apply { setColors(0xFFFF0000.toInt(), 0xFF0000FF.toInt()) }
        pieChart.data = PieData(pieDataSet)
        pieChart.invalidate()

        // Update Bar Chart
        val barEntries = listOf(
            BarEntry(1f, carCO2.toFloat()),
            BarEntry(2f, bikeCO2.toFloat())
        )
        val barDataSet = BarDataSet(barEntries, "CO₂ Emissions")
        barChart.data = BarData(barDataSet)
        barChart.invalidate()
    }
}
