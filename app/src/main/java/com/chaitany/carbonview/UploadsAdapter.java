package com.chaitany.carbonview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class UploadsAdapter extends RecyclerView.Adapter<UploadsAdapter.ViewHolder> {

    private final Context context;
    private final List<UploadItem> uploadList;

    public UploadsAdapter(Context context, List<UploadItem> uploadList) {
        this.context = context;
        this.uploadList = uploadList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_item_recent_uploads, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UploadItem uploadItem = uploadList.get(position);

        holder.fileNameText.setText(uploadItem.getFileName());
        holder.uploadDateText.setText("Uploaded on: " + uploadItem.getDate());

        // Analyze button click event
        holder.analyzeButton.setOnClickListener(v -> {
            Toast.makeText(context, "Analyzing " + uploadItem.getFileName(), Toast.LENGTH_SHORT).show();
            // TODO: Implement file analysis logic here
        });

        // Delete button click event
        holder.deleteButton.setOnClickListener(v -> deleteFile(uploadItem, position));
    }

    @Override
    public int getItemCount() {
        return uploadList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView fileNameText, uploadDateText;
        MaterialButton analyzeButton, deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileNameText = itemView.findViewById(R.id.fileNameText);
            uploadDateText = itemView.findViewById(R.id.dateText);
            analyzeButton = itemView.findViewById(R.id.analyzeButton);
            deleteButton = itemView.findViewById(R.id.deletebutton);
        }
    }

    private void deleteFile(UploadItem uploadItem, int position) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users")
                .child(uploadItem.getMobile())
                .child("uploads");

        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(uploadItem.getFileUrl());

        storageReference.delete().addOnSuccessListener(aVoid -> {
            databaseReference.child(uploadItem.getFileName()).removeValue().addOnSuccessListener(aVoid1 -> {
                uploadList.remove(position);
                notifyItemRemoved(position);
                Toast.makeText(context, "File deleted", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> Toast.makeText(context, "Failed to delete from database", Toast.LENGTH_SHORT).show());
        }).addOnFailureListener(e -> Toast.makeText(context, "Failed to delete file", Toast.LENGTH_SHORT).show());
    }
}
