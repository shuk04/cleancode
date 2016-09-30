package com.spr.ajwf.workflow_methods.file_operations.refactored_elf;

import java.util.Map;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.common.DfException;
import com.spr.ajwf.constants.IConstants;

public class SetImageMetadata {

    private final Map<String, String> mapOnlineImageMetada;

    private final IDfDocument onlineImageObject;

    public SetImageMetadata(final Map<String, String> mapOnlineImageMetada,
                            final IDfDocument onlineImageObject) {
        this.mapOnlineImageMetada = mapOnlineImageMetada;
        this.onlineImageObject = onlineImageObject;
    }

    public void setImageMetadata() throws DfException {

        if (mapOnlineImageMetada.containsKey(IConstants.STR_XPATH_NAME)) {
            onlineImageObject.setString(IConstants.OBJECT_NAME, mapOnlineImageMetada.get(
                    IConstants.STR_XPATH_NAME));
        }

        if (mapOnlineImageMetada.containsKey(IConstants.STR_XPATH_COLOR)) {
            onlineImageObject.setString(IConstants.ATTR_WORKITEM_IMG_COLOR, mapOnlineImageMetada
                    .get(IConstants.STR_XPATH_COLOR));
        }
        if (mapOnlineImageMetada.containsKey(IConstants.STR_XPATH_FORMAT)) {
            onlineImageObject.setString(IConstants.ATTR_WORKITEM_IMG_FORMAT, mapOnlineImageMetada
                    .get(IConstants.STR_XPATH_FORMAT));
        }
        if (mapOnlineImageMetada.containsKey(IConstants.STR_XPATH_HEIGHT)) {
            onlineImageObject.setString(IConstants.ATTR_WORKITEM_IMG_HEIGHT, mapOnlineImageMetada
                    .get(IConstants.STR_XPATH_HEIGHT));
        }
        if (mapOnlineImageMetada.containsKey(IConstants.STR_XPATH_WIDTH)) {
            onlineImageObject.setString(IConstants.ATTR_WORKITEM_IMG_WIDTH, mapOnlineImageMetada
                    .get(IConstants.STR_XPATH_WIDTH));
        }
        if (mapOnlineImageMetada.containsKey(IConstants.STR_XPATH_RENDITION)) {
            onlineImageObject.setString(IConstants.ATTR_WORKITEM_IMG_RENDITION, mapOnlineImageMetada
                    .get(IConstants.STR_XPATH_RENDITION));
        }
        if (mapOnlineImageMetada.containsKey(IConstants.STR_XPATH_TYPE)) {
            onlineImageObject.setString(IConstants.ATTR_WORKITEM_IMG_TYPE, mapOnlineImageMetada.get(
                    IConstants.STR_XPATH_TYPE));
        }
        if (mapOnlineImageMetada.containsKey(IConstants.STR_XPATH_WIDTH)) {
            onlineImageObject.setString(IConstants.ATTR_WORKITEM_IMG_WIDTH, mapOnlineImageMetada
                    .get(IConstants.STR_XPATH_WIDTH));
        }
        if (mapOnlineImageMetada.containsKey(IConstants.STR_XPATH_COLOR_ON_PAGE)) {
            onlineImageObject.setString(IConstants.ATTR_WORKITEM_IMG_COLOR_ON_PAGE,
                    mapOnlineImageMetada.get(IConstants.STR_XPATH_COLOR_ON_PAGE));
        }

        onlineImageObject.setString(IConstants.ATTR_WORKITEM_ERROR_DESC, "");
        onlineImageObject.setString(IConstants.ATTR_WORKITEM_WARNING_DESC, "");

        if (mapOnlineImageMetada.containsKey(IConstants.STR_XPATH_WARNING)) {
            onlineImageObject.setString(IConstants.ATTR_WORKITEM_WARNING_DESC, mapOnlineImageMetada
                    .get(IConstants.STR_XPATH_WARNING));
        }
        if (mapOnlineImageMetada.containsKey(IConstants.STR_XPATH_ERROR)) {
            onlineImageObject.setString(IConstants.ATTR_WORKITEM_ERROR_DESC, mapOnlineImageMetada
                    .get(IConstants.STR_XPATH_ERROR));
        }

        onlineImageObject.save();
    }

}
