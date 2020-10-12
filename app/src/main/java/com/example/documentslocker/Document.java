package com.example.documentslocker;

public class Document {
    private String name;
    private String url;
    private String storageFileName;
    private int isFavourite;

    public Document(){

    }

    public Document(String _name, String _url, String _storageName){
        this.name = _name;
        this.url = _url;
        this.storageFileName = _storageName;
        this.isFavourite = 0;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getStorageFileName() {
        return storageFileName;
    }

    public int getIsFavourite() {
        return isFavourite;
    }
}
