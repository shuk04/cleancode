
package com.spr.ajwf.workflow_methods.file_operations.refactored_elf;

import java.io.FileNotFoundException;
import java.text.MessageFormat;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.spr.ajwf.commons.logger.Logger;
import com.spr.ajwf.constants.IConstants;
import com.spr.ajwf.util.UtilityMethods;
import com.spr.ajwf.workflow_methods.file_operations.FileCommonUtil;

public class ProcessELFReceivedFilesRefactored {

    private IDfSession idfSession = null;

    ParseImageXML imageXmlData;

    public ProcessELFReceivedFilesRefactored(final IDfSession idfSession) {
        super();
        this.idfSession = idfSession;
    }

    public void processFilesFromElf(final IDfSession session, final String[] attachmentIds,
            final String strArticleFolderPath, final String strName) throws Exception {
        final String stageName = getStageName(strName);
        importAttachmentIdsAsDocuments(attachmentIds, strArticleFolderPath, stageName);
        processImageMetadata(getGraphicsFolderPath(strArticleFolderPath, stageName));
    }

    private void importAttachmentIdsAsDocuments(final String[] attachmentIds,
            final String strArticleFolderPath, final String stageName) throws Exception {
        LOGGER.debug("Processing files received from ELF");
        final String strAllObjIds = getAttachmentIds(attachmentIds);
        initMetaDataXml(strAllObjIds);
        imageXmlData = new ParseImageXML(idfMetadataXML);
        imageXmlData.parseElfMetadataXml();
        for (final String attachmentId : attachmentIds) {
            importAttachmentAsDocument(strArticleFolderPath, stageName, attachmentId);
        }
    }

    private StringBuilder getGraphicsFolderPath(final String strArticleFolderPath,
            final String strName) {
        return new StringBuilder(strArticleFolderPath + "/" + strName + "/" +
                IConstants.GRAPHICS_FOLDER);
    }

    private void importAttachmentAsDocument(final String strArticleFolderPath, final String strName,
            final String attachmentId) throws DfException {
        final IDfDocument attachedDoc = setContent(attachmentId);
        linkDocumentToArticle(attachedDoc, strArticleFolderPath, strName);
        LOGGER.debug("Method linkDocumentToArticle completed successfully for document : " +
                attachedDoc.getObjectName());
        deleteIfNeeded(attachedDoc);
    }

    private IDfDocument setContent(final String attachmentId) throws DfException {
        final IDfDocument attachedDoc = getDocumentFromID(attachmentId);
        fileUtil.setContentTypeAndName(idfSession, attachedDoc);
        LOGGER.debug("Name : " + attachedDoc.getObjectName() + "Content Type : " + attachedDoc
                .getContentType());
        return attachedDoc;
    }

    private void deleteIfNeeded(final IDfDocument attachedDoc) throws DfException {
        if (attachedDoc.findString("r_version_label", "DELETE") > 1) {
            LOGGER.debug(" Deleting document : " + attachedDoc.getObjectName());
            LOGGER.debug("Location : " + attachedDoc.getAllRepeatingStrings("i_folder_id", ","));
            attachedDoc.destroy();

        }
    }

    private IDfDocument getDocumentFromID(final String attachmentId) throws DfException {
        final IDfDocument attachedDoc = (IDfDocument) idfSession.getObjectByQualification(
                IConstants.TYPE_WORKITEM_OBJECT + " " + " WHERE r_object_id = '" + attachmentId +
                        "'");
        if (attachedDoc == null) {
            throw new IllegalArgumentException("Document does not exist with object ID : " +
                    attachmentId);
        }
        return attachedDoc;
    }

    private void initMetaDataXml(
            final String strAllObjIds) throws DfException, FileNotFoundException {
        LOGGER.debug("All Object IDs : " + strAllObjIds);
        idfMetadataXML = (IDfDocument) idfSession.getObjectByQualification(dql);
        LOGGER.debug(" Dql for Checking Metadata XML : " + dql);
        if (idfMetadataXML == null) {
            throw new FileNotFoundException("Metadata XML is missing in received package");
        }
    }

