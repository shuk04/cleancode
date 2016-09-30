package com.spr.ajwf.workflow_methods.file_operations.refactored_elf;

import com.spr.ajwf.constants.IConstants;

public interface Conditions {

    boolean isType(String attachment);

    AttachmentTypeData getAttachmentTypeData(final String objectName);

    public enum ItemTypeConditions implements Conditions {
        ITEM_TYPE_CHECK_PDF_ONLINE {

            @Override
            public boolean isType(final String objectName) {
                return objectName.toLowerCase().contains(IConstants.STR_CHECK_PDF_ONLINE) &&
                        objectName.toLowerCase().endsWith(".pdf");
            }

            @Override
            public AttachmentTypeData getAttachmentTypeData(final String objectName) {
                return new AttachmentTypeData(IConstants.TYPE_WORKITEM_OBJECT,
                        IConstants.ITEM_TYPE_CHECK_PDF_ONLINE, IConstants.PATH_GRAPHICS_FOLDER);

            }
        },
        ITEM_TYPE_CHECK_PDF_PRINT {

            @Override
            public boolean isType(final String objectName) {
                return objectName.toLowerCase().contains(IConstants.STR_CHECK_PDF_PRINT) &&
                        objectName.toLowerCase().endsWith(".pdf");
            }

            @Override
            public AttachmentTypeData getAttachmentTypeData(final String objectName) {
                return new AttachmentTypeData(IConstants.TYPE_WORKITEM_OBJECT,
                        IConstants.ITEM_TYPE_CHECK_PDF_ONLINE, IConstants.PATH_GRAPHICS_FOLDER);

            }
        },
        STR_COLOR_ON_PAGE_PRINT {

            @Override
            public boolean isType(final String objectName) {
                return objectName.toLowerCase().contains(IConstants.STR_COLOR_ON_PAGE_PRINT) &&
                        objectName.toLowerCase().endsWith(".pdf");
            }

            @Override
            public AttachmentTypeData getAttachmentTypeData(final String objectName) {
                return new AttachmentTypeData(IConstants.TYPE_WORKITEM_OBJECT,
                        IConstants.ITEM_TYPE_CHECK_PDF_ONLINE, IConstants.PATH_GRAPHICS_FOLDER);

            }
        },
        TYPE_LOG_OBJECT {

            @Override
            public boolean isType(final String objectName) {
                return objectName.toLowerCase().contains(IConstants.STR_COLOR_ON_PAGE_PRINT) &&
                        objectName.toLowerCase().endsWith(".pdf");
            }

            @Override
            public AttachmentTypeData getAttachmentTypeData(final String objectName) {
                return new AttachmentTypeData(IConstants.TYPE_WORKITEM_OBJECT,
                        IConstants.ITEM_TYPE_CHECK_PDF_ONLINE, IConstants.PATH_GRAPHICS_FOLDER);

            }
        },
        ITEM_TYPE_ONLINE_IMAGE {

            @Override
            public boolean isType(final String objectName) {

                return objectName.toLowerCase().contains(IConstants.IMAGE_RENDITION_HTML) &&
                        (objectName.toLowerCase().endsWith(".jpg") || objectName.toLowerCase()
                                .endsWith(".gif"));
            };

            @Override
            public AttachmentTypeData getAttachmentTypeData(final String objectName) {
                return new AttachmentTypeData(IConstants.TYPE_WORKITEM_OBJECT,
                        IConstants.ITEM_TYPE_CHECK_PDF_ONLINE, IConstants.PATH_GRAPHICS_FOLDER);

            }
        };

        public static AttachmentTypeData getData(final String objectName) {
            for (final Conditions data : ItemTypeConditions.values()) {
                if (data.isType(objectName)) {
                    return data.getAttachmentTypeData(objectName);
                }
            }
            throw new IllegalArgumentException();
        }
    }

}
