package com.spr.cleancode.movie;

import java.io.File;

import com.spr.cleancode.movie.dummy.IDfSession;
import com.spr.cleancode.movie.dummy.JdbcHelper;
import com.spr.cleancode.movie.dummy.SessionHelper;

public class XMLMovieWatcher {

    public static void main(final String[] args) throws Exception {
        try {
            final File file = new File("Some Path");
            new PlayMovie().processMovie(file);
            updateMoviedataBase(SessionHelper.getSession());
        } catch (final Exception e) {
            // do some handing.
        }

    }

    private static void updateMoviedataBase(final IDfSession session) {
        new JdbcHelper().executeUpdateSqlByJdbc(session, "Some sql to update stuff");

    }
}
