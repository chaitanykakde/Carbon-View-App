package com.chaitany.carbonview

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.google.firebase.database.*

class IOT_Simulator : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var pieChart: PieChart
    private lateinit var recyclerView: RecyclerView
    private lateinit var emissionAdapter: EmissionAdapter
    private val emissionList = ArrayList<EmissionData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iot_simulator)

        // Initialize Views
        pieChart = findViewById(R.id.pieChart)
        recyclerView = findViewById(R.id.recyclerView)

        setupPieChart()
        setupRecyclerView()

        // Firebase Database Reference
        database = FirebaseDatabase.getInstance().getReference("emissions_data")

        // Fetch Data in Real-Time
        fetchData()
    }

    // Configure the Pie Chart
    private fun setupPieChart() {
        pieChart.apply {
            description.isEnabled = false
            setUsePercentValues(true)
            isDrawHoleEnabled = true
            holeRadius = 40f
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
        }
    }

    // Setup RecyclerView
    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            reverseLayout = true // Newest entry at top
            stackFromEnd = true
        }
        emissionAdapter = EmissionAdapter(emissionList)
        recyclerView.adapter = emissionAdapter
    }

    // Fetch Data from Firebase
    private fun fetchData() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var scope1 = 0f
                var scope2 = 0f
                var scope3 = 0f
                val tempList = ArrayList<EmissionData>()

                for (data in snapshot.children) {
                    try {
                        val emissionData = data.getValue(EmissionData::class.java)
                        emissionData?.let {
                            tempList.add(it)

                            // Correctly retrieve emission values based on Scope
                            when (it.Scope) {
                                "Scope 1" -> scope1 += it.`Emission`.toFloat()
                                "Scope 2" -> scope2 += it.`Emission`.toFloat()
                                "Scope 3" -> scope3 += it.`Emission`.toFloat()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("FirebaseError", "Error parsing data: ${e.message}")
                    }
                }

                // Reverse list so newest entries appear first
                tempList.reverse()

                // Update RecyclerView
                emissionList.clear()
                emissionList.addAll(tempList)
                emissionAdapter.updateList(emissionList)

                // Update Pie Chart
                updatePieChart(scope1, scope2, scope3)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Database Error: ${error.message}")
            }
        })
    }


    private fun updatePieChart(scope1: Float, scope2: Float, scope3: Float) {
        pieChart.clear() // Clear old data

        val entries = mutableListOf<PieEntry>()
        if (scope1 > 0) entries.add(PieEntry(scope1, "Scope 1"))
        if (scope2 > 0) entries.add(PieEntry(scope2, "Scope 2"))
        if (scope3 > 0) entries.add(PieEntry(scope3, "Scope 3"))

        if (entries.isEmpty()) {
            Log.e("PieChart", "No data available for Pie Chart")
            return
        }

        val dataSet = PieDataSet(entries, "Emission Categories").apply {
            colors = listOf(Color.RED, Color.BLUE, Color.GREEN)
            valueTextSize = 14f
        }

        pieChart.data = PieData(dataSet)
        pieChart.invalidate() // Refresh chart
    }



}
