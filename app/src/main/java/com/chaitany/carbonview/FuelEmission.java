package com.chaitany.carbonview;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class FuelEmission extends AppCompatActivity {

    private EditText inputFuelAmount;
    private Spinner fuelTypeSpinner;
    private TextView carbonG, carbonLb, carbonKg, carbonMt, estimatedAt;
    private CardView resultCard;
    private Button btnCalculate;

    // Predefined emission factors (in kg CO2 per liter)
    private static final double EMISSION_FACTOR_PETROL = 2.31; // kg CO2 per liter
    private static final double EMISSION_FACTOR_DIESEL = 2.68; // kg CO2 per liter
    private static final double EMISSION_FACTOR_GAS = 1.96; // kg CO2 per liter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel_emission);

        inputFuelAmount = findViewById(R.id.inputFuelAmount);
        fuelTypeSpinner = findViewById(R.id.fuelTypeSpinner);
        carbonG = findViewById(R.id.carbonG);
        carbonLb = findViewById(R.id.carbonLb);
        carbonKg = findViewById(R.id.carbonKg);
        carbonMt = findViewById(R.id.carbonMt);
        estimatedAt = findViewById(R.id.estimatedAt);
        resultCard = findViewById(R.id.resultCard);
        btnCalculate = findViewById(R.id.btnCalculate);

        // Populate the fuel type spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.fuel_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fuelTypeSpinner.setAdapter(adapter);

        btnCalculate.setOnClickListener(v -> {
            String fuelAmountStr = inputFuelAmount.getText().toString();
            if (!fuelAmountStr.isEmpty()) {
                try {
                    double fuelAmount = Double.parseDouble(fuelAmountStr);
                    calculateFuelEmission(fuelAmount);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid input. Enter a valid number.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please enter fuel amount", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateFuelEmission(double fuelAmount) {
        String selectedFuelType = fuelTypeSpinner.getSelectedItem().toString();
        double emissionFactor;

        switch (selectedFuelType) {
            case "Petrol":
                emissionFactor = EMISSION_FACTOR_PETROL;
                break;
            case "Diesel":
                emissionFactor = EMISSION_FACTOR_DIESEL;
                break;
            case "Gas":
                emissionFactor = EMISSION_FACTOR_GAS;
                break;
            default:
                showError("Invalid fuel type selected");
                return;
        }

        // Calculate emissions
        double carbonEmissions = fuelAmount * emissionFactor; // in kg CO2

        // Convert emissions to different units
        double carbonGValue = carbonEmissions * 1000; // kg to g
        double carbonLbValue = carbonEmissions * 2.20462; // kg to lb
        double carbonKgValue = carbonEmissions; // already in kg
        double carbonMtValue = carbonEmissions / 1000; // kg to metric tons

        // Assuming the estimated date is the current date for simplicity
        String estimatedDate = java.time.LocalDate.now().toString();

        // Update UI with calculated values
        updateUI(carbonGValue, carbonLbValue, carbonKgValue, carbonMtValue, estimatedDate);
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
            Toast.makeText(FuelEmission.this, message, Toast.LENGTH_LONG).show();
            resultCard.setVisibility(View.GONE);
        });
    }
}