package com.example.documentslocker;

public class Document {
    private String name;
    private String url;
    private int code;
    private String storageFileName;

    public Document(){

    }

    public Document(String _name, String _url, int _code, String _storageName){
        this.name = _name;
        this.url = _url;
        this.code = _code;
        this.storageFileName = _storageName;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public int getCode() {
        return code;
    }

    public String getStorageFileName() {
        return storageFileName;
    }
}
