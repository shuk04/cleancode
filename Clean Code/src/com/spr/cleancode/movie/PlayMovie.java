package com.spr.cleancode.movie;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import com.spr.cleancode.movie.dummy.DOMHelper;

public class PlayMovie {

    private static final String XPATH_FOR_RATINGS = "XPATH_FOR_RATING";

    private static final String XPATH_FOR_TAGLINE = "XPATH_FOR_TAGLINE";

    private static final String XPATH_FOR_TITLE = "XPATH_FOR_TITLE";

    public Movie processMovie(
            final File file) throws ParserConfigurationException, SAXException, IOException, TransformerException {
        final Movie movie = getMovieDetails(file);
        playMovieOnConsole(movie);
        return movie;
    }

    private Movie getMovieDetails(
            final File file) throws ParserConfigurationException, SAXException, IOException, TransformerException {
        final DOMHelper movieDom = new DOMHelper(file);
        final Movie movie = new Movie();
        movie.setRating(movieDom.selectMandatoryText(XPATH_FOR_RATINGS));
        movie.setTitle(movieDom.selectMandatoryText(XPATH_FOR_TITLE));
        movie.setTagline(movieDom.selectMandatoryText(XPATH_FOR_TAGLINE));
        movie.setStatus(Status.WATCHED);
        return movie;
    }

    private void playMovieOnConsole(final Movie movie) {
        // TODO Playing MOvie on Console
        System.out.println(movie);
    }

}
