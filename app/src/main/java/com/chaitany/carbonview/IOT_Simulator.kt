package com.chaitany.carbonview

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.*

class IOT_Simulator : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var scope1Text: TextView
    private lateinit var scope2Text: TextView
    private lateinit var scope3Text: TextView
    private lateinit var recentEntryText: TextView
    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart
    private lateinit var generatePdfButton:MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iot_simulator)

        // Initialize UI Elements
        scope1Text = findViewById(R.id.scope1Text)
        scope2Text = findViewById(R.id.scope2Text)
        scope3Text = findViewById(R.id.scope3Text)
        recentEntryText = findViewById(R.id.recentEntryText)
        pieChart = findViewById(R.id.pieChart)
        barChart = findViewById(R.id.barChart)
        generatePdfButton=findViewById(R.id.generatePdfButton)

        database = FirebaseDatabase.getInstance().getReference("emissions_data")

        fetchData()


        generatePdfButton.setOnClickListener {
            val intent = Intent(this, IOTReport::class.java)
            startActivity(intent)
        }
    }

    private fun fetchData() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var scope1 = 0.0
                var scope2 = 0.0
                var scope3 = 0.0
                var recentEmission: EmissionData? = null

                for (data in snapshot.children) {
                    try {
                        val emissionData = data.getValue(EmissionData::class.java)
                        emissionData?.let {
                            recentEmission = it // Save the last entry
                            when (it.Scope) {
                                "Scope 1" -> scope1 += it.Emission
                                "Scope 2" -> scope2 += it.Emission
                                "Scope 3" -> scope3 += it.Emission
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("FirebaseError", "Error parsing data: ${e.message}")
                    }
                }

                // Update UI
                scope1Text.text = "Scope 1: $scope1 kg"
                scope2Text.text = "Scope 2: $scope2 kg"
                scope3Text.text = "Scope 3: $scope3 kg"

                // Show the most recent entry with all details
                recentEmission?.let {
                    val recentText = """
                        🔹 **Activity:** ${it.Activity}
                        📅 **Date:** ${it.Date}
                        🔥 **Emission:** ${it.Emission} kg
                        🌍 **Scope:** ${it.Scope}
                        ⏳ **Timestamp:** ${it.timestamp}
                    """.trimIndent()

                    recentEntryText.text = recentText
                }

                // Update Charts
                updatePieChart(scope1, scope2, scope3)
                updateBarChart(scope1, scope2, scope3)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Database Error: ${error.message}")
            }
        })
    }

    private fun updatePieChart(scope1: Double, scope2: Double, scope3: Double) {
        val entries = listOf(
            PieEntry(scope1.toFloat(), "Scope 1"),
            PieEntry(scope2.toFloat(), "Scope 2"),
            PieEntry(scope3.toFloat(), "Scope 3")
        )

        val dataSet = PieDataSet(entries, "Emissions").apply {
            colors = listOf(Color.RED, Color.YELLOW, Color.GREEN)
        }

        pieChart.apply {
            data = PieData(dataSet)
            description.text = "Distribution of Emission Scopes"
            setEntryLabelColor(Color.BLACK)
            animateY(1000)
            invalidate()
        }
    }

    private fun updateBarChart(scope1: Double, scope2: Double, scope3: Double) {
        val entries = listOf(
            BarEntry(1f, scope1.toFloat()),
            BarEntry(2f, scope2.toFloat()),
            BarEntry(3f, scope3.toFloat())
        )

        val dataSet = BarDataSet(entries, "Emission Data").apply {
            colors = listOf(Color.RED, Color.BLUE, Color.GREEN)
        }

        barChart.apply {
            data = BarData(dataSet)
            description.text = "Comparison of Emissions Across Scopes"
            setFitBars(true)
            animateY(1000)
            invalidate()
        }
    }
}


