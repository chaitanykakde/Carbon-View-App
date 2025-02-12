package com.chaitany.carbonview;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser ;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddData extends AppCompatActivity {
    private static final int FILE_SELECT_CODE = 1;

    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private FirebaseAuth auth;

    private Dialog progressDialog;
    private MaterialTextView progressText;

    private RecyclerView uploadsRecyclerView;
    private UploadsAdapter uploadAdapter;
    private List<UploadItem> uploadList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_data);

        uploadsRecyclerView = findViewById(R.id.uploadsRecyclerView);
        uploadsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize uploadList before setting adapter
        uploadList = new ArrayList<>();
        uploadAdapter = new UploadsAdapter(this, uploadList);
        uploadsRecyclerView.setAdapter(uploadAdapter);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        FirebaseUser  currentUser  = auth.getCurrentUser ();

        if (currentUser  == null) {
            Toast.makeText(this, "User  not logged in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }

        String userId = currentUser .getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("uploads");
        storageReference = FirebaseStorage.getInstance().getReference("uploads").child(userId);

        ImageButton backButton = findViewById(R.id.backButton);
        MaterialCardView fileUploadCard = findViewById(R.id.fileUploadCard);

        backButton.setOnClickListener(v -> onBackPressed());
        fileUploadCard.setOnClickListener(v -> openFileChooser());

        setupProgressDialog();

        // Call after initializing adapter
        loadUploadedFiles();
    }

    private void setupProgressDialog() {
        progressDialog = new Dialog(this);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setContentView(R.layout.dialog_upload_progress);
        progressDialog.setCancelable(false);
        progressText = progressDialog.findViewById(R.id.progressText);
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, FILE_SELECT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_SELECT_CODE && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                uploadFileToFirebase(fileUri);
            }
        }
    }

    private void uploadFileToFirebase(Uri fileUri) {
        String fileName = System.currentTimeMillis() + "_" + fileUri.getLastPathSegment();
        StorageReference fileRef = storageReference.child(fileName);

        StorageMetadata metadata = new StorageMetadata.Builder()
                .setCustomMetadata("userId", auth.getCurrentUser ().getUid())  // Store user ID in metadata
                .build();

        progressDialog.show();

        fileRef.putFile(fileUri, metadata)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String fileUrl = uri.toString();
                    String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

                    // Save metadata in Firebase Realtime Database
                    UploadItem uploadItem = new UploadItem(fileName, date, fileUrl, auth.getCurrentUser ().getUid());
                    databaseReference.push().setValue(uploadItem)
                            .addOnSuccessListener(aVoid -> {
                                progressDialog.dismiss();
                                Toast.makeText(this, "File uploaded successfully", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Database update failed", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            });

                })).addOnFailureListener(e -> {
                    Toast.makeText(this, "File upload failed", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                });
    }

    private void loadUploadedFiles() {
        // Fetching file data from Firebase Realtime Database
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                uploadList.clear();
                if (!snapshot.exists()) {
                    Toast.makeText(AddData.this, "No files found", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    UploadItem uploadItem = dataSnapshot.getValue(UploadItem.class);
                    if (uploadItem != null) {
                        uploadList.add(uploadItem);
                    }
                }
                uploadAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddData.this, "Failed to load uploads", Toast.LENGTH_SHORT).show();
            }
        });
    }
}