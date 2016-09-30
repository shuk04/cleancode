/**
 *
 */
package com.spr.ajwf.workflow_methods.file_operations;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.spr.ajwf.constants.IConstants;
import com.spr.ajwf.util.ACDCLogger;
import com.spr.ajwf.util.UtilityMethods;

public class ProcessELFReceivedFiles extends ACDCLogger {

    private IDfSession idfSession = null;

    private IDfDocument idfMetadataXML = null;

    private final Map<String, Map<String, String>> mapWeb = new HashMap<String, Map<String,
            String>>();

    private final Map<String, Map<String, String>> mapPrint = new HashMap<String, Map<String,
            String>>();

    private final Map<String, Map<String, String>> mapOnline = new HashMap<String, Map<String,
            String>>();

    private final UtilityMethods utilObj = new UtilityMethods();

    private final FileCommonUtil fileUtil = new FileCommonUtil();

    public void processFilesFromElf(final IDfSession session, final String[] attachmentIds,
            final String strArticleFolderPath,
            String strStageFolderName) throws DfException, XPathExpressionException, IOException, ParserConfigurationException, SAXException {

        idfSession = session;
        String strAllObjIds = "";

        if (IConstants.ATTR_STAGE_NAME_200.equalsIgnoreCase(strStageFolderName)) {
            strStageFolderName = IConstants.FOLDER_NAME_STAGE_200;
        } else {
            strStageFolderName = IConstants.FOLDER_NAME_STAGE_300;
        }

        debug("----------------------------------- Processing files received from ELF");

        for (final String attachmentId : attachmentIds) {
            if ("".equals(strAllObjIds)) {
                strAllObjIds += "'" + attachmentId + "'";
            } else {
                strAllObjIds += ",'" + attachmentId + "'";
            }
        }

        debug("All Object IDs : " + strAllObjIds);

        idfMetadataXML = (IDfDocument) idfSession.getObjectByQualification(
                IConstants.TYPE_WORKITEM_OBJECT + "" + " WHERE r_object_id in (" + strAllObjIds +
                        ") " + " and lower(object_name) like lower('%" +
                        IConstants.ELF_FILE_PREFIX_METADATA_XML + ".xml') ");

        debug(" Dql for Checking Metadata XML : " + IConstants.TYPE_WORKITEM_OBJECT + "" +
                " WHERE r_object_id in (" + strAllObjIds + ") " +
                " and lower(object_name) like lower('%" + IConstants.ELF_FILE_PREFIX_METADATA_XML +
                ".xml') ");

        if (idfMetadataXML == null) {
            error("Metadata XML is missing in received package **********************************");
            throw new FileNotFoundException("Metadata XML is missing in received package");
        }

        parseElfMetadataXml(idfMetadataXML);

        for (final String attachmentId : attachmentIds) {

            final IDfDocument idfDocument = (IDfDocument) idfSession.getObjectByQualification(
                    IConstants.TYPE_WORKITEM_OBJECT + " " + " WHERE r_object_id = '" +
                            attachmentId + "'");

            if (idfDocument == null) {
                DfLogger.error(this, "Document does not exist with object ID : " + attachmentId,
                        null, null);
                continue;
            }

            fileUtil.setContentTypeAndName(idfSession, idfDocument);

            DfLogger.debug(this, "Name : " + idfDocument.getObjectName(), null, null);
            DfLogger.debug(this, "Content Type : " + idfDocument.getContentType(), null, null);

            linkDocumentToArticle(idfDocument, strArticleFolderPath, strStageFolderName);

            debug("-------------------Method linkDocumentToArticle completed successfully for document : " +
                    idfDocument.getObjectName());

            if (idfDocument != null) {

                if (idfDocument.findString("r_version_label", "DELETE") > -1) {
                    debug("--------------------------------- Deleting document : " + idfDocument
                            .getObjectName());
                    debug("Location : " + idfDocument.getAllRepeatingStrings("i_folder_id", ","));
                    idfDocument.destroy();
                }
            }
        }

        final StringBuilder strGrpahicsFolderPath = new StringBuilder(strArticleFolderPath + "/" +
                strStageFolderName + "/" + IConstants.GRAPHICS_FOLDER);
        processImageMetadata(strGrpahicsFolderPath);

        debug("Processing completed successfully");

    }

