package com.spr.ajwf.methods.jobs.imports.deliveryarchiveimport.action.refactored;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;

import com.spr.ajwf.commons.util.FileVisitor;

public abstract class TokenFileVisitor implements FileVisitor {

    @Override
    public FileVisitResult preVisitDirectory(final File dir) {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(final File dir) {
        return FileVisitResult.CONTINUE;

    }

    @Override
    public void error(final File f, final IOException exc) throws IOException {
        throw exc;
    }

};
