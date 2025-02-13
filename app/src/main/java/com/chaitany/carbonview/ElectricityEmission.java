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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ElectricityEmission extends AppCompatActivity {

    private EditText inputElectricity;
    private TextView electricityResult;
    private CardView resultCard;
    private Button btnCalculate;

    private static final String API_URL = "https://api.carboninterface.com/v1/estimates"; // Replace with actual endpoint
    private static final String API_KEY = "EEjDUmc5BvD0n5ibNojQ"; // Replace with actual API key

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_electricity_emission);

        inputElectricity = findViewById(R.id.inputElectricity);
        electricityResult = findViewById(R.id.electricityResult);
        resultCard = findViewById(R.id.resultCard);
        btnCalculate = findViewById(R.id.btnCalculate);

        btnCalculate.setOnClickListener(v -> {
            if (!inputElectricity.getText().toString().isEmpty()) {
                double energyUsed = Double.parseDouble(inputElectricity.getText().toString());
                getElectricityEmission(energyUsed);
            } else {
                Toast.makeText(this, "Please enter electricity consumption", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getElectricityEmission(double energyUsed) {
        OkHttpClient client = new OkHttpClient();

        String jsonBody = "{ \"type\": \"electricity\", \"electricity_unit\": \"kwh\", \"electricity_value\": " + energyUsed + "}";

        Request request = new Request.Builder()
                .url(API_URL)
                .post(okhttp3.RequestBody.create(jsonBody, okhttp3.MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(ElectricityEmission.this, "API Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("API Error", "Error: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();
                Log.d("API Response", responseData);

                try {
                    JSONObject jsonResponse = new JSONObject(responseData);

                    if (jsonResponse.has("data")) {
                        JSONObject data = jsonResponse.getJSONObject("data");

                        if (data.has("attributes")) {
                            JSONObject attributes = data.getJSONObject("attributes");

                            if (attributes.has("carbon_kg")) {
                                double carbonKg = attributes.getDouble("carbon_kg");

                                runOnUiThread(() -> {
                                    resultCard.setVisibility(View.VISIBLE);
                                    electricityResult.setText("Electricity Emission: " + carbonKg + " kg CO₂");
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
        });
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            Toast.makeText(ElectricityEmission.this, message, Toast.LENGTH_LONG).show();
            Log.e("API Error", message);
        });
    }
}