    private String getAttachmentIds(final String[] attachmentIds) {
        String strAllObjIds = "";
        for (final String attachmentId : attachmentIds) {
            if ("".equals(strAllObjIds)) {
                strAllObjIds += "'" + attachmentId + "'";
            } else {
                strAllObjIds += ",'" + attachmentId + "'";
            }
        }
        return strAllObjIds;
    }

    private String getStageName(String strName) {
        if (IConstants.ATTR_STAGE_NAME_200.equalsIgnoreCase(strName)) {
            strName = IConstants.FOLDER_NAME_STAGE_200;
        } else {
            strName = IConstants.FOLDER_NAME_STAGE_300;
        }
        return strName;
    }

    private IDfDocument linkDocumentToArticle(final IDfDocument idfAttacment,
            final String strArticleFolderPath, final String strStageFolderName) throws DfException {
        String objectName;
        IDfDocument idfExistingObj;
        final UtilityMethods util = new UtilityMethods();
        objectName = idfAttacment.getObjectName();
        final AttachmentTypeData data = Conditions.ItemTypeConditions.getData(objectName);
        idfExistingObj = getExistingElfDocument(objectName, data);
        if (idfExistingObj == null) {
            return importAsNewObject(idfAttacment, objectName, util, data);
        } else {
            importAsVersion(idfAttacment, idfExistingObj);
        }

        return idfAttacment;

    }

    private IDfDocument importAsVersion(final IDfDocument idfAttacment,
            final IDfDocument idfExistingObj) throws DfException {
        LOGGER.debug("Existing object found, new minor version will be created");
        final boolean isCheckedOut = utilObj.executeCheckoutOperation(idfExistingObj);
        if (isCheckedOut) {
            final IDfDocument idfNewVersion = utilObj.executeCheckinOperation(idfSession,
                    idfExistingObj, idfAttacment);
            if (idfNewVersion != null) {
                idfAttacment.mark("DELETE");
                idfAttacment.save();
                DfLogger.info(this, "  " + idfAttacment.getObjectName() +
                        " Marked as to be deleted ", null, null);
                return idfNewVersion;
            }
        }
        return idfExistingObj;
    }

    private IDfDocument importAsNewObject(final IDfDocument idfAttacment, final String objectName,
            final UtilityMethods util, final AttachmentTypeData data) throws DfException {
        LOGGER.debug("New document processing. Linking to article folder ************** ");

        if (data.isLogObject == true) {
            importLog(idfAttacment, objectName, util, data);
        } else if (data.ObjectType.equalsIgnoreCase(IConstants.TYPE_WORKITEM_OBJECT)) {

            importWorkItem(idfAttacment, util, data);
        } else {
            idfAttacment.setString(IConstants.ATTR_ITEM_TYPE, data.ItemType);
        }
        LOGGER.debug("Folder Path : " + data.folderPah);
        final IDfACL folderAcl = idfSession.getFolderByPath(data.folderPah.toString()).getACL();
        LOGGER.debug("ACL : " + folderAcl.getObjectName());
        if (data.isImageObject && (objectName.split("_").length == 5)) {
            idfAttacment.setTitle(objectName.split("_")[3]);
        }

        idfAttacment.setACL(folderAcl);
        idfAttacment.unlink(idfAttacment.getFolderId(0).toString());
        idfAttacment.link(data.folderPah.toString());
        idfAttacment.save();
        return idfAttacment;
    }

    private void importWorkItem(final IDfDocument idfAttacment, final UtilityMethods util,
            final AttachmentTypeData data) throws DfException {
        final String strObjId = idfAttacment.getObjectId().getId();
        final String strDql = "change " + IConstants.TYPE_IMAGE_WORKITEM_OBJECT + " objects to " +
                IConstants.TYPE_WORKITEM_OBJECT + " where r_object_id = '" + strObjId + "' ";

        LOGGER.debug("Change Type DQL : " + strDql);

        final IDfCollection idfColl3 = util.executeQuery(idfSession, strDql, IDfQuery.EXEC_QUERY);
        if (idfColl3 != null && IDfCollection.DF_CLOSED_STATE != idfColl3.getState()) {
            idfColl3.close();
        }
        idfAttacment.fetch(null);
        idfAttacment.setString(IConstants.ATTR_ITEM_TYPE, data.getItemType());

        LOGGER.debug("Changed item type : " + idfAttacment.getString(IConstants.ATTR_ITEM_TYPE));
    }

