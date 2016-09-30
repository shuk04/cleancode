/**
 *
 */
package com.spr.ajwf.methods.jobs.imports.deliveryarchiveimport.action.refactored;

import java.io.File;
import java.io.IOException;

import com.documentum.fc.common.DfException;
import com.spr.ajwf.commons.dm.logic.Folder;
import com.spr.ajwf.commons.dm.logic.SysObject;
import com.spr.ajwf.commons.dm.util.operation.DMOperationException;
import com.spr.ajwf.commons.logger.Logger;
import com.spr.ajwf.commons.util.FileHelper;
import com.spr.ajwf.methods.jobs.imports.deliveryarchiveimport.action.DeliverableParentResolver;
import com.spr.ajwf.methods.jobs.imports.deliveryarchiveimport.action.DeliveryArchiveImportAction;
import com.spr.ajwf.methods.jobs.imports.deliveryarchiveimport.action.UpdateTokenVariables;
import com.spr.ajwf.methods.jobs.imports.deliveryarchiveimport.token.DeliveryImportToken;
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

    @Override
    public void execute(final DeliveryImportToken token) throws Exception {
        this.token = token;
        FileHelper.walkFileTree(token.getDelivery().getBaseDirectory(), new TokenFileVisitor() {

            @Override
            public java.nio.file.FileVisitResult visitFile(final File file) {
                importFileinDocbase(file);
                return java.nio.file.FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Imports the passed file in repository.Object type and metadata are
     * calculated based on delivery type and File being imported.
     *
     * @throws DfException
     * @throws IOException
     * @throws Exception
     * @throws DMOperationException
     */

    protected void importFileinDocbase(final File file) {
        try {
            LOGGER.info("Importing File [" + file.getName() + "]");
            final SingleFileImportObject fileImportHandler = new SingleFileImportObject(file,
                    token);
            final Folder folder = DeliverableParentResolver.getParentFolder(token, file,
                    fileImportHandler);
            final DeliverableHandler helper = SingleFileImportObject.DELIVERABLE_TYPE_TO_HANDLER_MAP
                    .get(fileImportHandler.getDeliveryType());
            final SysObject importedObject = helper.importDeliverable(new RenameFiles(
                    fileImportHandler, token).getFile(), folder, token);
            UpdateTokenVariables.setVariablesInToken(token, importedObject, file);
            LOGGER.info("Importing Fileinished File[" + file.getName() + "] imported as object [" +
                    importedObject.getObjectName() + "]");
        } catch (final Exception e) {
            throw new ImportFailedException(e);
        }
    }

    private DeliveryImportToken token;

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = new Logger(CheckinDeliverables.class);

}
