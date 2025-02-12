package com.chaitany.carbonview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.material.button.MaterialButton;

import java.util.List;

public class UploadsAdapter extends RecyclerView.Adapter<UploadsAdapter.UploadViewHolder> {
    private List<UploadItem> uploads;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onAnalyzeClick(int position);
        void onDeleteClick(int position);
    }

    public UploadsAdapter(List<UploadItem> uploads, OnItemClickListener listener) {
        this.uploads = uploads;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UploadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_item_recent_uploads, parent, false);
        return new UploadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UploadViewHolder holder, int position) {
        UploadItem item = uploads.get(position);
        holder.fileNameText.setText(item.getFileName());
        holder.dateText.setText(item.getDate());
    }

    @Override
    public int getItemCount() {
        return uploads.size();
    }

    class UploadViewHolder extends RecyclerView.ViewHolder {
        TextView fileNameText;
        TextView dateText;
        MaterialButton analyzeButton;
        ImageButton deleteButton;

        UploadViewHolder(View itemView) {
            super(itemView);
            fileNameText = itemView.findViewById(R.id.fileNameText);
            dateText = itemView.findViewById(R.id.dateText);
            analyzeButton = itemView.findViewById(R.id.analyzeButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);

            analyzeButton.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onAnalyzeClick(position);
                    }
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onDeleteClick(position);
                    }
                }
            });
        }
    }
}