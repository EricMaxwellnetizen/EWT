package com.htc.enter.util;

import com.htc.enter.dto.DocumentPayload;
import com.htc.enter.model.User;

public final class DocumentPayloadFactory {

    private DocumentPayloadFactory() {
    }

    public static String resolvePassword(User primaryUser, User fallbackUser) {
        return DocumentPasswordUtil.resolvePassword(primaryUser, fallbackUser);
    }

    public static DocumentPayload docx(byte[] content, String filename) {
        return DocumentPayload.ofDocx(content, filename);
    }
}