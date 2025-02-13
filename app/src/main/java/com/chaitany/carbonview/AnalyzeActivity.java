package com.chaitany.carbonview;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.Entry;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AnalyzeActivity extends AppCompatActivity {

    private LineChart lineChart;
    private ProgressBar progressBar;
    private String fileUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyze);

        lineChart = findViewById(R.id.lineChart);
        progressBar = findViewById(R.id.progressBar);

        // Get the file URL from the Intent extras
        fileUrl = getIntent().getStringExtra("fileUrl");

        // Start loading and fetch the file from Firebase
        if (fileUrl != null) {
            fetchAndDisplayFile(fileUrl);
        } else {
            Toast.makeText(this, "File URL is missing", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchAndDisplayFile(String fileUrl) {
        // Show loading
        progressBar.setVisibility(View.VISIBLE);

        // Get reference to the file in Firebase Storage
        StorageReference fileRef = FirebaseStorage.getInstance().getReferenceFromUrl(fileUrl);

        // Download the file from Firebase Storage
        fileRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
            // Process the file's content once fetched
            processFileContent(bytes);
        }).addOnFailureListener(e -> {
            // Handle errors
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Failed to fetch the file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void processFileContent(byte[] fileData) {
        try {
            // Convert the file data (byte array) to a String
            String fileContent = new String(fileData, StandardCharsets.UTF_8);

            // Log the fetched file content for debugging
            Log.d("AnalyzeActivity", "Fetched file content:\n" + fileContent);

            // Parse the CSV data into a list of DataPoints
            List<DataPoint> dataPoints = parseCsv(fileContent);

            // Log the number of data points for debugging
            Log.d("AnalyzeActivity", "Data Points: " + dataPoints.size());

            // Show the data on the chart if data points are found
            if (!dataPoints.isEmpty()) {
                showChart(dataPoints);
            } else {
                Toast.makeText(this, "No valid data found in the file.", Toast.LENGTH_SHORT).show();
            }

            // Hide loading indicator
            progressBar.setVisibility(View.GONE);

        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Error processing file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private List<DataPoint> parseCsv(String csvData) {
        List<DataPoint> dataPoints = new ArrayList<>();
        String[] lines = csvData.split("\n");

        // Assuming CSV structure: "Date, Value"
        for (String line : lines) {
            String[] columns = line.split(",");
            if (columns.length == 2) {
                try {
                    String date = columns[0].trim();
                    float value = Float.parseFloat(columns[1].trim());
                    dataPoints.add(new DataPoint(date, value));
                } catch (NumberFormatException e) {
                    continue;  // Skip if there's an error parsing the number
                }
            }
        }
        return dataPoints;
    }

    private void showChart(List<DataPoint> dataPoints) {
        List<Entry> entries = new ArrayList<>();

        // Loop through data points and create chart entries
        for (int i = 0; i < dataPoints.size(); i++) {
            DataPoint point = dataPoints.get(i);
            // Adding the entry (i for X-axis and value for Y-axis)
            entries.add(new Entry(i, point.getValue()));
        }

        // Create a DataSet and customize it
        LineDataSet dataSet = new LineDataSet(entries, "Uploaded Data");
        dataSet.setColor(Color.RED);  // Customize line color
        dataSet.setValueTextColor(Color.BLACK);  // Customize value text color

        // Add data to chart
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // Optional: Customize X and Y Axis
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setGranularity(1f);
        lineChart.getAxisLeft().setGranularity(1f);
        lineChart.getAxisRight().setEnabled(false);  // Disable right axis if not needed

        // Refresh chart
        lineChart.invalidate();
    }

    // DataPoint class
    public static class DataPoint {
        private String date;
        private float value;

        public DataPoint(String date, float value) {
            this.date = date;
            this.value = value;
        }

        public String getDate() {
            return date;
        }

        public float getValue() {
            return value;
        }
    }
}
