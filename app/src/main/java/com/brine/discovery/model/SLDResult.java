package com.brine.discovery.model;

/**
 * Created by phamhai on 19/03/2017.
 */

public class SLDResult {
    private String uri;
    private String label;
    private String description;
    private String thumbnail;

    public SLDResult(String _uri, String _label, String _description, String _thumbnail){
        this.uri = _uri;
        this.label = _label;
        this.description = _description;
        this.thumbnail = _thumbnail;
    }

    public String getUri() {
        return uri;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnail() {
        return thumbnail;
    }
}
