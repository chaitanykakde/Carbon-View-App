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
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ElectricityEmission extends AppCompatActivity {

    private EditText inputElectricity;
    private TextView electricityResult, carbonG, carbonLb, carbonKg, carbonMt, estimatedAt;
    private CardView resultCard;
    private Button btnCalculate;

    private static final String API_URL = "https://www.carboninterface.com/api/v1/estimates";
    private static final String API_KEY = "EEjDUmc5BvD0n5ibNojQ"; // Replace with actual API key
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_electricity_emission);

        inputElectricity = findViewById(R.id.inputElectricity);
        electricityResult = findViewById(R.id.electricityResult);
        carbonG = findViewById(R.id.carbonG);
        carbonLb = findViewById(R.id.carbonLb);
        carbonKg = findViewById(R.id.carbonKg);
        carbonMt = findViewById(R.id.carbonMt);
        estimatedAt = findViewById(R.id.estimatedAt);
        resultCard = findViewById(R.id.resultCard);
        btnCalculate = findViewById(R.id.btnCalculate);

        btnCalculate.setOnClickListener(v -> {
            String input = inputElectricity.getText().toString();
            if (!input.isEmpty()) {
                try {
                    double energyUsed = Double.parseDouble(input);
                    getElectricityEmission(energyUsed);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid input. Enter a valid number.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please enter electricity consumption", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getElectricityEmission(double energyUsed) {
        OkHttpClient client = new OkHttpClient();

        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("type", "electricity");
            jsonRequest.put("electricity_unit", "kwh");
            jsonRequest.put("electricity_value", energyUsed);
            jsonRequest.put("country", "US");
        } catch (JSONException e) {
            showError("JSON Creation Error: " + e.getMessage());
            return;
        }

        RequestBody requestBody = RequestBody.create(jsonRequest.toString(), JSON);

        Request request = new Request.Builder()
                .url(API_URL)
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showError("API Error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    showError("API Response Error: " + response.message());
                    return;
                }

                final String responseData = response.body().string();
                Log.d("API Response", responseData);

                try {
                    JSONObject jsonResponse = new JSONObject(responseData);
                    JSONObject data = jsonResponse.optJSONObject("data");

                    if (data != null) {
                        JSONObject attributes = data.optJSONObject("attributes");
                        if (attributes != null) {
                            double carbonGValue = attributes.optDouble("carbon_g", -1);
                            double carbonLbValue = attributes.optDouble("carbon_lb", -1);
                            double carbonKgValue = attributes.optDouble("carbon_kg", -1);
                            double carbonMtValue = attributes.optDouble("carbon_mt", -1);
                            String estimatedDate = attributes.optString("estimated_at", "N/A");

                            if (carbonGValue >= 0 && carbonLbValue >= 0 && carbonKgValue >= 0 && carbonMtValue >= 0) {
                                runOnUiThread(() -> {
                                    resultCard.setVisibility(View.VISIBLE);
                                    carbonG.setText("Carbon (g): " + carbonGValue + " g CO₂");
                                    carbonLb.setText("Carbon (lb): " + carbonLbValue + " lb CO₂");
                                    carbonKg.setText("Carbon (kg): " + carbonKgValue + " kg CO₂");
                                    carbonMt.setText("Carbon (mt): " + carbonMtValue + " mt CO₂");
                                    estimatedAt.setText("Estimated At: " + estimatedDate);
                                });
                            } else {
                                showError("Invalid carbon emission data received");
                            }

                        } else {
                            showError("Invalid carbon emission data");
                        }
                    } else {
                        showError("Missing attributes in response");
                    }
                } catch (JSONException e) {
                    showError("JSON Parsing Error: " + e.getMessage());
                }
            }
        });
    }

    private void showError(String message) {
        runOnUiThread(() -> Toast.makeText(ElectricityEmission.this, message, Toast.LENGTH_LONG).show());
    }
}
