package com.spr.dirtycode.movie;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import com.spr.cleancode.movie.dummy.DOMHelper;
import com.spr.cleancode.movie.dummy.IDfSession;
import com.spr.cleancode.movie.dummy.JdbcHelper;

public class TimeForFun {

    public Fun doMyStuff(
            final File file) throws ParserConfigurationException, SAXException, IOException, TransformerException {
        final DOMHelper d = new DOMHelper(file);
        final String t = d.selectMandatoryText("SOME_XPATH");
        final String r = d.selectMandatoryText("SOME_XPATH");
        final String ta = d.selectMandatoryText("SOME_XPATH");
        final IDfSession s = getSession();
        new JdbcHelper().executeUpdateSqlByJdbc(s, "Some sql to update stuff");
        final Fun mo = new Fun();
        mo.setI(5);
        mo.setR(r);
        mo.setT(t);
        mo.setTa(ta);
        enjoy(mo);
        return mo;
    }

    private void enjoy(final Fun m) {
        // Playing MOvie on Console
        System.out.println(m);
    }

    private static IDfSession getSession() {

        return null;
    }

}
