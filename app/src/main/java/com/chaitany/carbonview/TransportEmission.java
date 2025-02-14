package com.chaitany.carbonview;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser ;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class TransportEmission extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private EditText inputWeight, inputDistance;
    private Spinner weightUnitSpinner, distanceUnitSpinner, transportMethodSpinner;
    private TextView carbonG, carbonLb, carbonKg, carbonMt, estimatedAt;
    private CardView resultCard;
    private Button btnCalculate;

    private String selectedWeightUnit = "g";
    private String selectedDistanceUnit = "km";
    private String selectedTransportMethod = "truck";

    // Predefined emission factors (example values)
    private static final double EMISSION_FACTOR_TRUCK = 0.2; // kg CO2 per ton-km
    private static final double EMISSION_FACTOR_SHIP = 0.01; // kg CO2 per ton-km
    private static final double EMISSION_FACTOR_TRAIN = 0.05; // kg CO2 per ton-km
    private static final double EMISSION_FACTOR_PLANE = 0.5; // kg CO2 per ton-km

    private FirebaseAuth auth;
    private StorageReference storageReference;
    private Dialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transport_emission);

        initializeViews();
        setupSpinners();
        setupButtonListener();

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        FirebaseUser  currentUser  = auth.getCurrentUser ();

        if (currentUser  == null) {
            Toast.makeText(this, "User  not logged in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }

        // Initialize Firebase Storage reference
        storageReference = FirebaseStorage.getInstance().getReference("uploads").child(currentUser .getUid());

        //setupProgressDialog();
    }

    private void initializeViews() {
        inputWeight = findViewById(R.id.inputWeight);
        inputDistance = findViewById(R.id.inputDistance);
        weightUnitSpinner = findViewById(R.id.weightUnitSpinner);
        distanceUnitSpinner = findViewById(R.id.distanceUnitSpinner);
        transportMethodSpinner = findViewById(R.id.transportMethodSpinner);
        carbonG = findViewById(R.id.carbonG);
        carbonLb = findViewById(R.id.carbonLb);
        carbonKg = findViewById(R.id.carbonKg);
        carbonMt = findViewById(R.id.carbonMt);
        estimatedAt = findViewById(R.id.estimatedAt);
        resultCard = findViewById(R.id.resultCard);
        btnCalculate = findViewById(R.id.btnCalculate);
    }

    private void setupSpinners() {
        // Weight units spinner
        ArrayAdapter<CharSequence> weightAdapter = ArrayAdapter.createFromResource(this,
                R.array.weight_units, android.R.layout.simple_spinner_item);
        weightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        weightUnitSpinner.setAdapter(weightAdapter);
        weightUnitSpinner.setOnItemSelectedListener(this);

        // Distance units spinner
        ArrayAdapter<CharSequence> distanceAdapter = ArrayAdapter.createFromResource(this,
                R.array.distance_units, android.R.layout.simple_spinner_item);
        distanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        distanceUnitSpinner.setAdapter(distanceAdapter);
        distanceUnitSpinner.setOnItemSelectedListener(this);

        // Transport methods spinner
        ArrayAdapter<CharSequence> transportAdapter = ArrayAdapter.createFromResource(this,
                R.array.transport_methods, android.R.layout.simple_spinner_item);
        transportAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        transportMethodSpinner.setAdapter(transportAdapter);
        transportMethodSpinner.setOnItemSelectedListener(this);
    }

    private void setupButtonListener() {
        btnCalculate.setOnClickListener(v -> {
            String weightStr = inputWeight.getText().toString().trim();
            String distanceStr = inputDistance.getText().toString().trim();

            if (validateInputs(weightStr, distanceStr)) {
                try {
                    double weight = Double.parseDouble(weightStr);
                    double distance = Double.parseDouble(distanceStr);
                    if (validateUnits()) {
                        calculateShippingEmission(weight, distance);
                    }
                } catch (NumberFormatException e) {
                    showError("Invalid number format");
                }
            }
        });
    }

    private boolean validateInputs(String weight, String distance) {
        if (weight.isEmpty() || distance.isEmpty()) {
            showError("Please fill all fields");
            return false;
        }
        return true;
    }

    private boolean validateUnits() {
        if (!Arrays.asList("g", "lb", "kg", "mt").contains(selectedWeightUnit)) {
            showError("Invalid weight unit selected");
            return false;
        }
        if (!Arrays.asList("km", "mi").contains(selectedDistanceUnit)) {
            showError("Invalid distance unit selected");
            return false;
        }
        if (!Arrays.asList("truck", "ship", "train", "plane").contains(selectedTransportMethod)) {
            showError("Invalid transport method");
            return false;
        }
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selectedItem = parent.getItemAtPosition(position).toString();

        try {
            int spinnerId = parent.getId(); // Store spinner ID in a variable

            if (spinnerId == R.id.weightUnitSpinner) {
                selectedWeightUnit = extractUnitFromSpinner(selectedItem);
            } else if (spinnerId == R.id.distanceUnitSpinner) {
                selectedDistanceUnit = extractUnitFromSpinner(selectedItem);
            } else if (spinnerId == R.id.transportMethodSpinner) {
                selectedTransportMethod = selectedItem.toLowerCase();
            }
        } catch (Exception e) {
            showError("Invalid selection format");
        }
    }

    private String extractUnitFromSpinner(String text) throws Exception {
        int start = text.indexOf('(') + 1;
        int end = text.indexOf(')');
        if (start <= 0 || end <= 0 || start >= end) {
            throw new Exception("Invalid unit format");
        }
        return text.substring(start, end);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Default values already set
    }

    private void calculateShippingEmission(double weightValue, double distanceValue) {
        double weightInTons = convertWeightToTons(weightValue, selectedWeightUnit);
        double distanceInKm = convertDistanceToKm(distanceValue, selectedDistanceUnit);

        // Calculate emissions based on transport method
        double emissionFactor = getEmissionFactor(selectedTransportMethod);
        double carbonEmissions = weightInTons * distanceInKm * emissionFactor; // in kg CO2

        // Convert emissions to different units
        double carbonGValue = carbonEmissions * 1000; // kg to g
        double carbonLbValue = carbonEmissions * 2.20462; // kg to lb
        double carbonKgValue = carbonEmissions; // already in kg
        double carbonMtValue = carbonEmissions / 1000; // kg to metric tons

        // Assuming the estimated date is the current date for simplicity
        String estimatedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        // Update UI with calculated values
        updateUI(carbonGValue, carbonLbValue, carbonKgValue, carbonMtValue, estimatedDate);

        // Upload the calculated emissions to Firebase Storage
        uploadEmissionsToStorage(carbonGValue, carbonLbValue, carbonKgValue, carbonMtValue, estimatedDate);
    }

    private double convertWeightToTons(double weight, String unit) {
        switch (unit) {
            case "kg":
                return weight / 1000; // kg to tons
            case "g":
                return weight / 1000000; // g to tons
            case "lb":
                return weight * 0.000453592; // lb to tons
            case "mt":
                return weight; // already in tons
            default:
                return 0;
        }
    }

    private double convertDistanceToKm(double distance, String unit) {
        switch (unit) {
            case "km":
                return distance; // already in km
            case "mi":
                return distance * 1.60934; // miles to km
            default:
                return 0;
        }
    }

    private double getEmissionFactor(String transportMethod) {
        switch (transportMethod) {
            case "truck":
                return EMISSION_FACTOR_TRUCK;
            case "ship":
                return EMISSION_FACTOR_SHIP;
            case "train":
                return EMISSION_FACTOR_TRAIN;
            case "plane":
                return EMISSION_FACTOR_PLANE;
            default:
                return 0;
        }
    }

    private void uploadEmissionsToStorage(double carbonG, double carbonLb, double carbonKg, double carbonMt, String estimatedDate) {
        // Create a simple CSV file content
        String csvContent = String.format("Carbon (g),Carbon (lb),Carbon (kg),Carbon (mt),Estimated Date\n%.2f,%.2f,%.2f,%.2f,%s",
                carbonG, carbonLb, carbonKg, carbonMt, estimatedDate);

        // Create a temporary file to upload
        File file = new File(getFilesDir(), "emissions.csv");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(csvContent.getBytes());
            fos.flush();
        } catch (IOException e) {
            showError("Error creating file: " + e.getMessage());
            return;
        }

        // Upload the file to Firebase Storage
        Uri fileUri = Uri.fromFile(file);
        String userId = auth.getCurrentUser ().getUid();
        StorageReference fileReference = storageReference.child("emissions.csv");

        fileReference.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Toast.makeText(TransportEmission.this, "Emissions uploaded successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    showError("File upload failed: " + e.getMessage());
                });
    }

    private void updateUI(double g, double lb, double kg, double mt, String date) {
        resultCard.setVisibility(View.VISIBLE);
        carbonG.setText(String.format("Carbon (g): %.2f g CO₂", g));
        carbonLb.setText(String.format("Carbon (lb): %.2f lb CO₂", lb));
        carbonKg.setText(String.format("Carbon (kg): %.2f kg CO₂", kg));
        carbonMt.setText(String.format("Carbon (mt): %.2f mt CO₂", mt));
        estimatedAt.setText("Estimated At: " + date);
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            Toast.makeText(TransportEmission.this, message, Toast.LENGTH_LONG).show();
            resultCard.setVisibility(View.GONE);
        });
    }
}