    private void importLog(final IDfDocument idfAttacment, final String objectName,
            final UtilityMethods util, final AttachmentTypeData data) throws DfException {
        LOGGER.debug("Object Name of log file : " + objectName);

        final String strObjId = idfAttacment.getObjectId().getId();
        String strDql = "change " + IConstants.TYPE_IMAGE_WORKITEM_OBJECT +
                " objects to dm_document where r_object_id = '" + strObjId + "' ";

        LOGGER.debug("Change Type DQL : " + strDql);

        final IDfCollection idfColl1 = util.executeQuery(idfSession, strDql, IDfQuery.EXEC_QUERY);
        if (idfColl1 != null && IDfCollection.DF_CLOSED_STATE != idfColl1.getState()) {
            idfColl1.close();
        }

        strDql = "change dm_document objects to " + IConstants.TYPE_LOG_OBJECT +
                " where r_object_id='" + strObjId + "' ";

        final IDfCollection idfColl2 = util.executeQuery(idfSession, strDql, IDfQuery.EXEC_QUERY);
        if (idfColl2 != null && IDfCollection.DF_CLOSED_STATE != idfColl2.getState()) {
            idfColl2.close();
        }
        idfAttacment.fetch(null);
        idfAttacment.setString(IConstants.ATTR_LOG_TYPE, data.logType);
        idfAttacment.setString(IConstants.ATTR_LOG_CATEGORY, data.logCategory);
        idfAttacment.setObjectName(objectName);
    }

    private IDfDocument getExistingElfDocument(final String objectName,
            final AttachmentTypeData data) throws DfException {
        IDfDocument idfExistingObj;
        if (data.isLogObject) {
            idfExistingObj = (IDfDocument) idfSession.getObjectByQualification(data.ObjectType +
                    " where Folder('" + data.folderPah + "') " + " and " +
                    IConstants.ATTR_LOG_CATEGORY + " = '" + data.logCategory + "'");
        } else {

            String strQualificationQuery = data.getObjectType() + " where " + "Folder('" +
                    data.folderPah + "') and " + "item_type = '" + data.getItemType() + "' ";
            if (objectName.substring(objectName.indexOf(".") + 1).equalsIgnoreCase("jpg") ||
                    objectName.substring(objectName.indexOf(".") + 1).equalsIgnoreCase("jpeg") ||
                    objectName.substring(objectName.indexOf(".") + 1).equalsIgnoreCase("gif")) {
                strQualificationQuery += "and object_name like '" + objectName.substring(0,
                        objectName.indexOf(".")) + "%' ";
            } else {
                strQualificationQuery += "and object_name ='" + objectName + "'";
            }

            LOGGER.debug(" Qualification to check existing object : " + strQualificationQuery);

            idfExistingObj = (IDfDocument) idfSession.getObjectByQualification(
                    strQualificationQuery);
        }
        return idfExistingObj;
    }

