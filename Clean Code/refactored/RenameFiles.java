package com.spr.ajwf.methods.jobs.imports.deliveryarchiveimport.action.refactored;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import com.spr.ajwf.commons.dm.logic.Article;
import com.spr.ajwf.commons.dm.logic.Issue;
import com.spr.ajwf.commons.dm.logic.Journal;
import com.spr.ajwf.commons.logger.Logger;
import com.spr.ajwf.commons.util.FileHelper;
import com.spr.ajwf.constants.FilePrefixConstants;
import com.spr.ajwf.constants.IConstants;
import com.spr.ajwf.methods.jobs.imports.deliveryarchiveimport.token.DeliveryImportToken;
import com.spr.ajwf.methods.jobs.imports.util.SingleFileImportObject;

public class RenameFiles {

    private final SingleFileImportObject fileImportHandler;

    private final DeliveryImportToken token;

    public RenameFiles(final SingleFileImportObject fileImportHandler,
                       final DeliveryImportToken token) {
        this.token = token;
        this.fileImportHandler = fileImportHandler;
    }

    private File renameFiles() throws IOException {
        final String strFileName = fileImportHandler.getFile().getName();
        String strNewFileName = "";
        File file = null;
        Issue issue;
        LOGGER.info("renameLogFiles :: File name : " + strFileName);
        switch (fileImportHandler.getDeliveryType()) {
            case BREEZE_LOG_TEXT:
            case BREEZE_LOG_XML:
                strNewFileName = strFileName.replace(
                        FilePrefixConstants.FILE_SUFFIX_BREEZE_ERROR_FILE,
                        FilePrefixConstants.FILE_SUFFIX_BREEZE_ERROR_FILE + "_" +
                                FilePrefixConstants.FILE_SUFFIX_BREEZE);
                break;
            case COVERTIFF_IMAGE:
                issue = (Issue) token.getWorkflowObject();
                strNewFileName = getIssueId(issue) + SAPARATOR + issue.getString(
                        Issue.ZSS_ISSUE_NUMBER) + FilePrefixConstants.FILE_SUFFIX_COVER_TIFF;
                break;
            case COVERTIFF_LOG:
                issue = (Issue) token.getWorkflowObject();
                strNewFileName = getIssueId(issue) + SAPARATOR + issue.getString(
                        Issue.ZSS_ISSUE_NUMBER) + COVER_LOG_SUFFIX + "." + FileHelper.getExtension(
                                fileImportHandler.getFile());
                break;
            case MATHML_CONTENT:
                strNewFileName = getArticleNameForMathML(strNewFileName, token,
                        IConstants.ARTICLE_FILENAME_SUFFIX);
                break;
            case MATHML_LOG_TEXT:
                strNewFileName = getArticleNameForMathML(strNewFileName, token,
                        IConstants.MATHML_LOGFILE_SUFFIX);

                break;
            default:
                return fileImportHandler.getFile();
        }
        file = new File(fileImportHandler.getFile().getParent(), strNewFileName);
        Files.move(fileImportHandler.getFile().toPath(), file.toPath(),
                StandardCopyOption.REPLACE_EXISTING);
        LOGGER.info("renameLogFiles :: renamed file name : " + file.getAbsolutePath());
        return file;
    }

    private String getIssueId(final Issue issue) {
        return issue.getJournal().getString(Journal.JOURNAL_ID) + SAPARATOR + issue.getString(
                Issue.ZSS_VOLUME_NUMBER);
    }

    public File getFile() throws IOException {
        return renameFiles();
    }

    private String getArticleNameForMathML(final String strNewFileName,
            final DeliveryImportToken token, final String fileNameSuffix) {
        if (token.getWorkflowObject() instanceof Article) {
            final Article article = (Article) token.getWorkflowObject();
            if (null == article) {
                throw new RuntimeException(
                        "Failed to obtain article object from workflow object : Null object found");
            }
            LOGGER.info("MathML File :: renamed file name : " + strNewFileName);
            return article.getArticleIdForName() + fileNameSuffix;
        } else {
            throw new RuntimeException(
                    "Failed to obtain article object instanc from workflow object : instance of check has failed");
        }
    }

    private static final String COVER_LOG_SUFFIX = "_Cover_Error";

    private static final String SAPARATOR = "_";

    private static final Logger LOGGER = new Logger(CheckinDeliverables.class);

}
