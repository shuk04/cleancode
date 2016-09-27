package com.spr.dirtycode.movie;

public class Fun {

    public String getT() {
        return t;
    }

    public void setT(final String title) {
        this.t = title;
    }

    public String getR() {
        return r;
    }

    public void setR(final String rating) {
        this.r = rating;
    }

    public String getTa() {
        return ta;
    }

    public void setTa(final String tagline) {
        this.ta = tagline;
    }

    private String t;

    private String r;

    private String ta;

    private int i;

    @Override
    public String toString() {
        return "Movie [title=" + t + ", rating=" + r + ", tagline=" + ta + ", status=" + i + "]";
    }

    public int getI() {
        return i;
    }

    public void setI(final int i) {
        this.i = i;
    }
}