    private IDfDocument linkDocumentToArticle(final IDfDocument idfAttacment,
            final String strArticleFolderPath, final String strStageFolderName) throws DfException {
        String strObjectType = "";
        String strItemType = "";
        String strLogCategory = "";
        final String strLogType = "";
        String strObjectName = "";
        boolean isLogObject = false;
        boolean isImageObject = false;
        IDfDocument idfExistingObj = null;

        final UtilityMethods util = new UtilityMethods();

        final StringBuilder strFolderPah = new StringBuilder(strArticleFolderPath + "/" +
                strStageFolderName);

        strObjectName = idfAttacment.getObjectName();

        if (strObjectName.toLowerCase().contains(IConstants.STR_CHECK_PDF_ONLINE) && strObjectName
                .toLowerCase().endsWith(".pdf")) {
            strItemType = IConstants.ITEM_TYPE_CHECK_PDF_ONLINE;
            strObjectType = IConstants.TYPE_WORKITEM_OBJECT;
            strFolderPah.append(IConstants.PATH_GRAPHICS_FOLDER);
        } else if (strObjectName.toLowerCase().contains(IConstants.STR_CHECK_PDF_PRINT) &&
                strObjectName.toLowerCase().endsWith(".pdf")) {

            strItemType = IConstants.ITEM_TYPE_CHECK_PDF_PRINT;
            strObjectType = IConstants.TYPE_WORKITEM_OBJECT;
            strFolderPah.append(IConstants.PATH_GRAPHICS_FOLDER);
        } else if (strObjectName.toLowerCase().contains(IConstants.STR_COLOR_ON_PAGE_PRINT) &&
                strObjectName.toLowerCase().endsWith(".pdf")) {

            strItemType = IConstants.ITEM_TYPE_COLOR_ON_PAGE_PRINT;
            strObjectType = IConstants.TYPE_WORKITEM_OBJECT;
            strFolderPah.append(IConstants.PATH_GRAPHICS_FOLDER);
        } else if (strObjectName.toLowerCase().contains(IConstants.ELF_FILE_PREFIX_METADATA_XML) &&
                strObjectName.endsWith(".xml")) {

            strObjectType = IConstants.TYPE_LOG_OBJECT;
            strLogCategory = IConstants.ATTR_LOG_CATEGORY_ELF_METADATA;

            isLogObject = true;
            strFolderPah.append(IConstants.PATH_GRAPHICS_FOLDER + "/" +
                    IConstants.FOLDER_NAME_LOGS);
        } else if (strObjectName.toLowerCase().contains(IConstants.IMAGE_RENDITION_HTML) &&
                (strObjectName.toLowerCase().endsWith(".jpg") || strObjectName.toLowerCase()
                        .endsWith(".gif"))) {

            strItemType = IConstants.ITEM_TYPE_ONLINE_IMAGE;
            strObjectType = IConstants.TYPE_IMAGE_WORKITEM_OBJECT;
            strFolderPah.append(IConstants.PATH_GRAPHICS_FOLDER + "/" + IConstants.ONLINE_FOLDER);
            isImageObject = true;
            DfLogger.debug(this, "isImageObject : " + isImageObject, null, null);
        } else if (strObjectName.toLowerCase().contains(IConstants.ELF_FILE_PREFIX_ERROR_XML) &&
                strObjectName.endsWith(".xml")) {

            strObjectType = IConstants.TYPE_LOG_OBJECT;
            strLogCategory = IConstants.ATTR_LOG_CATEGORY_ELF_MESSAGES;
            isLogObject = true;
            strFolderPah.append(IConstants.PATH_GRAPHICS_FOLDER + "/" +
                    IConstants.FOLDER_NAME_LOGS);
        } else {

            DfLogger.warn(this, "Unexpected file received : " + strObjectName +
                    " | no operations required on Object.. ", null, null);
            return idfAttacment;
        }

        debug("ProcessElfFiles : Document Item Type : " + strItemType);
        debug("ProcessElfFiles : Document Object Type : " + strObjectType);
        debug("ProcessElfFiles : Document Log Category : " + strLogCategory);
        debug("ProcessElfFiles : Log type : " + strLogType);
        debug("ProcessElfFiles : Folder Path : " + strFolderPah);

        debug("ProcessElfFiles : Is Image Object : " + isImageObject);
        debug("ProcessElfFiles : Is Log Object : " + isLogObject);

        if (isLogObject) {

            idfExistingObj = (IDfDocument) idfSession.getObjectByQualification(strObjectType +
                    " where Folder('" + strFolderPah + "') " + " and " +
                    IConstants.ATTR_LOG_CATEGORY + " = '" + strLogCategory + "'");

            DfLogger.debug(this, strObjectType + " where Folder('" + strFolderPah + "') " +
                    " and " + IConstants.ATTR_LOG_CATEGORY + " = '" + strLogCategory + "'", null,
                    null);

        } else {

            String strQualificationQuery = strObjectType + " where " + "Folder('" + strFolderPah +
                    "') and " + "item_type = '" + strItemType + "' ";
            if (strObjectName.substring(strObjectName.indexOf(".") + 1).equalsIgnoreCase("jpg") ||
                    strObjectName.substring(strObjectName.indexOf(".") + 1).equalsIgnoreCase(
                            "jpeg") || strObjectName.substring(strObjectName.indexOf(".") + 1)
                                    .equalsIgnoreCase("gif")) {
                strQualificationQuery += "and object_name like '" + strObjectName.substring(0,
                        strObjectName.indexOf(".")) + "%' ";
            } else {
                strQualificationQuery += "and object_name ='" + strObjectName + "'";
            }

            DfLogger.debug(this, "ProcessElfFiles : Qualification to check existing object : " +
                    strQualificationQuery, null, null);

            idfExistingObj = (IDfDocument) idfSession.getObjectByQualification(
                    strQualificationQuery);
        }

        if (idfExistingObj == null) {
            debug("New document processing. Linking to article folder ************** ");

            if (isLogObject == true) {
                debug("Object Name of log file : " + strObjectName);

                final String strObjId = idfAttacment.getObjectId().getId();
                String strDql = "change " + IConstants.TYPE_IMAGE_WORKITEM_OBJECT +
                        " objects to dm_document where r_object_id = '" + strObjId + "' ";

                debug("Change Type DQL : " + strDql);

                final IDfCollection idfColl1 = util.executeQuery(idfSession, strDql,
                        IDfQuery.EXEC_QUERY);
                if (idfColl1 != null && IDfCollection.DF_CLOSED_STATE != idfColl1.getState()) {
                    idfColl1.close();
                }

                strDql = "change dm_document objects to " + IConstants.TYPE_LOG_OBJECT +
                        " where r_object_id='" + strObjId + "' ";

                final IDfCollection idfColl2 = util.executeQuery(idfSession, strDql,
                        IDfQuery.EXEC_QUERY);
                if (idfColl2 != null && IDfCollection.DF_CLOSED_STATE != idfColl2.getState()) {
                    idfColl2.close();
                }
                idfAttacment.fetch(null);
                idfAttacment.setString(IConstants.ATTR_LOG_TYPE, strLogType);
                idfAttacment.setString(IConstants.ATTR_LOG_CATEGORY, strLogCategory);
                idfAttacment.setObjectName(strObjectName);
            } else if (strObjectType.equalsIgnoreCase(IConstants.TYPE_WORKITEM_OBJECT)) {

                final String strObjId = idfAttacment.getObjectId().getId();
                final String strDql = "change " + IConstants.TYPE_IMAGE_WORKITEM_OBJECT +
                        " objects to " + IConstants.TYPE_WORKITEM_OBJECT +
                        " where r_object_id = '" + strObjId + "' ";

                debug("Change Type DQL : " + strDql);

                final IDfCollection idfColl3 = util.executeQuery(idfSession, strDql,
                        IDfQuery.EXEC_QUERY);
                if (idfColl3 != null && IDfCollection.DF_CLOSED_STATE != idfColl3.getState()) {
                    idfColl3.close();
                }
                idfAttacment.fetch(null);
                idfAttacment.setString(IConstants.ATTR_ITEM_TYPE, strItemType);

                debug("Changed item type : " + idfAttacment.getString(IConstants.ATTR_ITEM_TYPE));
            } else {
                idfAttacment.setString(IConstants.ATTR_ITEM_TYPE, strItemType);
            }

            debug("Folder Path : " + strFolderPah);

            final IDfACL folderAcl = idfSession.getFolderByPath(strFolderPah.toString()).getACL();

            debug("ACL : " + folderAcl.getObjectName());

            if (isImageObject && (strObjectName.split("_").length == 5)) {
                idfAttacment.setTitle(strObjectName.split("_")[3]);
            }

            idfAttacment.setACL(folderAcl);
            idfAttacment.unlink(idfAttacment.getFolderId(0).toString());
            idfAttacment.link(strFolderPah.toString());

            idfAttacment.save();

            return idfAttacment;
        } else {
            debug("Existing object found, new minor version will be created");

            final boolean isCheckedOut = utilObj.executeCheckoutOperation(idfExistingObj);

            if (isCheckedOut) {
                final IDfDocument idfNewVersion = utilObj.executeCheckinOperation(idfSession,
                        idfExistingObj, idfAttacment);
                if (idfNewVersion != null) {

                    // Pramod | Attachment object is no longer required on
                    // creation of new version so it will be marked as DELETE
                    // and will be deleted at the end of the processing
                    idfAttacment.mark("DELETE");
                    idfAttacment.save();

                    DfLogger.info(this, " ---------------------- " + idfAttacment.getObjectName() +
                            " Marked as to be deleted ", null, null);

                    return idfNewVersion;
                }
            }
        }

        return idfAttacment;

    }

