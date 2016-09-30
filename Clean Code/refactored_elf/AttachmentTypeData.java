package com.spr.ajwf.workflow_methods.file_operations.refactored_elf;

public class AttachmentTypeData {

    String ObjectType = "";

    String ItemType = "";

    String folderPah;

    boolean isLogObject = false;

    public AttachmentTypeData(final String objectType, final String itemType,
                              final String pathGraphicsFolder) {
        super();
        ObjectType = objectType;
        ItemType = itemType;
        folderPah = pathGraphicsFolder;
    }

    public String getObjectType() {
        return ObjectType;
    }

    public void setObjectType(final String objectType) {
        ObjectType = objectType;
    }

    public String getItemType() {
        return ItemType;
    }

    public void setItemType(final String itemType) {
        ItemType = itemType;
    }

    public boolean isLogObject() {
        return isLogObject;
    }

    public void setLogObject(final boolean isLogObject) {
        this.isLogObject = isLogObject;
    }

    public boolean isImageObject() {
        return isImageObject;
    }

    public void setImageObject(final boolean isImageObject) {
        this.isImageObject = isImageObject;
    }

    public String getLogCategory() {
        return logCategory;
    }

    public void setLogCategory(final String logCategory) {
        this.logCategory = logCategory;
    }

    public String getLogType() {
        return logType;
    }

    boolean isImageObject = false;

    String logCategory = "";

    final String logType = "";

}
