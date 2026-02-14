package com.htc.enter.serviceimpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.htc.enter.model.User;
import com.htc.enter.service.UserDocumentService;

@Service
public class UserDocumentServiceImpl extends BaseDocumentService implements UserDocumentService {

    @Override
    @Transactional(readOnly = true)
    public ByteArrayOutputStream generatePasswordProtectedUsersDocument(
            List<User> users, String password) throws IOException {
        return generatePasswordProtectedDocument(
                "User Directory",
                "No users found.",
                users,
                password,
                this::addUserContent);
    }

    private void addUserContent(XWPFDocument doc, User user) {
        XWPFParagraph p = createSectionHeader(doc, "User: " + user.getUsername() + " (ID: " + user.getId() + ")");
        
        XWPFRun r2 = p.createRun();
        addField(r2, "Email", user.getEmail());
        addField(r2, "Role", user.getRole());
        addField(r2, "Access Level", user.getAccessLevel());
        addField(r2, "Job Title", user.getJob_title());
        addField(r2, "Department", user.getDepartment());
        addField(r2, "Joining Date", user.getJoining_date());
        addField(r2, "Reporting To", user.getReportingTo() != null ? 
            user.getReportingTo().getUsername() + " (ID: " + user.getReportingTo().getId() + ")" : null);
        addSectionSpacing(r2);
    }
}