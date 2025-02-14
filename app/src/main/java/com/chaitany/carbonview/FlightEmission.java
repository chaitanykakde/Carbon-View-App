package com.chaitany.carbonview;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class FlightEmission extends AppCompatActivity {

    private EditText departureAirport, destinationAirport, passengerCount;
    private MaterialButton calculateButton;
    private MaterialCardView resultCard;
    private TextView estimatedAt, carbonG, carbonLb, carbonKg, carbonMt, distanceUnit;

    private final OkHttpClient client = new OkHttpClient(); // OkHttp Client

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_emission);

        departureAirport = findViewById(R.id.departureAirport);
        destinationAirport = findViewById(R.id.destinationAirport);
        passengerCount = findViewById(R.id.passengerCount);
        calculateButton = findViewById(R.id.calculateButton);
        resultCard = findViewById(R.id.resultCard);
        estimatedAt = findViewById(R.id.estimatedAt);
        carbonG = findViewById(R.id.carbonG);
        carbonKg=findViewById(R.id.carbonKg2);
        carbonLb = findViewById(R.id.carbonLb);
        carbonMt = findViewById(R.id.carbonMt);
        distanceUnit = findViewById(R.id.distanceUnit);
        calculateButton.setOnClickListener(v -> calculateEmission());
    }

    private void calculateEmission() {
        String departure = departureAirport.getText().toString().trim().toUpperCase();
        String destination = destinationAirport.getText().toString().trim().toUpperCase();
        String passengers = passengerCount.getText().toString().trim();

        if (departure.isEmpty() || destination.isEmpty() || passengers.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int passengerNum = Integer.parseInt(passengers);

        // Prepare JSON Request Body
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("type", "flight");
            requestBody.put("passengers", passengerNum);

            JSONArray legsArray = new JSONArray();
            JSONObject leg = new JSONObject();
            leg.put("departure_airport", departure);
            leg.put("destination_airport", destination);
            legsArray.put(leg);

            requestBody.put("legs", legsArray);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        String url = "https://www.carboninterface.com/api/v1/estimates";
        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        RequestBody body = RequestBody.create(requestBody.toString(), JSON);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", "Bearer EEjDUmc5BvD0n5ibNojQ") // Replace with your API key
                .addHeader("Content-Type", "application/json")
                .build();

        // Execute API Call
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(FlightEmission.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(FlightEmission.this, "Failed to fetch data", Toast.LENGTH_SHORT).show());
                    return;
                }

                try {
                    String responseData = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseData);
                    JSONObject attributes = jsonResponse.getJSONObject("data").getJSONObject("attributes");
                    String estimatedAtValue = attributes.getString("estimated_at");
                    int carbonGValue = attributes.getInt("carbon_g");
                    int carbonLbValue = attributes.getInt("carbon_lb");
                    double carbonKgValue = attributes.getDouble("carbon_kg");
                    int carbonMtValue = attributes.getInt("carbon_mt");
                    String distanceUnitValue = attributes.getString("distance_unit");
                    double distanceValue = attributes.getDouble("distance_value");
                    runOnUiThread(() -> {

                        estimatedAt.setText("Estimated At: " + estimatedAtValue);
                        carbonG.setText("Carbon Emission: " + carbonGValue + " g");
                        carbonLb.setText("Carbon Emission: " + carbonLbValue + " lb");
                        carbonKg.setText("Carbon Emission: " + carbonKgValue + " kg");
                        carbonMt.setText("Carbon Emission: " + carbonMtValue + " metric tons");
                        distanceUnit.setText("Distance: " + distanceValue + " " + distanceUnitValue);
                        resultCard.setVisibility(View.VISIBLE);
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
