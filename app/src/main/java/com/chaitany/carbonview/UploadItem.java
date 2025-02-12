package com.chaitany.carbonview;



public class UploadItem {
    private String fileName;
    private String date;

    public UploadItem(String fileName, String date) {
        this.fileName = fileName;
        this.date = date;
    }

    public String getFileName() {
        return fileName;
    }

    public String getDate() {
        return date;
    }
}