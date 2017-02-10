package com.brine.discovery.model;

/**
 * Created by phamhai on 09/02/2017.
 */

public class Recommend {
    private String label;
    private String uri;
    private String image;

    public Recommend(String label, String uri, String image){
        this.label = label;
        this.uri = uri;
        if(image != null){
            this.image = image.replace("http://", "https://");
        }else{
            this.image = image;
        }
    }

    public String getLabel(){
        return label;
    }

    public String getUri(){
        return uri;
    }

    public String getImage(){
        return image;
    }
}
