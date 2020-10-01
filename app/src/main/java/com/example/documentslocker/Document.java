package com.example.documentslocker;

public class Document {
    private String name;
    private String url;
    private int code;

    public Document(){

    }

    public Document(String _name, String _url, int _code){
        this.name = _name;
        this.url = _url;
        this.code = _code;
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
}
