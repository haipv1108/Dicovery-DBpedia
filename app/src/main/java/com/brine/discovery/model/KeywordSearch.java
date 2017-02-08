package com.brine.discovery.model;

/**
 * Created by phamhai on 08/02/2017.
 */

public class KeywordSearch {
    private String label;
    private String uri;
    private String description;
    private String thumb;
    private String type;

    public KeywordSearch(String label, String uri, String description, String thumb, String type){
        this.label = label;
        this.uri = uri;
        this.description = description;
        this.thumb = thumb;
        this.type = type;
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

    public void setThumb(String thumb){
        this.thumb = thumb;
    }

    public String getThumb(){
        return thumb;
    }

    public String getType(){
        return type;
    }
}
