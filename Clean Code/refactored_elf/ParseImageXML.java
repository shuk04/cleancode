package com.spr.ajwf.workflow_methods.file_operations.refactored_elf;

import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.documentum.fc.client.IDfDocument;
import com.spr.ajwf.commons.logger.Logger;
import com.spr.ajwf.constants.IConstants;

public class ParseImageXML {

    private final IDfDocument logObject;

    public ParseImageXML(final IDfDocument logObject) {
        this.logObject = logObject;
    }

    public void parseElfMetadataXml() throws Exception {

        LOGGER.debug("Parsing Metadata XML received from ELF");

        final DocumentBuilderFactory docb = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBldr = null;

        docBldr = docb.newDocumentBuilder();
        final Document doc = docBldr.parse(logObject.getContent());
        final Element element = doc.getDocumentElement();
        final XPath xPath = XPathFactory.newInstance().newXPath();
        String strXpathValue = "";
        final NodeList nodeLst = element.getElementsByTagName("Image");

        for (int cnt = 1; cnt <= nodeLst.getLength(); cnt++) {
            final String imageName = xPath.compile("(//Image/@Name)" + "[" + cnt + "]").evaluate(
                    doc);

            LOGGER.debug("Image Name : " + imageName);

            final Map<String, String> webMetadataValues = new HashMap<String, String>();
            final Map<String, String> printMetadataValues = new HashMap<String, String>();
            final Map<String, String> onlineMetadataValues = new HashMap<String, String>();
            strXpathValue = "";

            for (final String element2 : IConstants.STR_ARR_WEB_XPATHS) {
                strXpathValue = xPath.compile("//Image[@Name='" + imageName + "']/Web" + "/" +
                        element2).evaluate(doc);
                webMetadataValues.put(element2, strXpathValue);
                LOGGER.debug("Web metadata : " + strXpathValue);
            }

            String strErrorExpression = "//Image[@Name='" + imageName + "']/Web/Error";
            String strErrorDesc = xPath.compile(strErrorExpression).evaluate(doc);

            webMetadataValues.put(IConstants.STR_XPATH_ERROR, strErrorDesc);

            String strWarnExpression = "//Image[@Name='" + imageName + "']/Web/Warning";
            String strWarnDesc = xPath.compile(strWarnExpression).evaluate(doc);

            webMetadataValues.put(IConstants.STR_XPATH_WARNING, strWarnDesc);

            LOGGER.debug("Web Map : " + webMetadataValues);

            mapWeb.put(imageName, webMetadataValues);

            strXpathValue = "";

            for (final String element2 : IConstants.STR_ARR_PRINT_XPATHS) {
                strXpathValue = xPath.compile("//Image[@Name='" + imageName + "']/Print" + "/" +
                        element2).evaluate(doc);
                printMetadataValues.put(element2, strXpathValue);
                LOGGER.debug("Print metadata : " + strXpathValue);
            }

            strErrorExpression = "//Image[@Name='" + imageName + "']/Print/Error";
            strErrorDesc = xPath.compile(strErrorExpression).evaluate(doc);

            printMetadataValues.put(IConstants.STR_XPATH_ERROR, strErrorDesc);

            strWarnExpression = "//Image[@Name='" + imageName + "']/Print/Warning";
            strWarnDesc = xPath.compile(strWarnExpression).evaluate(doc);

            printMetadataValues.put(IConstants.STR_XPATH_WARNING, strWarnDesc);

            LOGGER.debug("Print Map : " + printMetadataValues);

            mapPrint.put(imageName, printMetadataValues);

            strXpathValue = "";

            for (final String element2 : IConstants.STR_ARR_WEB_XPATHS) {
                strXpathValue = xPath.compile("//Image[@Name='" + imageName + "']/Online" + "/" +
                        element2).evaluate(doc);
                onlineMetadataValues.put(element2, strXpathValue);
                LOGGER.debug("Online metadata : " + strXpathValue);
            }

            strErrorExpression = "//Image[@Name='" + imageName + "']/Online/Error";
            strErrorDesc = xPath.compile(strErrorExpression).evaluate(doc);

            onlineMetadataValues.put(IConstants.STR_XPATH_ERROR, strErrorDesc);

            strWarnExpression = "//Image[@Name='" + imageName + "']/Online/Warning";
            strWarnDesc = xPath.compile(strWarnExpression).evaluate(doc);

            onlineMetadataValues.put(IConstants.STR_XPATH_WARNING, strWarnDesc);

            LOGGER.debug("Online Map : " + onlineMetadataValues);

            if ("".equals(onlineMetadataValues.get(IConstants.STR_XPATH_NAME))) {
                LOGGER.debug("Online Image does not exist for image : " + imageName);
            } else {
                mapOnline.put(imageName, onlineMetadataValues);
            }

            LOGGER.debug("Metadata XML parsing completed successfully");
        }
    }

    final Map<String, Map<String, String>> mapWeb = new HashMap<>();

    final Map<String, Map<String, String>> mapPrint = new HashMap<>();

    final Map<String, Map<String, String>> mapOnline = new HashMap<>();

    public Map<String, Map<String, String>> getMapWeb() {
        return mapWeb;
    }

    public Map<String, Map<String, String>> getMapPrint() {
        return mapPrint;
    }

    public Map<String, Map<String, String>> getMapOnline() {
        return mapOnline;
    }

    Logger LOGGER = new Logger(ProcessELFReceivedFilesRefactored.class);

}
