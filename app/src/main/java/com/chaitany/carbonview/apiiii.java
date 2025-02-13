package com.chaitany.carbonview;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import okhttp3.*;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

public class apiiii extends AppCompatActivity {

    private CarbonAPIHelper carbonAPIHelper;
    private TextView electricityResult, fuelResult, flightResult;
    private CardView resultCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_insights); // Ensure the correct layout is set

        carbonAPIHelper = new CarbonAPIHelper();

        EditText inputElectricity = findViewById(R.id.inputElectricity);
        EditText inputFuel = findViewById(R.id.inputFuel);
        EditText inputFlightDeparture = findViewById(R.id.inputFlightDeparture);
        EditText inputFlightDestination = findViewById(R.id.inputFlightDestination);

        Button btnCalculateElectricity = findViewById(R.id.btnCalculateElectricity);
        Button btnCalculateFuel = findViewById(R.id.btnCalculateFuel);
        Button btnCalculateFlight = findViewById(R.id.btnCalculateFlight);

        resultCard = findViewById(R.id.resultCard);
        electricityResult = findViewById(R.id.electricityResult);
        fuelResult = findViewById(R.id.fuelResult);
        flightResult = findViewById(R.id.flightResult);

        btnCalculateElectricity.setOnClickListener(v -> {
            if (!inputElectricity.getText().toString().isEmpty()) {
                double energyUsed = Double.parseDouble(inputElectricity.getText().toString());
                carbonAPIHelper.getElectricityEmission(energyUsed, getCallback("electricity"));
            } else {
                Toast.makeText(this, "Please enter electricity usage", Toast.LENGTH_SHORT).show();
            }
        });

        btnCalculateFuel.setOnClickListener(v -> {
            if (!inputFuel.getText().toString().isEmpty()) {
                double fuelUsed = Double.parseDouble(inputFuel.getText().toString());
                carbonAPIHelper.getFuelEmission("diesel", fuelUsed, getCallback("fuel"));
            } else {
                Toast.makeText(this, "Please enter fuel usage", Toast.LENGTH_SHORT).show();
            }
        });

        btnCalculateFlight.setOnClickListener(v -> {
            if (!inputFlightDeparture.getText().toString().isEmpty() && !inputFlightDestination.getText().toString().isEmpty()) {
                carbonAPIHelper.getFlightEmission(inputFlightDeparture.getText().toString(), inputFlightDestination.getText().toString(), getCallback("flight"));
            } else {
                Toast.makeText(this, "Please enter both departure and destination airports", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Callback getCallback(String type) {
        return new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(apiiii.this, "API Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("API Error", "Error: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();
                Log.d("API Response", responseData);

                try {
                    JSONObject jsonResponse = new JSONObject(responseData);

                    // Ensure response structure matches
                    if (jsonResponse.has("data")) {
                        JSONObject data = jsonResponse.getJSONObject("data");

                        if (data.has("attributes")) {
                            JSONObject attributes = data.getJSONObject("attributes");

                            if (attributes.has("carbon_kg")) {
                                double carbonKg = attributes.getDouble("carbon_kg");

                                runOnUiThread(() -> {
                                    resultCard.setVisibility(View.VISIBLE);
                                    if (type.equals("electricity")) {
                                        electricityResult.setText("Electricity Emission: " + carbonKg + " kg CO₂");
                                    } else if (type.equals("fuel")) {
                                        fuelResult.setText("Fuel Emission: " + carbonKg + " kg CO₂");
                                    } else if (type.equals("flight")) {
                                        flightResult.setText("Flight Emission: " + carbonKg + " kg CO₂");
                                    }
                                });
                            } else {
                                showError("Missing carbon_kg in response");
                            }
                        } else {
                            showError("Missing attributes in response");
                        }
                    } else {
                        showError("Invalid API response structure");
                    }

                } catch (JSONException e) {
                    showError("JSON Parsing Error: " + e.getMessage());
                }
            }
        };
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            Toast.makeText(apiiii.this, message, Toast.LENGTH_LONG).show();
            Log.e("API Error", message);
        });
    }
}
