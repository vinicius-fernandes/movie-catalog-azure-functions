package com.vinfern.functions;


public class MovieRequest {
    private String id  ;
    private String title;
    private String year;
    private String video;
    private String thumbnail;

    public MovieRequest(String title, String year, String video, String thumbnail) {
        this.title = title;
        this.year = year;
        this.video = video;
        this.thumbnail = thumbnail;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
}