    private void processImageMetadata(
            final StringBuilder strGrpahicsFolderPath) throws DfException {

        Map<String, String> mapWebImageMetada = null;
        Map<String, String> mapPrintImageMetada = null;
        Map<String, String> mapOnlineImageMetada = null;
        boolean isOnlineImageExist = false;

        IDfDocument webImageObject = null;
        IDfDocument printImageObject = null;
        IDfDocument onlineImageObject = null;

        debug("Web Map : " + mapWeb);
        debug("Print Map : " + mapPrint);
        debug("Online Map : " + mapOnline);

        if (mapWeb.size() != mapPrint.size()) {
            error("************** Received metadata count is not same for Web and Print");
        }

        for (final Map.Entry<String, Map<String, String>> entry : mapWeb.entrySet()) {

            isOnlineImageExist = false;

            debug("Updating metadata for Image : " + entry.getKey());

            final String strWebImageName = entry.getKey();

            mapWebImageMetada = entry.getValue();

            if (mapPrint.containsKey(strWebImageName)) {
                mapPrintImageMetada = mapPrint.get(strWebImageName);
            }

            if (mapOnline.containsKey(strWebImageName)) {
                mapOnlineImageMetada = mapOnline.get(strWebImageName);
                isOnlineImageExist = true;
            }

            debug(" is Online Image exisit for " + strWebImageName + " : " + isOnlineImageExist);

            webImageObject = (IDfDocument) idfSession.getObjectByQualification(
                    IConstants.TYPE_IMAGE_WORKITEM_OBJECT + " " + " where object_name ='" +
                            mapWebImageMetada.get(IConstants.STR_XPATH_NAME) + "' " +
                            " and item_type = '" + IConstants.ITEM_TYPE_WEB_IMAGE + "' " +
                            " and folder('" + strGrpahicsFolderPath + "',descend)");

            debug("DQL : " + //
                    IConstants.TYPE_IMAGE_WORKITEM_OBJECT + " " + " where object_name ='" +
                    mapWebImageMetada.get(IConstants.STR_XPATH_NAME) + "' " + " and item_type = '" +
                    IConstants.ITEM_TYPE_WEB_IMAGE + "' " + " and folder('" +
                    strGrpahicsFolderPath + "',descend)");

            printImageObject = (IDfDocument) idfSession.getObjectByQualification(
                    IConstants.TYPE_IMAGE_WORKITEM_OBJECT + " " + " where object_name ='" +
                            mapPrintImageMetada.get(IConstants.STR_XPATH_NAME) + "' " +
                            " and item_type = '" + IConstants.ITEM_TYPE_PRINT_IMAGE + "' " +
                            " and folder('" + strGrpahicsFolderPath + "',descend)");

            debug("DQL : " + IConstants.TYPE_IMAGE_WORKITEM_OBJECT + " " + " where object_name ='" +
                    mapPrintImageMetada.get(IConstants.STR_XPATH_NAME) + "' " +
                    " and item_type = '" + IConstants.ITEM_TYPE_PRINT_IMAGE + "' " +
                    " and folder('" + strGrpahicsFolderPath + "',descend)");

            if ((webImageObject == null) || (printImageObject == null)) {
                error("Web or Print object does not found, image metadata processing can not be done");
                continue;
            }

            if (isOnlineImageExist) {
                onlineImageObject = (IDfDocument) idfSession.getObjectByQualification(
                        IConstants.TYPE_IMAGE_WORKITEM_OBJECT + " " + " where object_name like '" +
                                mapOnlineImageMetada.get(IConstants.STR_XPATH_NAME).substring(0,
                                        mapOnlineImageMetada.get(IConstants.STR_XPATH_NAME).indexOf(
                                                ".")) + "%'" + " and item_type = '" +
                                IConstants.ITEM_TYPE_ONLINE_IMAGE + "' " + " and folder('" +
                                strGrpahicsFolderPath + "',descend)");

                debug("DQL : " + IConstants.TYPE_IMAGE_WORKITEM_OBJECT + " " +
                        " where object_name like '" + mapOnlineImageMetada.get(
                                IConstants.STR_XPATH_NAME).substring(0, mapOnlineImageMetada.get(
                                        IConstants.STR_XPATH_NAME).indexOf(".")) + "%'" +
                        " and item_type = '" + IConstants.ITEM_TYPE_ONLINE_IMAGE + "' " +
                        " and folder('" + strGrpahicsFolderPath + "',descend)");

                if (onlineImageObject != null) {
                    setImageMetadata(mapOnlineImageMetada, onlineImageObject);
                    onlineImageObject.setString(IConstants.ATTR_WORKITEM_ITEM_STATUS,
                            IConstants.STATUS_WORK_ITEM_SUCCESS);
                    onlineImageObject.save();

                    debug("Online Image object saved successfully :  " + onlineImageObject
                            .getObjectName());
                }
            }

            setImageMetadata(mapWebImageMetada, webImageObject);
            setImageMetadata(mapPrintImageMetada, printImageObject);

            if (isOnlineImageExist == false) {
                // If no online image and no error for web\print..
                if (StringUtils.isEmpty(mapWebImageMetada.get(IConstants.STR_XPATH_ERROR)) &&
                        StringUtils.isEmpty(mapPrintImageMetada.get(IConstants.STR_XPATH_ERROR))) {

                    debug("Marking Images as Error- Online image did not received from ELF");

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

                    debug("Marking Print Images as error given by ELF and web image as error in print image");

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
                    debug("Marking Web Image as error given by ELF and print image as error in web image");

                    webImageObject.setString(IConstants.ATTR_WORKITEM_ITEM_STATUS,
                            IConstants.STATUS_WORK_ITEM_ERROR);
                    printImageObject.setString(IConstants.ATTR_WORKITEM_ITEM_STATUS,
                            IConstants.STATUS_WORK_ITEM_ERROR);

                    webImageObject.setString(IConstants.ATTR_WORKITEM_ERROR_DESC, mapWebImageMetada
                            .get(IConstants.STR_XPATH_ERROR));
                    printImageObject.setString(IConstants.ATTR_WORKITEM_ERROR_DESC,
                            IConstants.ERROR_MSG_WEB_IMG_ELF);
                } else {
                    debug("Marking Web and Print Image as error given by ELF");

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

                debug("Marking images as SUCCESS");
                webImageObject.setString(IConstants.ATTR_WORKITEM_ITEM_STATUS,
                        IConstants.STATUS_WORK_ITEM_SUCCESS);
                printImageObject.setString(IConstants.ATTR_WORKITEM_ITEM_STATUS,
                        IConstants.STATUS_WORK_ITEM_SUCCESS);
            }

            webImageObject.save();
            printImageObject.save();

            debug("Web and Print Image objects saved successfully :  " + webImageObject
                    .getObjectName());
        }
    }

    private void parseElfMetadataXml(
            final IDfDocument logObject) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, DfException {

        debug("Parsing Metadata XML received from ELF");

        final DocumentBuilderFactory docb = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBldr = null;

        docBldr = docb.newDocumentBuilder();
        final Document doc = docBldr.parse(logObject.getContent());

        final Element element = doc.getDocumentElement();

        final NodeList nodeLst = element.getElementsByTagName("Image");

        final XPath xPath = XPathFactory.newInstance().newXPath();
        String strXpathValue = "";
        /*
         * String strXpathPrefix = "(//"; String strXpathSuffix = ")";
         */

        for (int cnt = 1; cnt <= nodeLst.getLength(); cnt++) {

            final String imageName = xPath.compile("(//Image/@Name)" + "[" + cnt + "]").evaluate(
                    doc);

            debug("Image Name : " + imageName);

            final Map<String, String> webMetadataValues = new HashMap<String, String>();
            final Map<String, String> printMetadataValues = new HashMap<String, String>();
            final Map<String, String> onlineMetadataValues = new HashMap<String, String>();
            strXpathValue = "";

            for (final String element2 : IConstants.STR_ARR_WEB_XPATHS) {
                // strXpathValue = xPath.compile(strXpathPrefix + "Web" + "/" +
                // IConstants.STR_ARR_WEB_XPATHS[i] + strXpathSuffix +
                // "["+cnt+"]").evaluate(doc);
                strXpathValue = xPath.compile("//Image[@Name='" + imageName + "']/Web" + "/" +
                        element2).evaluate(doc);
                webMetadataValues.put(element2, strXpathValue);
                DfLogger.debug(this, "Web metadata : " + strXpathValue, null, null);
            }

            String strErrorExpression = "//Image[@Name='" + imageName + "']/Web/Error";
            String strErrorDesc = xPath.compile(strErrorExpression).evaluate(doc);

            webMetadataValues.put(IConstants.STR_XPATH_ERROR, strErrorDesc);

            String strWarnExpression = "//Image[@Name='" + imageName + "']/Web/Warning";
            String strWarnDesc = xPath.compile(strWarnExpression).evaluate(doc);

            webMetadataValues.put(IConstants.STR_XPATH_WARNING, strWarnDesc);

            debug("Web Map : " + webMetadataValues);

            mapWeb.put(imageName, webMetadataValues);

            strXpathValue = "";

            for (final String element2 : IConstants.STR_ARR_PRINT_XPATHS) {
                // strXpathValue = xPath.compile(strXpathPrefix + "Print" + "/"
                // + IConstants.STR_ARR_PRINT_XPATHS[i] + strXpathSuffix
                // +"["+cnt+"]").evaluate(doc);

                strXpathValue = xPath.compile("//Image[@Name='" + imageName + "']/Print" + "/" +
                        element2).evaluate(doc);
                printMetadataValues.put(element2, strXpathValue);
                DfLogger.debug(this, "Print metadata : " + strXpathValue, null, null);
            }

            strErrorExpression = "//Image[@Name='" + imageName + "']/Print/Error";
            strErrorDesc = xPath.compile(strErrorExpression).evaluate(doc);

            printMetadataValues.put(IConstants.STR_XPATH_ERROR, strErrorDesc);

            strWarnExpression = "//Image[@Name='" + imageName + "']/Print/Warning";
            strWarnDesc = xPath.compile(strWarnExpression).evaluate(doc);

            printMetadataValues.put(IConstants.STR_XPATH_WARNING, strWarnDesc);

            debug("Print Map : " + printMetadataValues);

            mapPrint.put(imageName, printMetadataValues);

            strXpathValue = "";

            for (final String element2 : IConstants.STR_ARR_WEB_XPATHS) {
                // strXpathValue = xPath.compile(strXpathPrefix + "Online" + "/"
                // + IConstants.STR_ARR_WEB_XPATHS[i] + strXpathSuffix
                // +"["+cnt+"]").evaluate(doc);

                strXpathValue = xPath.compile("//Image[@Name='" + imageName + "']/Online" + "/" +
                        element2).evaluate(doc);
                onlineMetadataValues.put(element2, strXpathValue);
                DfLogger.debug(this, "Online metadata : " + strXpathValue, null, null);
            }

            strErrorExpression = "//Image[@Name='" + imageName + "']/Online/Error";
            strErrorDesc = xPath.compile(strErrorExpression).evaluate(doc);

            onlineMetadataValues.put(IConstants.STR_XPATH_ERROR, strErrorDesc);

            strWarnExpression = "//Image[@Name='" + imageName + "']/Online/Warning";
            strWarnDesc = xPath.compile(strWarnExpression).evaluate(doc);

            onlineMetadataValues.put(IConstants.STR_XPATH_WARNING, strWarnDesc);

            debug("Online Map : " + onlineMetadataValues);

            if ("".equals(onlineMetadataValues.get(IConstants.STR_XPATH_NAME))) {
                debug("Online Image does not exist for image : " + imageName);
            } else {
                mapOnline.put(imageName, onlineMetadataValues);
            }

            debug("Metadata XML parsing completed successfully");
        }
    }

    private void setImageMetadata(final Map<String, String> metadataMap,
            final IDfDocument imageObject) throws DfException {

        if (metadataMap.containsKey(IConstants.STR_XPATH_NAME)) {
            imageObject.setString(IConstants.OBJECT_NAME, metadataMap.get(
                    IConstants.STR_XPATH_NAME));
        }

        if (metadataMap.containsKey(IConstants.STR_XPATH_COLOR)) {
            imageObject.setString(IConstants.ATTR_WORKITEM_IMG_COLOR, metadataMap.get(
                    IConstants.STR_XPATH_COLOR));
        }
        if (metadataMap.containsKey(IConstants.STR_XPATH_FORMAT)) {
            imageObject.setString(IConstants.ATTR_WORKITEM_IMG_FORMAT, metadataMap.get(
                    IConstants.STR_XPATH_FORMAT));
        }
        if (metadataMap.containsKey(IConstants.STR_XPATH_HEIGHT)) {
            imageObject.setString(IConstants.ATTR_WORKITEM_IMG_HEIGHT, metadataMap.get(
                    IConstants.STR_XPATH_HEIGHT));
        }
        if (metadataMap.containsKey(IConstants.STR_XPATH_WIDTH)) {
            imageObject.setString(IConstants.ATTR_WORKITEM_IMG_WIDTH, metadataMap.get(
                    IConstants.STR_XPATH_WIDTH));
        }
        if (metadataMap.containsKey(IConstants.STR_XPATH_RENDITION)) {
            imageObject.setString(IConstants.ATTR_WORKITEM_IMG_RENDITION, metadataMap.get(
                    IConstants.STR_XPATH_RENDITION));
        }
        if (metadataMap.containsKey(IConstants.STR_XPATH_TYPE)) {
            imageObject.setString(IConstants.ATTR_WORKITEM_IMG_TYPE, metadataMap.get(
                    IConstants.STR_XPATH_TYPE));
        }
        if (metadataMap.containsKey(IConstants.STR_XPATH_WIDTH)) {
            imageObject.setString(IConstants.ATTR_WORKITEM_IMG_WIDTH, metadataMap.get(
                    IConstants.STR_XPATH_WIDTH));
        }
        if (metadataMap.containsKey(IConstants.STR_XPATH_COLOR_ON_PAGE)) {
            imageObject.setString(IConstants.ATTR_WORKITEM_IMG_COLOR_ON_PAGE, metadataMap.get(
                    IConstants.STR_XPATH_COLOR_ON_PAGE));
        }

        imageObject.setString(IConstants.ATTR_WORKITEM_ERROR_DESC, "");
        imageObject.setString(IConstants.ATTR_WORKITEM_WARNING_DESC, "");

        if (metadataMap.containsKey(IConstants.STR_XPATH_WARNING)) {
            imageObject.setString(IConstants.ATTR_WORKITEM_WARNING_DESC, metadataMap.get(
                    IConstants.STR_XPATH_WARNING));
        }
        if (metadataMap.containsKey(IConstants.STR_XPATH_ERROR)) {
            imageObject.setString(IConstants.ATTR_WORKITEM_ERROR_DESC, metadataMap.get(
                    IConstants.STR_XPATH_ERROR));
        }

        imageObject.save();
    }

}
