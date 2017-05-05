package com.brine.discovery.model;


/**
 * Created by phamhai on 09/02/2017.
 */

public class Recommend {
    private String label;
    private String description;
    private String uri;
    private String image;
    private float threshold;

    public Recommend(String label, String description, String uri, String image){
        this.label = label;
        this.description = description;
        if(uri.contains("http")){
            this.uri = uri;
        }else{
            this.uri = "http://dbpedia.org/resource/" + uri;
        }
        if(image != null){
            this.image = image.replace("http://", "https://");
        }else{
            this.image = image;
        }
    }

    public Recommend(String label, String description, String uri, String image, float threshold){
        this.label = label;
        this.description = description;
        if(uri.contains("http")){
            this.uri = uri;
        }else{
            this.uri = "http://dbpedia.org/resource/" + uri;
        }
        if(image != null){
            this.image = image.replace("http://", "https://");
        }else{
            this.image = image;
        }
        this.threshold = threshold;
    }

    public String getLabel(){
        return label;
    }

    public String getDescription() {
        return description;
    }

    public String getUri(){
        return uri;
    }

    public String getImage(){
        return image;
    }

    public float getThreshold(){
        return threshold;
    }
}
