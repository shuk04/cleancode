/**
 *
 */
package com.spr.ajwf.methods.jobs.imports.deliveryarchiveimport.action;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import com.documentum.fc.common.DfException;
import com.spr.ajwf.commons.dm.logic.Article;
import com.spr.ajwf.commons.dm.logic.Folder;
import com.spr.ajwf.commons.dm.logic.Issue;
import com.spr.ajwf.commons.dm.logic.Journal;
import com.spr.ajwf.commons.dm.logic.SysObject;
import com.spr.ajwf.commons.dm.util.operation.DMOperationException;
import com.spr.ajwf.commons.logger.Logger;
import com.spr.ajwf.commons.util.FileHelper;
import com.spr.ajwf.commons.util.FileVisitor;
import com.spr.ajwf.constants.FilePrefixConstants;
import com.spr.ajwf.constants.IConstants;
import com.spr.ajwf.methods.jobs.exception.SkipFileException;
import com.spr.ajwf.methods.jobs.exception.StopJobException;
import com.spr.ajwf.methods.jobs.imports.deliveryarchiveimport.token.DeliveryImportToken;
import com.spr.ajwf.methods.jobs.imports.util.FileProcessingFailedException;
import com.spr.ajwf.methods.jobs.imports.util.SingleFileImportObject;
import com.spr.ajwf.methods.jobs.imports.util.deliverable.DeliverableHandler;
import com.spr.ajwf.methods.jobs.transfer.ImportFailedException;

/**
 * This class iterates over all the files in the given Import package/File and
 * Imports them in the repository.
 *
 * @author Chandresh Shukla
 */
public class CheckinDeliverables implements DeliveryArchiveImportAction {

    private static final String SAPARATOR = "_";

    @Override
    public void execute(
            final DeliveryImportToken token) throws FileProcessingFailedException, SkipFileException, StopJobException, Exception {
        FileHelper.walkFileTree(token.getDelivery().getBaseDirectory(), new FileVisitor() {

            @Override
            public FileVisitResult visitFile(final File file) {
                try {
                    importFileinDocbase(file, token);
                } catch (final Exception e) {
                    throw new ImportFailedException(e);
                }
                return FileVisitResult.CONTINUE;
            }

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

        });
    }

    /**
     * Imports the passed file in repository.Object type and metadata are
     * calculated based on delivery type and File being imported.
     *
     * @param token
     *            token.
     * @throws DfException
     * @throws IOException
     * @throws Exception
     * @throws DMOperationException
     */

    protected void importFileinDocbase(final File file,
            final DeliveryImportToken token) throws DfException, IOException {
        LOGGER.info("Importing File [" + file.getName() + "]");
        final SingleFileImportObject fileImportHandler = new SingleFileImportObject(file, token);
        Folder folder;
        folder = DeliverableParentResolver.getParentFolder(token, file, fileImportHandler);
        final DeliverableHandler helper = SingleFileImportObject.DELIVERABLE_TYPE_TO_HANDLER_MAP
                .get(fileImportHandler.getDeliveryType());
        final SysObject importedObject = helper.importDeliverable(renameFiles(fileImportHandler,
                token), folder, token);
        UpdateTokenVariables.setVariablesInToken(token, importedObject, file);
        LOGGER.info("Importing Fileinished File[" + file.getName() + "] imported as object [" +
                importedObject.getObjectName() + "]");
    }

    private File renameFiles(final SingleFileImportObject fileImportHandler,
            final DeliveryImportToken token) throws IOException {
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

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = new Logger(CheckinDeliverables.class);

}