    private void processImageMetadata(
            final StringBuilder strGrpahicsFolderPath) throws DfException {
        Map<String, String> mapWebImageMetada = null;
        Map<String, String> mapPrintImageMetada = null;
        Map<String, String> mapOnlineImageMetada = null;
        boolean isOnlineImageExist = false;
        IDfDocument printImageObject = null;
        IDfDocument onlineImageObject = null;

        validateImages();

        for (final Map.Entry<String, Map<String, String>> entry : imageXmlData.mapWeb.entrySet()) {

            isOnlineImageExist = false;

            LOGGER.debug("Updating metadata for Image : " + entry.getKey());

            final String strWebImageName = entry.getKey();

            mapWebImageMetada = entry.getValue();

            if (imageXmlData.mapPrint.containsKey(strWebImageName)) {
                mapPrintImageMetada = imageXmlData.mapPrint.get(strWebImageName);
            }

            if (imageXmlData.mapOnline.containsKey(strWebImageName)) {
                mapOnlineImageMetada = imageXmlData.mapOnline.get(strWebImageName);
                isOnlineImageExist = true;
            }
            LOGGER.debug(" is Online Image exisit for " + strWebImageName + " : " +
                    isOnlineImageExist);
            final IDfDocument webImageObject = getImageFromXpath(dql, mapWebImageMetada.get(
                    IConstants.STR_XPATH_NAME), IConstants.ITEM_TYPE_WEB_IMAGE);
            printImageObject = getImageFromXpath(dql, mapPrintImageMetada.get(
                    IConstants.STR_XPATH_NAME), IConstants.ITEM_TYPE_PRINT_IMAGE);
            if (isOnlineImageExist) {
                onlineImageObject = getImageFromXpath(dql, mapOnlineImageMetada.get(
                        IConstants.STR_XPATH_NAME).substring(0, mapOnlineImageMetada.get(
                                IConstants.STR_XPATH_NAME).indexOf(".")),
                        IConstants.ITEM_TYPE_ONLINE_IMAGE);
                if (onlineImageObject != null) {
                    modifyImageOnline(mapOnlineImageMetada, onlineImageObject);
                }
            }
            new SetImageMetadata(mapWebImageMetada, webImageObject).setImageMetadata();
            ;
            new SetImageMetadata(mapPrintImageMetada, printImageObject).setImageMetadata();

            if (isOnlineImageExist == false) {
                // If no online image and no error for web\print..
                if (StringUtils.isEmpty(mapWebImageMetada.get(IConstants.STR_XPATH_ERROR)) &&
                        StringUtils.isEmpty(mapPrintImageMetada.get(IConstants.STR_XPATH_ERROR))) {

                    LOGGER.debug("Marking Images as Error Online image did not received from ELF");

                    webImageObject.setString(IConstants.ATTR_WORKITEM_ITEM_STATUS,
                            IConstants.STATUS_WORK_ITEM_ERROR);
                    printImageObject.setString(IConstants.ATTR_WORKITEM_ITEM_STATUS,
                            IConstants.STATUS_WORK_ITEM_ERROR);

                    webImageObject.setString(IConstants.ATTR_WORKITEM_ERROR_DESC,
                            IConstants.ERROR_MSG_ONLINE_IMG_ELF);
                    printImageObject.setString(IConstants.ATTR_WORKITEM_ERROR_DESC,
                            IConstants.ERROR_MSG_ONLINE_IMG_ELF);
                }
                // If print image as error, marking web as error as well and
                // with desc. as 'Error in Print Image'
                else if (StringUtils.isEmpty(mapWebImageMetada.get(IConstants.STR_XPATH_ERROR)) &&
                        !StringUtils.isEmpty(mapPrintImageMetada.get(IConstants.STR_XPATH_ERROR))) {

                    LOGGER.debug(
                            "Marking Print Images as error given by ELF and web image as error in print image");

                    webImageObject.setString(IConstants.ATTR_WORKITEM_ITEM_STATUS,
                            IConstants.STATUS_WORK_ITEM_ERROR);
                    printImageObject.setString(IConstants.ATTR_WORKITEM_ITEM_STATUS,
                            IConstants.STATUS_WORK_ITEM_ERROR);

                    webImageObject.setString(IConstants.ATTR_WORKITEM_ERROR_DESC,
                            IConstants.ERROR_MSG_PRINT_IMG_ELF);
                    printImageObject.setString(IConstants.ATTR_WORKITEM_ERROR_DESC,
                            mapPrintImageMetada.get(IConstants.STR_XPATH_ERROR));
                }
                // If web image as error, marking print as error as well and
                // with desc. as 'Error in Web Image'
                else if (!StringUtils.isEmpty(mapWebImageMetada.get(IConstants.STR_XPATH_ERROR)) &&
                        StringUtils.isEmpty(mapPrintImageMetada.get(IConstants.STR_XPATH_ERROR))) {
                    LOGGER.debug(
                            "Marking Web Image as error given by ELF and print image as error in web image");

                    webImageObject.setString(IConstants.ATTR_WORKITEM_ITEM_STATUS,
                            IConstants.STATUS_WORK_ITEM_ERROR);
                    printImageObject.setString(IConstants.ATTR_WORKITEM_ITEM_STATUS,
                            IConstants.STATUS_WORK_ITEM_ERROR);

                    webImageObject.setString(IConstants.ATTR_WORKITEM_ERROR_DESC, mapWebImageMetada
                            .get(IConstants.STR_XPATH_ERROR));
                    printImageObject.setString(IConstants.ATTR_WORKITEM_ERROR_DESC,
                            IConstants.ERROR_MSG_WEB_IMG_ELF);
                } else {
                    LOGGER.debug("Marking Web and Print Image as error given by ELF");

                    webImageObject.setString(IConstants.ATTR_WORKITEM_ITEM_STATUS,
                            IConstants.STATUS_WORK_ITEM_ERROR);
                    printImageObject.setString(IConstants.ATTR_WORKITEM_ITEM_STATUS,
                            IConstants.STATUS_WORK_ITEM_ERROR);

                    webImageObject.setString(IConstants.ATTR_WORKITEM_ERROR_DESC, mapWebImageMetada
                            .get(IConstants.STR_XPATH_ERROR));
                    printImageObject.setString(IConstants.ATTR_WORKITEM_ERROR_DESC,
                            mapPrintImageMetada.get(IConstants.STR_XPATH_ERROR));
                }
            } else {

                LOGGER.debug("Marking images as SUCCESS");
                webImageObject.setString(IConstants.ATTR_WORKITEM_ITEM_STATUS,
                        IConstants.STATUS_WORK_ITEM_SUCCESS);
                printImageObject.setString(IConstants.ATTR_WORKITEM_ITEM_STATUS,
                        IConstants.STATUS_WORK_ITEM_SUCCESS);
            }

            webImageObject.save();
            printImageObject.save();

            LOGGER.debug("Web and Print Image objects saved successfully :  " + webImageObject
                    .getObjectName());
        }
    }

