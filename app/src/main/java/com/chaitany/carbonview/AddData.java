package com.chaitany.carbonview;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class AddData extends AppCompatActivity implements UploadsAdapter.OnItemClickListener {
    private RecyclerView uploadsRecyclerView;
    private UploadsAdapter adapter;
    private List<UploadItem> uploads;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_data);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ImageButton backButton = findViewById(R.id.backButton);
        MaterialCardView manualEntryCard = findViewById(R.id.manualEntryCard);
        MaterialCardView fileUploadCard = findViewById(R.id.fileUploadCard);
        uploadsRecyclerView = findViewById(R.id.uploadsRecyclerView);


        backButton.setOnClickListener(v -> onBackPressed());
        manualEntryCard.setOnClickListener(v -> handleManualEntry());
        fileUploadCard.setOnClickListener(v -> handleFileUpload());

        // Set up RecyclerView
        setupRecyclerView();
    }
    private void setupRecyclerView() {
        uploads = new ArrayList<>();
        // Add sample data
        for (int i = 0; i < 7; i++) {
            uploads.add(new UploadItem("March_Electricity.csv", "17/01/2025"));
        }

        adapter = new UploadsAdapter(uploads, this);
        uploadsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        uploadsRecyclerView.setAdapter(adapter);
    }

    private void handleManualEntry() {
        // TODO: Implement manual entry
        Toast.makeText(this, "Manual Entry clicked", Toast.LENGTH_SHORT).show();
    }

    private void handleFileUpload() {
        // TODO: Implement file upload
        Toast.makeText(this, "File Upload clicked", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAnalyzeClick(int position) {
        // TODO: Implement analysis
        Toast.makeText(this, "Analyzing " + uploads.get(position).getFileName(),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClick(int position) {
        // TODO: Implement delete
        uploads.remove(position);
        adapter.notifyItemRemoved(position);
        Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show();
    }
}








