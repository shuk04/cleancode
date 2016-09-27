package com.spr.dirtycode.badcomments;

import java.util.ArrayList;
import java.util.List;

public class BadComments {

    /**
     * is class closed?
     */
    private boolean closed;

    // Utility method that returns when this.closed is true. Throws an exception
    // if the timeout is reached.
    public synchronized void waitForClose(final long timeoutMillis) throws Exception {
        if (!closed) {
            wait(timeoutMillis);
            if (!closed) {
                throw new Exception("MockResponseSender could not be closed");
            }
        }
    }

    /**
     * @param title
     *            The title of the CD
     * @param author
     *            The author of the CD
     * @param tracks
     *            The number of tracks on the CD
     * @param durationInMinutes
     *            The duration of the CD in minutes
     */
    public void addCD(final String title, final String author, final int tracks,
            final int durationInMinutes) {
        final CD cd = new CD();
        cd.title = title;
        cd.author = author;
        cd.tracks = tracks;
        cd.duration = durationInMinutes;
        final List<CD> cdList = new ArrayList<>();
        cdList.add(cd);
    }
}