    private void modifyImageOnline(final Map<String, String> mapOnlineImageMetada,
            final IDfDocument onlineImageObject) throws DfException {
        new SetImageMetadata(mapOnlineImageMetada, onlineImageObject).setImageMetadata();
        onlineImageObject.setString(IConstants.ATTR_WORKITEM_ITEM_STATUS,
                IConstants.STATUS_WORK_ITEM_SUCCESS);
        onlineImageObject.save();

        LOGGER.debug("Online Image object saved successfully :  " + onlineImageObject
                .getObjectName());
    }

    private IDfDocument getImageFromXpath(final String dqlFormat, final String xPath,
            final String itemType) throws DfException {
        final Object[] objects = { xPath, itemType };
        final String dql = new MessageFormat(dqlFormat).format(objects);
        final IDfDocument webImageObject = (IDfDocument) idfSession.getObjectByQualification(dql);
        LOGGER.debug("DQL : " + dql);
        return webImageObject;
    }

    private void validateImages() {
        if (imageXmlData.mapWeb.size() != imageXmlData.mapPrint.size()) {
            LOGGER.error("Received metadata count is not same for Web and Print");
        }
    }

    Logger LOGGER = new Logger(ProcessELFReceivedFilesRefactored.class);

    private IDfDocument idfMetadataXML = null;

    private final UtilityMethods utilObj = new UtilityMethods();

    private final FileCommonUtil fileUtil = new FileCommonUtil();

    final String dql = IConstants.TYPE_IMAGE_WORKITEM_OBJECT + " " + " where object_name ='" +
            "'{0}'" + "' " + " and item_type = '" + "'{1}'" + "' " + " and folder('" + "'{2}'" +
            "',descend)";

    final String QUALIFICATION_TO_GET_METADATA_XML = IConstants.TYPE_WORKITEM_OBJECT + "" +
            " WHERE r_object_id in (''{1}'' ) " + " and lower(object_name) like lower('%" +
            IConstants.ELF_FILE_PREFIX_METADATA_XML + ".xml')";

}
