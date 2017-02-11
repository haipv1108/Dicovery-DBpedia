package com.brine.discovery.model;

/**
 * Created by phamhai on 11/02/2017.
 */

public class TypeUri {
    private String uri;
    private String label;

    public TypeUri(String label, String uri){
        this.uri = uri;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public String getUri() {
        return uri;
    }
}
