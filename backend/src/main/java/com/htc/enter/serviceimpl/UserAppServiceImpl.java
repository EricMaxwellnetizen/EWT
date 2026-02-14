package com.htc.enter.serviceimpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.htc.enter.dto.DocumentPayload;
import com.htc.enter.model.User;
import com.htc.enter.service.UserAppService;
import com.htc.enter.service.UserAuthService;
import com.htc.enter.service.UserDocumentService;
import com.htc.enter.service.UserService;
import com.htc.enter.util.DocumentPasswordUtil;

@Service
public class UserAppServiceImpl implements UserAppService {

    private final UserService userService;
    private final UserDocumentService documentService;
    private final UserAuthService authService;

    public UserAppServiceImpl(UserService userService,
                              UserDocumentService documentService,
                              UserAuthService authService) {
        this.userService = userService;
        this.documentService = documentService;
        this.authService = authService;
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentPayload buildUsersDocument() throws IOException {
        List<User> users = userService.findAll();
        User current = authService.getCurrentUser();
        String password = DocumentPasswordUtil.resolvePassword(current, null);

        ByteArrayOutputStream os = documentService.generatePasswordProtectedUsersDocument(users, password);
        String fileName = "users_report.docx";
        return DocumentPayload.ofDocx(os.toByteArray(), fileName);
    }
}