package com.htc.enter.serviceimpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.htc.enter.dto.DocumentPayload;
import com.htc.enter.model.Client;
import com.htc.enter.model.User;
import com.htc.enter.service.ClientAppService;
import com.htc.enter.service.ClientDocumentService;
import com.htc.enter.service.ClientService;
import com.htc.enter.service.UserAuthService;
import com.htc.enter.util.DocumentPasswordUtil;

@Service
public class ClientAppServiceImpl implements ClientAppService {

    private final ClientService clientService;
    private final ClientDocumentService documentService;
    private final UserAuthService authService;

    public ClientAppServiceImpl(ClientService clientService,
                                ClientDocumentService documentService,
                                UserAuthService authService) {
        this.clientService = clientService;
        this.documentService = documentService;
        this.authService = authService;
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentPayload buildClientsDocument() throws IOException {
        List<Client> clients = clientService.findAll();
        User current = authService.getCurrentUser();
        String password = DocumentPasswordUtil.resolvePassword(current, null);

        ByteArrayOutputStream os = documentService.generatePasswordProtectedClientsDocument(clients, password);
        String fileName = "clients_report.docx";
        return new DocumentPayload(os.toByteArray(), fileName, 
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }
}