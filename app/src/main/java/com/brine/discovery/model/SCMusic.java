package com.brine.discovery.model;

/**
 * Created by phamhai on 13/02/2017.
 */

public class SCMusic {
    private long musicId;
    private String title;
    private String streamUrl;
    private String artworkUrl;

    public SCMusic(long musicId, String title, String streamUrl, String artworkUrl){
        this.musicId = musicId;
        this.title = title;
        this.streamUrl = streamUrl;
        this.artworkUrl = artworkUrl;
    }

    public long getMusicId() {
        return musicId;
    }

    public String getTitle() {
        return title;
    }

    public String getStreamUrl() {
        return streamUrl;
    }

    public String getArtworkUrl() {
        return artworkUrl;
    }
}
