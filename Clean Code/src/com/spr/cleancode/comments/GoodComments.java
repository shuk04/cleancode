package com.spr.cleancode.comments;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

import com.spr.cleancode.comments.dummy.SysObject;

/**
 * Copyright (C) 2003,2004,2005 by Anup Das, Inc. All rights reserved. Released
 * under the terms of the GNU General Public License version 2 or later.
 */
public class GoodComments {

    // format matched kk:mm:ss EEE, MMM dd, yyyy
    Pattern timeMatcher = Pattern.compile("\\d*:\\d*:\\d* \\w*, \\w* \\d*, \\d*");

    public int compareTo(final Object o) {
        if (o instanceof GoodComments) {
            final GoodComments p = (GoodComments) o;
            return p.compareTo(this);
        }
        return 1; // we are greater GoodComments object is grater than any
                  // object.[By US-XXX]
    }

    @Test
    public void testCompareTo() throws Exception {
        final GoodComments a = serilize("PageA");
        final GoodComments ab = serilize("PageA.PageB");
        final GoodComments b = serilize("PageB");
        final GoodComments aa = serilize("PageA.PageA");
        final GoodComments bb = serilize("PageB.PageB");
        final GoodComments ba = serilize("PageB.PageA");
        assertTrue(a.compareTo(a) == 0); // a == a
        assertTrue(a.compareTo(b) != 0); // a != b
        assertTrue(ab.compareTo(ab) == 0); // ab == ab
        assertTrue(a.compareTo(b) == -1); // a < b
        assertTrue(aa.compareTo(ab) == -1); // aa < ab
        assertTrue(ba.compareTo(bb) == -1); // ba < bb
        assertTrue(b.compareTo(a) == 1); // b > a
        assertTrue(ab.compareTo(aa) == 1); // ab > aa
        assertTrue(bb.compareTo(ba) == 1); // bb > ba
    }

    private GoodComments serilize(final String comment) {
        return new GoodComments();
    }

    // Dont run it unless you are very sure.
    public void cleanUpRepo() {
        final List<SysObject> objects = getAllobjectFromRepo();
        for (final SysObject object : objects) {
            object.destroy();
        }
    }

    // FIXME :Implement this.
    private List<SysObject> getAllobjectFromRepo() {
        throw new UnsupportedOperationException();
    }

    public ListItemWidget amplification(final Object match) {
        // the trim is real important. It removes the starting
        // spaces that could cause the item to be recognized
        // as another list.
        final String listItemContent = match.toString().trim();

        return new ListItemWidget(this, listItemContent);
    }

}
