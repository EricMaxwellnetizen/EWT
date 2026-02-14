package com.htc.enter.serviceimpl;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.htc.enter.model.Epic;
import com.htc.enter.model.Project;
import com.htc.enter.model.Story;
import com.htc.enter.service.ReportService;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class ReportServiceImpl implements ReportService {

    private final DateTimeFormatter df = DateTimeFormatter.ISO_LOCAL_DATE;

    @Transactional(readOnly = true)
    private byte[] projectsToExcel(List<Project> projects) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = wb.createSheet("Projects");
            int row = 0;
            org.apache.poi.ss.usermodel.Row header = sheet.createRow(row++);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Name");
            header.createCell(2).setCellValue("Manager");
            header.createCell(3).setCellValue("Created By");
            header.createCell(4).setCellValue("Deadline");
            header.createCell(5).setCellValue("Approved");
            header.createCell(6).setCellValue("End Date");
            for (Project p : projects) {
                org.apache.poi.ss.usermodel.Row r = sheet.createRow(row++);
                r.createCell(0).setCellValue(p.getProjectId());
                r.createCell(1).setCellValue(p.getName());
                r.createCell(2).setCellValue(p.getManager_id() != null ? p.getManager_id().getUsername() : "");
                r.createCell(3).setCellValue(p.getCreated_by() != null ? p.getCreated_by().getUsername() : "");
                r.createCell(4).setCellValue(p.getDeadline() != null ? p.getDeadline().format(df) : "");
                r.createCell(5).setCellValue(p.isIs_approved());
                r.createCell(6).setCellValue(p.getIs_end() != null ? p.getIs_end().format(df) : "");
            }
            wb.write(out);
            return out.toByteArray();
        }
    }

    @Transactional(readOnly = true)
    private byte[] epicsToExcel(List<Epic> epics) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = wb.createSheet("Epics");
            int row = 0;
            org.apache.poi.ss.usermodel.Row header = sheet.createRow(row++);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Name");
            header.createCell(2).setCellValue("Project");
            header.createCell(3).setCellValue("Deadline");
            header.createCell(4).setCellValue("Approved");
            header.createCell(5).setCellValue("End Date");
            for (Epic e : epics) {
                org.apache.poi.ss.usermodel.Row r = sheet.createRow(row++);
                r.createCell(0).setCellValue(e.getEpicId());
                r.createCell(1).setCellValue(e.getName());
                r.createCell(2).setCellValue(e.getProjectId() != null ? e.getProjectId().getName() : "");
                r.createCell(3).setCellValue(e.getDeadline() != null ? e.getDeadline().format(df) : "");
                r.createCell(4).setCellValue(e.isIs_approved());
                r.createCell(5).setCellValue(e.getIs_end() != null ? e.getIs_end().format(df) : "");
            }
            wb.write(out);
            return out.toByteArray();
        }
    }

    @Transactional(readOnly = true)
    private byte[] storiesToExcel(List<Story> stories) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = wb.createSheet("Stories");
            int row = 0;
            org.apache.poi.ss.usermodel.Row header = sheet.createRow(row++);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Title");
            header.createCell(2).setCellValue("Project");
            header.createCell(3).setCellValue("Epic");
            header.createCell(4).setCellValue("Manager");
            header.createCell(5).setCellValue("Assigned To");
            header.createCell(6).setCellValue("Deadline");
            header.createCell(7).setCellValue("Approved");
            header.createCell(8).setCellValue("End Date");
            for (Story s : stories) {
                org.apache.poi.ss.usermodel.Row r = sheet.createRow(row++);
                r.createCell(0).setCellValue(s.getStoryId());
                r.createCell(1).setCellValue(s.getTitle());
                r.createCell(2).setCellValue(s.getProjectId() != null ? s.getProjectId().getName() : "");
                r.createCell(3).setCellValue(s.getEpicId() != null ? s.getEpicId().getName() : "");
                r.createCell(4).setCellValue(s.getManager() != null ? s.getManager().getUsername() : "");
                r.createCell(5).setCellValue(s.getAssigned_to() != null ? s.getAssigned_to().getUsername() : "");
                r.createCell(6).setCellValue(s.getDeadline() != null ? s.getDeadline().format(df) : "");
                r.createCell(7).setCellValue(s.isIs_approved());
                r.createCell(8).setCellValue(s.getIs_end() != null ? s.getIs_end().format(df) : "");
            }
            wb.write(out);
            return out.toByteArray();
        }
    }

    private byte[] projectsToWord(List<Project> projects, String password) throws Exception {
        ByteArrayOutputStream tempStream = new ByteArrayOutputStream();
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFParagraph p = doc.createParagraph();
            XWPFRun r = p.createRun();
            r.setText("Projects Report\n");
            for (Project proj : projects) {
                XWPFParagraph p2 = doc.createParagraph();
                XWPFRun run = p2.createRun();
                run.setText(String.format("%d - %s - Manager:%s - Approved:%s - End:%s",
                        proj.getProjectId(), proj.getName(),
                        proj.getManager_id() != null ? proj.getManager_id().getUsername() : "",
                        proj.isIs_approved(), proj.getIs_end() != null ? proj.getIs_end().format(df) : ""));
            }
            doc.write(tempStream);
        }
        return encryptDoc(tempStream, password);
    }

    private byte[] epicsToWord(List<Epic> epics, String password) throws Exception {
        ByteArrayOutputStream tempStream = new ByteArrayOutputStream();
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFParagraph p = doc.createParagraph();
            p.createRun().setText("Epics Report\n");
            for (Epic e : epics) {
                XWPFParagraph p2 = doc.createParagraph();
                p2.createRun().setText(String.format("%d - %s - Project:%s - Approved:%s - End:%s",
                        e.getEpicId(), e.getName(), e.getProjectId() != null ? e.getProjectId().getName() : "",
                        e.isIs_approved(), e.getIs_end() != null ? e.getIs_end().format(df) : ""));
            }
            doc.write(tempStream);
        }
        return encryptDoc(tempStream, password);
    }

    private byte[] storiesToWord(List<Story> stories, String password) throws Exception {
        ByteArrayOutputStream tempStream = new ByteArrayOutputStream();
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFParagraph p = doc.createParagraph();
            p.createRun().setText("Stories Report\n");
            for (Story s : stories) {
                XWPFParagraph p2 = doc.createParagraph();
                p2.createRun().setText(String.format("%d - %s - Project:%s - Epic:%s - Manager:%s - Approved:%s - End:%s",
                        s.getStoryId(), s.getTitle(), s.getProjectId() != null ? s.getProjectId().getName() : "",
                        s.getEpicId() != null ? s.getEpicId().getName() : "",
                        s.getManager() != null ? s.getManager().getUsername() : "",
                        s.isIs_approved(), s.getIs_end() != null ? s.getIs_end().format(df) : ""));
            }
            doc.write(tempStream);
        }
        return encryptDoc(tempStream, password);
    }

    private byte[] projectsToPdf(List<Project> projects, String password) throws Exception {
        Document doc = new Document();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            writer.setEncryption(password.getBytes(), password.getBytes(), 
                PdfWriter.ALLOW_PRINTING, PdfWriter.ENCRYPTION_AES_128);
            doc.open();
            doc.add(new Paragraph("Projects Report"));
            for (Project p : projects) {
                doc.add(new Paragraph(String.format("%d - %s - Manager:%s - Approved:%s - End:%s",
                        p.getProjectId(), p.getName(), p.getManager_id() != null ? p.getManager_id().getUsername() : "",
                        p.isIs_approved(), p.getIs_end() != null ? p.getIs_end().format(df) : "")));
            }
            doc.close();
            return out.toByteArray();
        }
    }

    private byte[] epicsToPdf(List<Epic> epics, String password) throws Exception {
        Document doc = new Document();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            writer.setEncryption(password.getBytes(), password.getBytes(), 
                PdfWriter.ALLOW_PRINTING, PdfWriter.ENCRYPTION_AES_128);
            doc.open();
            doc.add(new Paragraph("Epics Report"));
            for (Epic e : epics) {
                doc.add(new Paragraph(String.format("%d - %s - Project:%s - Approved:%s - End:%s",
                        e.getEpicId(), e.getName(), e.getProjectId() != null ? e.getProjectId().getName() : "",
                        e.isIs_approved(), e.getIs_end() != null ? e.getIs_end().format(df) : "")));
            }
            doc.close();
            return out.toByteArray();
        }
    }

    private byte[] storiesToPdf(List<Story> stories, String password) throws Exception {
        Document doc = new Document();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            writer.setEncryption(password.getBytes(), password.getBytes(), 
                PdfWriter.ALLOW_PRINTING, PdfWriter.ENCRYPTION_AES_128);
            doc.open();
            doc.add(new Paragraph("Stories Report"));
            for (Story s : stories) {
                doc.add(new Paragraph(String.format("%d - %s - Project:%s - Epic:%s - Manager:%s - Approved:%s - End:%s",
                        s.getStoryId(), s.getTitle(), s.getProjectId() != null ? s.getProjectId().getName() : "",
                        s.getEpicId() != null ? s.getEpicId().getName() : "",
                        s.getManager() != null ? s.getManager().getUsername() : "",
                        s.isIs_approved(), s.getIs_end() != null ? s.getIs_end().format(df) : "")));
            }
            doc.close();
            return out.toByteArray();
        }
    }

    
    /**
     * Encrypts a Word document with a password using AES-256 agile encryption
     */
    private byte[] encryptDoc(ByteArrayOutputStream tempStream, String password) throws Exception {
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
            throw new Exception("Failed to encrypt document", e);
        }
        return outputStream.toByteArray();
    }

    @Override
    public byte[] exportProjectsExcel(List<Project> projects) throws Exception { return projectsToExcel(projects); }

    @Override
    public byte[] exportProjectsWord(List<Project> projects, String password) throws Exception { return projectsToWord(projects, password); }

    @Override
    public byte[] exportProjectsPdf(List<Project> projects, String password) throws Exception { return projectsToPdf(projects, password); }

    @Override
    public byte[] exportEpicsExcel(List<Epic> epics) throws Exception { return epicsToExcel(epics); }

    @Override
    public byte[] exportEpicsWord(List<Epic> epics, String password) throws Exception { return epicsToWord(epics, password); }

    @Override
    public byte[] exportEpicsPdf(List<Epic> epics, String password) throws Exception { return epicsToPdf(epics, password); }

    @Override
    public byte[] exportStoriesExcel(List<Story> stories) throws Exception { return storiesToExcel(stories); }

    @Override
    public byte[] exportStoriesWord(List<Story> stories, String password) throws Exception { return storiesToWord(stories, password); }

    @Override
    public byte[] exportStoriesPdf(List<Story> stories, String password) throws Exception { return storiesToPdf(stories, password); }
}
