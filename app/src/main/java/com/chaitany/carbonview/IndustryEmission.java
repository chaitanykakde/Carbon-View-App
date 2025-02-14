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

public class IndustryEmission extends AppCompatActivity {

    // UI components
    private EditText inputProductionAmount; // Input field for production amount
    private Spinner industryTypeSpinner; // Spinner for selecting industry type
    private TextView carbonG, carbonLb, carbonKg, carbonMt, estimatedAt; // TextViews for displaying results
    private CardView resultCard; // CardView to show results
    private Button btnCalculate; // Button to trigger calculation

    // Predefined emission factors (in kg CO2 per unit of production)
    private static final double EMISSION_FACTOR_MANUFACTURING = 1.5; // kg CO2 per unit for manufacturing
    private static final double EMISSION_FACTOR_MINING = 2.0; // kg CO2 per unit for mining
    private static final double EMISSION_FACTOR_CONSTRUCTION = 1.8; // kg CO2 per unit for construction

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_industry_emission); // Set the content view to the XML layout

        // Initialize UI components
        inputProductionAmount = findViewById(R.id.inputProductionAmount);
        industryTypeSpinner = findViewById(R.id.industryTypeSpinner);
        carbonG = findViewById(R.id.carbonG);
        carbonLb = findViewById(R.id.carbonLb);
        carbonKg = findViewById(R.id.carbonKg);
        carbonMt = findViewById(R.id.carbonMt);
        estimatedAt = findViewById(R.id.estimatedAt);
        resultCard = findViewById(R.id.resultCard);
        btnCalculate = findViewById(R.id.btnCalculate);

        // Populate the industry type spinner with predefined options
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.industry_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        industryTypeSpinner.setAdapter(adapter);

        // Set up the button click listener
        btnCalculate.setOnClickListener(v -> {
            String productionAmountStr = inputProductionAmount.getText().toString();
            if (!productionAmountStr.isEmpty()) {
                try {
                    double productionAmount = Double.parseDouble(productionAmountStr);
                    calculateIndustryEmission(productionAmount); // Call the calculation method
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid input. Enter a valid number.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please enter production amount", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateIndustryEmission(double productionAmount) {
        // Get the selected industry type from the spinner
        String selectedIndustryType = industryTypeSpinner.getSelectedItem().toString();
        double emissionFactor;

        // Determine the emission factor based on the selected industry type
        switch (selectedIndustryType) {
            case "Manufacturing":
                emissionFactor = EMISSION_FACTOR_MANUFACTURING;
                break;
            case "Mining":
                emissionFactor = EMISSION_FACTOR_MINING;
                break;
            case "Construction":
                emissionFactor = EMISSION_FACTOR_CONSTRUCTION;
                break;
            default:
                showError("Invalid industry type selected");
                return;
        }

        // Calculate emissions based on production amount and emission factor
        double carbonEmissions = productionAmount * emissionFactor; // in kg CO2

        // Convert emissions to different units
        double carbonGValue = carbonEmissions * 1000; // kg to g
        double carbonLbValue = carbonEmissions * 2.20462; // kg to lb
        double carbonKgValue = carbonEmissions; // already in kg
        double carbonMtValue = carbonEmissions / 1000; // kg to metric tons

        // Get the current date for the estimated date
        String estimatedDate = java.time.LocalDate.now().toString();

        // Update the UI with the calculated values
        updateUI(carbonGValue, carbonLbValue, carbonKgValue, carbonMtValue, estimatedDate);
    }

    private void updateUI(double g, double lb, double kg, double mt, String date) {
        // Make the result card visible and update the text views with calculated values
        resultCard.setVisibility(View.VISIBLE);
        carbonG.setText(String.format("Carbon (g): %.2f g CO₂", g));
        carbonLb.setText(String.format("Carbon (lb): %.2f lb CO₂", lb));
        carbonKg.setText(String.format("Carbon (kg): %.2f kg CO₂", kg));
        carbonMt.setText(String.format("Carbon (mt): %.2f mt CO₂", mt));
        estimatedAt.setText("Estimated At: " + date);
    }

    private void showError(String message) {
        // Show an error message and hide the result card
        runOnUiThread(() -> {
            Toast.makeText(IndustryEmission.this, message, Toast.LENGTH_LONG).show();
            resultCard.setVisibility(View.GONE);
        });
    }
}