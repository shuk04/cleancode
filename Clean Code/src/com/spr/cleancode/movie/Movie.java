package com.spr.cleancode.movie;

public class Movie {

    private String rating;

    private int status;

    private String tagline;

    private String title;

    public String getRating() {
        return rating;
    }

    public int getStatus() {
        return status;
    }

    public String getTagline() {
        return tagline;
    }

    public String getTitle() {
        return title;
    }

    public void setRating(final String rating) {
        this.rating = rating;
    }

    public void setStatus(final int status) {
        this.status = status;
    }

    public void setTagline(final String tagline) {
        this.tagline = tagline;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

}
