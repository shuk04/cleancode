package com.spr.dirtycode.movie;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

public class XMLMovieWatcher {

    public static void main(
            final String[] args) throws ParserConfigurationException, SAXException, IOException, TransformerException {
        try

        {
            final File file = new File("Some Path");
            new TimeForFun().doMyStuff(file);
        } catch (final Exception e) {
            // do some handing.
        }

    }
}
