package com.htc.enter.serviceimpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

/**
 * Base class for all document service implementations.
 * Provides common functionality for document encryption and formatting.
 */
public abstract class BaseDocumentService {

    /**
     * Encrypts a Word document with a password using AES-256 agile encryption
     * 
     * @param tempStream the unencrypted document stream
     * @param password the password to protect the document
     * @return encrypted document stream
     * @throws IOException if encryption fails
     */
    protected ByteArrayOutputStream encryptDoc(ByteArrayOutputStream tempStream, String password) 
            throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            POIFSFileSystem fs = new POIFSFileSystem();
            EncryptionInfo info = new EncryptionInfo(EncryptionMode.agile);
            Encryptor encryptor = info.getEncryptor();
            encryptor.confirmPassword(password);
            try (OPCPackage opc = OPCPackage.open(new java.io.ByteArrayInputStream(tempStream.toByteArray()))) {
                java.io.OutputStream os = encryptor.getDataStream(fs);
                opc.save(os);
                os.close();
            }
            fs.writeFilesystem(outputStream);
            fs.close();
        } catch (Exception e) {
            throw new IOException("Failed to encrypt document", e);
        }
        return outputStream;
    }

    /**
     * Creates a centered title paragraph with bold, large font
     * 
     * @param doc the document to add the title to
     * @param titleText the title text
     * @return the created run for further customization if needed
     */
    protected XWPFRun createDocumentTitle(XWPFDocument doc, String titleText) {
        XWPFParagraph title = doc.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = title.createRun();
        run.setBold(true);
        run.setFontSize(16);
        run.setText(titleText);
        run.addBreak();
        return run;
    }

    /**
     * Creates a section header paragraph with bold text
     * 
     * @param doc the document to add the header to
     * @param headerText the header text
     * @return the created paragraph
     */
    protected XWPFParagraph createSectionHeader(XWPFDocument doc, String headerText) {
        XWPFParagraph p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun r = p.createRun();
        r.setBold(true);
        r.setText(headerText);
        r.addBreak();
        return p;
    }

    /**
     * Adds a field with label and value to an existing paragraph
     * 
     * @param run the run to add the field to
     * @param label the field label
     * @param value the field value (will show "n/a" if null)
     */
    protected void addField(XWPFRun run, String label, Object value) {
        run.setText(label + ": " + (value != null ? value.toString() : "n/a"));
        run.addBreak();
    }

    /**
     * Creates an empty message paragraph
     * 
     * @param doc the document to add the message to
     * @param message the message text
     */
    protected void addEmptyMessage(XWPFDocument doc, String message) {
        XWPFParagraph p = doc.createParagraph();
        p.createRun().setText(message);
    }

    /**
     * Adds spacing between sections
     * 
     * @param run the run to add spacing to
     */
    protected void addSectionSpacing(XWPFRun run) {
        run.addBreak();
    }

    protected <T> ByteArrayOutputStream generatePasswordProtectedDocument(
            String titleText,
            String emptyMessage,
            java.util.List<T> items,
            String password,
            java.util.function.BiConsumer<XWPFDocument, T> contentWriter) throws IOException {
        ByteArrayOutputStream tempStream = new ByteArrayOutputStream();

        try (XWPFDocument doc = new XWPFDocument()) {
            createDocumentTitle(doc, titleText);

            if (items == null || items.isEmpty()) {
                addEmptyMessage(doc, emptyMessage);
            } else {
                for (T item : items) {
                    contentWriter.accept(doc, item);
                }
            }

            doc.write(tempStream);
        }

        return encryptDoc(tempStream, password);
    }
}