package com.example.documentslocker;

public class Document {
    private String name;
    private String url;

    public Document(String _name, String _url){
        this.name = _name;
        this.url = _url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
