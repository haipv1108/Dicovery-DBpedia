package com.brine.discovery.model;

/**
 * Created by phamhai on 13/02/2017.
 */

public class FSResult {
    private String uri;
    private String label;
    private String description;
    private String thumbnail;

    public FSResult(String uri, String label, String description, String thumbnail){
        this.uri = uri;
        this.label = label;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public String getLabel(){
        return label;
    }

    public String getUri(){
        return uri;
    }

    public String getDescription(){
        return description;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumb){
        this.thumbnail = thumb.replace("http://", "https://");
    }
}
