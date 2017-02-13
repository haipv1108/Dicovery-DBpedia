package com.brine.discovery.model;

/**
 * Created by phamhai on 13/02/2017.
 */

public class YoutubeVideo {
    private String videoId;
    private String title;
    private String thumbnail;
    private String channelTitle;

    public YoutubeVideo(String videoId, String title, String thumbnail, String channelTitle){
        this.videoId = videoId;
        this.title = title;
        this.thumbnail = thumbnail;
        this.channelTitle = channelTitle;
    }

    public String getVideoId() {
        return videoId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnail() {
        return thumbnail;
    }
}
