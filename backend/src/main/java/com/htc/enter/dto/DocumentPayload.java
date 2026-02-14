package com.htc.enter.dto;

public class DocumentPayload {
    private final byte[] content;
    private final String filename;
    private final String contentType;

    public static final String DOCX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    public DocumentPayload(byte[] content, String filename, String contentType) {
        this.content = content;
        this.filename = filename;
        this.contentType = contentType;
    }

    public static DocumentPayload ofDocx(byte[] content, String filename) {
        return new DocumentPayload(content, filename, DOCX_CONTENT_TYPE);
    }

    public byte[] getContent() { return content; }
    public String getFilename() { return filename; }
    public String getContentType() { return contentType; }
}